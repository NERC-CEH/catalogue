package uk.ac.ceh.gateway.catalogue.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.model.DocumentUploadFile;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload.Type;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@AllArgsConstructor
public class DocumentUploadService {
    private final File directory;
    private final DocumentRepository documentRepository;

    public void add(String guid, String filename, InputStream input) throws IOException, DocumentRepositoryException {
        if (StringUtils.isBlank(guid)) throw new IllegalArgumentException("guid can not be blank");
        if (StringUtils.isBlank(filename)) throw new IllegalArgumentException("filename can not be blank");

        delete(guid, filename);

        val documentUpload = get(guid);
        val folder = new File(documentUpload.getPath());
        val file = new File(folder, filename);
        FileUtils.copyInputStreamToFile(input, file);

        val documentUploadFile = new DocumentUploadFile();
        documentUploadFile.addComment("added by service");
        documentUploadFile.setName(filename);
        documentUploadFile.setId(filename.replaceAll("[^\\w?]","_"));
        documentUploadFile.setPath(file.getAbsolutePath());
        documentUploadFile.setFormat(FilenameUtils.getExtension(filename));
        documentUploadFile.setMediatype(Files.probeContentType(file.toPath()));
        documentUploadFile.setEncoding("utf-8");
        documentUploadFile.setBytes(file.length());
        documentUploadFile.setHash(hash(file));

        documentUpload.getDocuments().put(filename, documentUploadFile);
        save(documentUpload);
    }

    public void delete(String guid, String filename) throws IOException, DocumentRepositoryException {
        if (StringUtils.isBlank(guid)) throw new IllegalArgumentException("guid can not be blank");
        if (StringUtils.isBlank(filename)) throw new IllegalArgumentException("filename can not be blank");

        val documentUpload = get(guid);
        val file = new File(documentUpload.getPath(), filename);
        if (file.exists()) FileUtils.forceDelete(file);
        documentUpload.getDocuments().remove(filename);
        documentUpload.getInvalid().remove(filename);
        save(documentUpload);
    }

    public void acceptInvalid(String guid, String filename) throws IOException, DocumentRepositoryException {
        if (StringUtils.isBlank(guid)) throw new IllegalArgumentException("guid can not be blank");
        if (StringUtils.isBlank(filename)) throw new IllegalArgumentException("filename can not be blank");

        val documentUpload = get(guid);
        val documentUploadFile = documentUpload.getInvalid().get(filename);
        if (null != documentUploadFile) {
            documentUploadFile.addComment("accepted");
            documentUploadFile.setType(Type.DOCUMENTS);
            documentUpload.getInvalid().remove(filename);
            documentUpload.getDocuments().put(filename, documentUploadFile);
            save(documentUpload);
        }
    }

    public DocumentUpload get(String guid) throws IOException, DocumentRepositoryException {
        if (StringUtils.isBlank(guid)) throw new IllegalArgumentException("guid can not be blank");

        val dataFile = createDataFile(guid);
        val mapper = new ObjectMapper();
        val documentUpload = mapper.readValue(dataFile, DocumentUpload.class);
        updateWithChecksumsFile(documentUpload);
        updateKnownInvalidFiles(documentUpload);
        updateFromDataAndMetaToInvalid(documentUpload);
        autoFix(documentUpload);
        save(documentUpload);
        return documentUpload;
    }

    private static final Pattern regex = Pattern.compile("([a-f0-9]{32})\\ \\*(.*)");

    private void updateWithChecksumsFile(DocumentUpload documentUpload) throws IOException {
        val folder = new File(documentUpload.getPath());
        val checksums = new File(folder, "checksums.hash");
        
        if (checksums.exists()) {
            val lines = FileUtils.readLines(checksums, Charset.defaultCharset());
            for(val line : lines) {
                val matches = regex.matcher(line);
                if (matches.matches()) {
                    val hash = matches.group(1);
                    val name = matches.group(2);
                    documentUpload.getDocuments().remove(name);
                    documentUpload.getInvalid().remove(name);

                    val documentUploadFile = new DocumentUploadFile();
                    documentUploadFile.addComment("added from checksums.hash");
                    documentUploadFile.setName(name);
                    documentUploadFile.setId(name.replaceAll("[^\\w?]","-"));
                    documentUploadFile.setFormat(FilenameUtils.getExtension(name));
                    documentUploadFile.setEncoding("utf-8");
                    documentUploadFile.setHash(hash);

                    val file = new File(folder, name);
                    documentUploadFile.setPath(file.getAbsolutePath());
                    documentUploadFile.setMediatype(Files.probeContentType(file.toPath()));
                    documentUploadFile.setBytes(file.length());

                    documentUpload.getDocuments().put(name, documentUploadFile);
                }
            }
        }
    }

    private void updateKnownInvalidFiles(DocumentUpload documentUpload) throws IOException {
        val documentUploadFiles = documentUpload.getInvalid().values();
        for(val documentUploadFile : documentUploadFiles) {
            val name = documentUploadFile.getName();
            val path = documentUploadFile.getPath();
            val file = new File(path);
            val exists = file.exists();
            switch (Type.valueOf(documentUploadFile.getType())) {
                case UNKNOWN_FILE:
                case INVALID_HASH:
                    if (!exists) {
                        documentUploadFile.setType(Type.MISSING_FILE);
                        documentUploadFile.addComment("File is missing");
                        documentUpload.getInvalid().put(name, documentUploadFile);
                    }
                    break;
                default:
                    if (exists) {
                        documentUploadFile.setType(Type.UNKNOWN_FILE);
                        documentUploadFile.addComment("Uknown file");
                        documentUpload.getInvalid().put(name, documentUploadFile);
                    }
                    break;
            }
        }
    }

    private void updateFromDataAndMetaToInvalid(DocumentUpload documentUpload) throws IOException {
        validateHashesAndMissingFiles(documentUpload);
        validateUnknownFiles(documentUpload);
    }

    private void validateHashesAndMissingFiles(DocumentUpload documentUpload) throws IOException {
        val documentUploadFiles = documentUpload.getFiles();
        for(val documentUploadFile : documentUploadFiles) {
            val path = documentUploadFile.getPath();
            val file = new File(path);
            if (!file.exists()) {
                val name = documentUploadFile.getName();
                val type = documentUploadFile.getType();
                documentUpload.getFiles(type).remove(name);

                documentUploadFile.setType(Type.MISSING_FILE);
                documentUploadFile.addComment("File is missing");
                documentUpload.getInvalid().put(name, documentUploadFile);
            } else {
                val hash = hash(file);
                if (!documentUploadFile.getHash().equals(hash)) {
                    val name = documentUploadFile.getName();
                    val type = documentUploadFile.getType();
                    documentUpload.getFiles(type).remove(name);

                    documentUploadFile.setHash(hash);
                    documentUploadFile.setType(Type.INVALID_HASH);
                    documentUploadFile.addComment("File has changed");
                    documentUpload.getInvalid().put(name, documentUploadFile);
                }
            }
        }
    }

    private void validateUnknownFiles(DocumentUpload documentUpload) throws IOException {
        val path = documentUpload.getPath();
        val folder = new File(path);
        val files = folder.listFiles(file -> !file.getName().equals("_data.json") && !file.getName().equals("checksums.hash"));
        for(val file : files) {
            val name = file.getName();
            val inData = documentUpload.getDocuments().containsKey(name);
            val alreadyInvalid = documentUpload.getInvalid().containsKey(name);
            if (!inData && !alreadyInvalid) {
                val documentUploadFile = new DocumentUploadFile();
                documentUploadFile.setName(name);
                documentUploadFile.setId(name.replaceAll("[^\\w?]","-"));
                documentUploadFile.setPath(file.getAbsolutePath());
                documentUploadFile.setFormat(FilenameUtils.getExtension(name));
                documentUploadFile.setMediatype(Files.probeContentType(file.toPath()));
                documentUploadFile.setEncoding("utf-8");
                documentUploadFile.setBytes(file.length());
                documentUploadFile.setHash(hash(file));

                documentUploadFile.setType(Type.UNKNOWN_FILE);
                documentUploadFile.addComment("Unknown file");
                documentUpload.getInvalid().put(name, documentUploadFile);
            }
        }
    }

    private void autoFix(DocumentUpload documentUpload) throws IOException {
        for (val entrySet : documentUpload.getFiles(Type.DOCUMENTS).entrySet()) {
            val name = entrySet.getKey();
            val documentUploadFile = entrySet.getValue();
            val file = new File(documentUpload.getPath(), name);
            documentUploadFile.setName(name);
            documentUploadFile.setFormat(FilenameUtils.getExtension(name));
            documentUploadFile.setMediatype(Files.probeContentType(file.toPath()));
            documentUploadFile.setEncoding("utf-8");
            documentUploadFile.setBytes(file.length());
            documentUploadFile.setType(Type.DOCUMENTS);
            documentUploadFile.setPath(file.getAbsolutePath());
        }
    }

    private File createDataFile(String guid) throws IOException, DocumentRepositoryException {
        val folder = new File(directory, guid);
        if (!folder.exists()) FileUtils.forceMkdir(folder);

        val file = new File(folder, "_data.json");
        if (!file.exists()) {
            val document = documentRepository.read(guid);
            val documentUpload = new DocumentUpload(
                document.getTitle(),
                document.getType(),
                guid,
                folder.getAbsolutePath()
            );
            saveJson(documentUpload);
        }
        return file;
    }

    private void save(DocumentUpload documentUpload) throws IOException {
        saveJson(documentUpload);

        val checksums = new File(documentUpload.getPath(), "checksums.hash");
        val documentUploadFiles = documentUpload.getDocuments()
            .values()
            .stream()
            .map(documentUploadFile -> String.format("%s *%s", documentUploadFile.getHash(), documentUploadFile.getName()))
            .collect(Collectors.toList());

        FileUtils.writeLines(checksums, documentUploadFiles);
    }

    private void saveJson(DocumentUpload documentUpload) throws IOException {
        val json = new File(documentUpload.getPath(), "_data.json");
        val mapper = new ObjectMapper();
        mapper.writeValue(json, documentUpload);
    }

    private String hash(File file) throws IOException {
        val input = new FileInputStream(file);
        val hash = DigestUtils.md5Hex(input);
        input.close();
        return hash;
    }
}