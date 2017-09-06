package uk.ac.ceh.gateway.catalogue.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.model.DocumentUploadFile;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Slf4j
@AllArgsConstructor
public class DocumentUploadService {
    private final File dropbox;
    private final DocumentRepository documentRepository;

    public void add(String guid, String filename, InputStream input) throws IOException, DocumentRepositoryException {
        if (StringUtils.isBlank(guid)) throw new IllegalArgumentException("guid can not be blank");
        if (StringUtils.isBlank(filename)) throw new IllegalArgumentException("filename can not be blank");

        delete(guid, filename);

        val documentUpload = get(guid);
        File file = new File(documentUpload.getPath(), filename);
        OutputStream out = Files.newOutputStream(file.toPath());
        IOUtils.copyLarge(input, out);
        input.close();
        out.close();
        String hash = DigestUtils.md5Hex(new FileInputStream(file));

        val documentUploadFile = new DocumentUploadFile();
        documentUploadFile.setName(filename);
        documentUploadFile.setPath(file.getAbsolutePath());
        documentUploadFile.setFormat(FilenameUtils.getExtension(filename));
        documentUploadFile.setMediatype(Files.probeContentType(file.toPath()));
        documentUploadFile.setEncoding("utf-8");
        documentUploadFile.setBytes(file.length());
        documentUploadFile.setHash(hash);

        documentUpload.getData().put(filename, documentUploadFile);
        save(documentUpload);
    }

    public void delete(String guid, String filename) throws IOException, DocumentRepositoryException {
        if (StringUtils.isBlank(guid)) throw new IllegalArgumentException("guid can not be blank");
        if (StringUtils.isBlank(filename)) throw new IllegalArgumentException("filename can not be blank");

        val documentUpload = get(guid);
        val file = new File(documentUpload.getPath(), filename);
        if (file.exists()) FileUtils.forceDelete(file);
        documentUpload.getData().remove(filename);
        documentUpload.getMeta().remove(filename);
        documentUpload.getInvalid().remove(filename);
        save(documentUpload);
    }

    public void changeFileType(String guid, String filename, DocumentUpload.Type type) throws IOException, DocumentRepositoryException {
        if (StringUtils.isBlank(guid)) throw new IllegalArgumentException("guid can not be blank");
        if (StringUtils.isBlank(filename)) throw new IllegalArgumentException("filename can not be blank");

        val documentUpload = get(guid);
        if (type.equals(DocumentUpload.Type.META)) changeFileType(guid, filename, DocumentUpload.Type.DATA, DocumentUpload.Type.META, documentUpload);
        else if (type.equals(DocumentUpload.Type.DATA)) changeFileType(guid, filename, DocumentUpload.Type.META, DocumentUpload.Type.DATA, documentUpload);
        save(documentUpload);
    }

    private void changeFileType(String guid, String filename, DocumentUpload.Type from, DocumentUpload.Type to, DocumentUpload documentUpload) throws IOException, DocumentRepositoryException {
        DocumentUploadFile documentUploadFile = documentUpload.getFiles(from).get(filename);
        if (null == documentUploadFile) documentUploadFile = documentUpload.getInvalid().get(filename);
        if (null != documentUploadFile) {
            documentUploadFile.setType(to);
            documentUpload.getInvalid().remove(filename);
            documentUpload.getFiles(from).remove(filename);
            documentUpload.getFiles(to).put(filename, documentUploadFile);
        }
    }

    public void acceptInvalid(String guid, String filename) throws IOException, DocumentRepositoryException {
        if (StringUtils.isBlank(guid)) throw new IllegalArgumentException("guid can not be blank");
        if (StringUtils.isBlank(filename)) throw new IllegalArgumentException("filename can not be blank");

        val documentUpload = get(guid);
        val documentUploadFile = documentUpload.getInvalid().get(filename);
        if (null != documentUploadFile) {
            documentUploadFile.addComment("accepted");
            documentUploadFile.setType(DocumentUpload.Type.DATA);
            documentUpload.getInvalid().remove(filename);
            documentUpload.getData().put(filename, documentUploadFile);
            save(documentUpload);
        }
    }

    public DocumentUpload get(String guid) throws IOException, DocumentRepositoryException {
        if (StringUtils.isBlank(guid)) throw new IllegalArgumentException("guid can not be blank");

        val dataFile = createDataFile(guid);
        val mapper = new ObjectMapper();
        val documentUpload = mapper.readValue(dataFile, DocumentUpload.class);
        validateFilesInFolder(documentUpload);
        validateFilesFromData(documentUpload);
        removeDuplicates(documentUpload);
        autoFix(documentUpload, DocumentUpload.Type.DATA);
        autoFix(documentUpload, DocumentUpload.Type.META);
        save(documentUpload);
        return documentUpload;
    }

    private void removeDuplicates(DocumentUpload documentUpload) {
        for (val documentUploadFile : documentUpload.getData().values()) {
            val name = documentUploadFile.getName();
            if (documentUpload.getMeta().containsKey(name)) {
                documentUpload.getMeta().remove(name);
            }
        }
    }

    private void autoFix(DocumentUpload documentUpload, DocumentUpload.Type type) throws IOException {
        for (val entrySet : documentUpload.getFiles(type).entrySet()) {
            val name = entrySet.getKey();
            val documentUploadFile = entrySet.getValue();
            val file = new File(documentUpload.getPath(), name);
            documentUploadFile.setName(name);
            documentUploadFile.setFormat(FilenameUtils.getExtension(name));
            documentUploadFile.setMediatype(Files.probeContentType(file.toPath()));
            documentUploadFile.setEncoding("utf-8");
            documentUploadFile.setBytes(file.length());
            documentUploadFile.setType(type);
            documentUploadFile.setPath(file.getAbsolutePath());
        }
    }

    private void validateFilesFromData(DocumentUpload documentUpload) throws IOException {
        documentUpload.getFiles().forEach(file -> {
            if (!new File(file.getPath()).exists()) {
                documentUpload.getFiles(file.getType()).remove(file.getName());
                file.setType(DocumentUpload.Type.FILE_DOES_NOT_EXISTS);
                file.addComment("The file is missing");
                documentUpload.getInvalid().put(file.getName(), file);
            }
        });
    }

    private void validateFilesInFolder(DocumentUpload documentUpload) throws IOException {
        val folder = new File(documentUpload.getPath());
        val files = folder.listFiles();
        for (val file : files) {
            if (!file.getName().equals("_data.json")) {
                String hash = DigestUtils.md5Hex(new FileInputStream(file));
                String name = file.getName();

                boolean inData = documentUpload.getData().containsKey(name);
                boolean inMeta = documentUpload.getMeta().containsKey(name);
                boolean notInEither = !inMeta && !inData;

                if (notInEither) invalidateBoth(name, hash, documentUpload, DocumentUpload.Type.NOT_META_OR_DATA, "Unknown file exists in folder", file);
                else if (inData) invalidateHash(name, hash, documentUpload, DocumentUpload.Type.DATA, "File's contents has changed");
                else if (inMeta) invalidateHash(name, hash, documentUpload, DocumentUpload.Type.META, "File's contents has changed");
            }
        }
    }

    private void invalidateBoth(String name, String hash, DocumentUpload documentUpload,
        DocumentUpload.Type type, String comment, File file) throws IOException {
        documentUpload.getData().remove(name);
        documentUpload.getMeta().remove(name);
        val documentUploadFile = new DocumentUploadFile();
        documentUploadFile.setName(name);
        documentUploadFile.setPath(file.getAbsolutePath());
        documentUploadFile.setFormat(FilenameUtils.getExtension(name));
        documentUploadFile.setMediatype(Files.probeContentType(file.toPath()));
        documentUploadFile.setEncoding("utf-8");
        documentUploadFile.setBytes(file.length());
        documentUploadFile.setHash(hash);
        documentUploadFile.setType(type);
        documentUploadFile.addComment(comment);
        documentUpload.getInvalid().put(name, documentUploadFile);
    }

    private void invalidateHash(String name, String hash, DocumentUpload documentUpload, DocumentUpload.Type type, String comment) {
        val files = documentUpload.getFiles(type);
        DocumentUploadFile found = files.get(name);
        if (!found.getHash().equals(hash)) {
            files.remove(name);
            found.setType(DocumentUpload.Type.INVALID_HASH);
            found.addComment(comment);
            found.setHash(hash);
            documentUpload.getInvalid().put(name, found);
        }
    }

    private File createDataFile(String guid) throws IOException, DocumentRepositoryException {
        val folder = new File(dropbox, guid);
        FileUtils.forceMkdir(folder);

        val file = new File(folder, "_data.json");
        if (!file.exists()) {
            val document = documentRepository.read(guid);
            val documentUpload = new DocumentUpload(
                document.getTitle(),
                document.getType(),
                guid,
                folder.getAbsolutePath()
            );
            save(documentUpload);
        }
        return file;
    }

    private void save(DocumentUpload documentUpload) throws IOException {
        val file = new File(documentUpload.getPath(), "_data.json");
        val mapper = new ObjectMapper();
        mapper.writeValue(file, documentUpload);
    }
}