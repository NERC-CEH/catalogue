package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;

import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

@AllArgsConstructor
public class UploadDocumentService {

    private static final Pattern regex = Pattern.compile("([a-f0-9]{32})\\s*\\*?(.*)");
    private final DocumentRepository documentRepository;
    private final Map<String, File> folders;

    @SneakyThrows
    public UploadDocument create(CatalogueUser user, GeminiDocument geminiDocument) {
        val guid = geminiDocument.getId();
        Map<String, UploadFiles> uploadFiles = Maps.newHashMap();
        folders.entrySet().stream().forEach(entry -> {
            val key = entry.getKey();
            val value = entry.getValue();
            val directory = new File(value, guid);
            uploadFiles.put(key, new UploadFiles(directory.getAbsolutePath(), Maps.newHashMap(), Maps.newHashMap(), false));
        });
        val uploadDocument = new UploadDocument(guid, uploadFiles);
        documentRepository.saveNew(user, uploadDocument, "eidc", "creating new upload document");
        geminiDocument.setUploadId(uploadDocument.getId());
        documentRepository.save(user, geminiDocument, "eidc", String.format("updating upload id: %s", uploadDocument.getId()));
        return uploadDocument;
    }

    public void add() {
    }

    public void move() {
    }

    public void delete() {
    }

    public void zip() {
    }

    public void unzip() {
    }

    public void acceptInvalid() {
    }

    private String hash(File file) throws IOException {
        val input = new FileInputStream(file);
        val hash = DigestUtils.md5Hex(input);
        input.close();
        return hash;
    }
}