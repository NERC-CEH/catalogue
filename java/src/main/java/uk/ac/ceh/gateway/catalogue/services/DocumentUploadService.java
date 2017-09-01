package uk.ac.ceh.gateway.catalogue.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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

@AllArgsConstructor
public class DocumentUploadService {
    private final File dropbox;
    private final DocumentRepository documentRepository;

    public void add(String guid, String filename, InputStream input) throws Exception {
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

    public void delete(String guid, String filename) throws Exception {
        val documentUpload = get(guid);
        val file = new File(documentUpload.getPath(), filename);
        if (file.exists()) FileUtils.forceDelete(file);
        documentUpload.getData().remove(filename);
        documentUpload.getMeta().remove(filename);
        save(documentUpload);
    }

    public void changeFileType(String guid, String filename, DocumentUpload.Type type) throws Exception {
        val documentUpload = get(guid);
        if (type.equals(DocumentUpload.Type.META)) {
            val documentUploadFile = documentUpload.getData().get(filename);
            if (null != documentUploadFile) {
                documentUpload.getData().remove(filename);
                documentUpload.getMeta().put(filename, documentUploadFile);
            }
        } else if (type.equals(DocumentUpload.Type.DATA)) {
            val documentUploadFile = documentUpload.getMeta().get(filename);
            if (null != documentUploadFile) {
                documentUpload.getMeta().remove(filename);
                documentUpload.getData().put(filename, documentUploadFile);
            }
        }
        save(documentUpload);
    }

    public DocumentUpload get (String guid) throws Exception {
        val file = createDataFile(guid);
        val mapper = new ObjectMapper();
        return mapper.readValue(file, DocumentUpload.class);
    }

    private File createDataFile (String guid) throws Exception {
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

    private void save (DocumentUpload documentUpload) throws Exception {
        val file = new File(documentUpload.getPath(), "_data.json");
        val mapper = new ObjectMapper();
        mapper.writeValue(file, documentUpload);
    }
}