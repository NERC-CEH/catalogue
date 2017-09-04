package uk.ac.ceh.gateway.catalogue.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.model.DocumentUploadFile;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@AllArgsConstructor
public class DocumentUploadService {
    private final File dropbox;
    private final DocumentRepository documentRepository;

    public void add(String guid, String filename, InputStream input) throws IOException, DocumentRepositoryException {
        delete(guid, filename);

        val documentUpload = get(guid);
        File file = new File(documentUpload.getPath(), filename);
        OutputStream out = Files.newOutputStream(file.toPath());
        IOUtils.copy(input, out);
        String hash = DigestUtils.md5Hex(new FileInputStream(file));

        documentUpload.getData().put(filename, new DocumentUploadFile(
            filename,
            file.getAbsolutePath(),
            FilenameUtils.getExtension(filename),
            Files.probeContentType(file.toPath()),
            "utf-8",
            file.length(),
            hash
        ));
        save(documentUpload);
    }

    public void delete(String guid, String filename) throws IOException, DocumentRepositoryException {
        val documentUpload = get(guid);
        val file = new File(documentUpload.getPath(), filename);
        if (file.exists()) FileUtils.forceDelete(file);
        documentUpload.getData().remove(filename);
        documentUpload.getMeta().remove(filename);
        documentUpload.getInvalid().remove(filename);
        save(documentUpload);
    }

    public void changeFileType(String guid, String filename, DocumentUpload.Type type) throws IOException, DocumentRepositoryException {
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

    public DocumentUpload get (String guid) throws IOException, DocumentRepositoryException {
        val dataFile = createDataFile(guid);
        val mapper = new ObjectMapper();
        val documentUpload = mapper.readValue(dataFile, DocumentUpload.class);

        documentUpload.getInvalid().keySet().forEach(key -> documentUpload.getInvalid().remove(key));
        validateFilesInFolder(documentUpload);
        validateFilesFromData(documentUpload);

        return documentUpload;
    }

    private void validateFilesFromData(DocumentUpload documentUpload) throws IOException {
        documentUpload.getFiles().stream().forEach(file -> {
            if (!new File(file.getPath()).exists()) {
                documentUpload.getFiles(file.getType()).remove(file.getName());
                file.setType(DocumentUpload.Type.FILE_DOES_NOT_EXISTS);
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
                boolean inBoth = inData && inMeta;
                boolean notInEither = !inMeta && !inData;

                if (notInEither) invalidateBoth(name, hash, documentUpload, DocumentUpload.Type.NOT_META_OR_DATA, file);
                else if (inBoth) invalidateBoth(name, hash, documentUpload, DocumentUpload.Type.BOTH_META_AND_DATA, file);
                else if (inData) invalidateHash(name, hash, documentUpload, DocumentUpload.Type.DATA);
                else if (inMeta) invalidateHash(name, hash, documentUpload, DocumentUpload.Type.META);
            }
        }
    }

    private void invalidateBoth (String name, String hash, DocumentUpload documentUpload, DocumentUpload.Type type, File file) throws IOException {
        documentUpload.getData().remove(name);
        documentUpload.getMeta().remove(name);
        val documentUploadFile = new DocumentUploadFile(
            name,
            file.getAbsolutePath(),
            FilenameUtils.getExtension(name),
            Files.probeContentType(file.toPath()),
            "utf-8",
            file.length(),
            hash
        );
        documentUploadFile.setType(type);
        documentUpload.getInvalid().put(name, documentUploadFile);
    }

    private void invalidateHash (String name, String hash, DocumentUpload documentUpload, DocumentUpload.Type type) {
        val files = documentUpload.getFiles(type);
        DocumentUploadFile found = files.get(name);
        if (!found.getHash().equals(hash)) {
            files.remove(name);
            found.setType(DocumentUpload.Type.INVALID_HASH);
            documentUpload.getInvalid().put(name, found);
        }
    }

    private File createDataFile (String guid) throws IOException, DocumentRepositoryException {
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

    private void save (DocumentUpload documentUpload) throws IOException {
        val file = new File(documentUpload.getPath(), "_data.json");
        val mapper = new ObjectMapper();
        mapper.writeValue(file, documentUpload);
    }
}