package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.util.FileListUtils;
import uk.ac.ceh.gateway.catalogue.util.HashUtils;
import uk.ac.ceh.gateway.catalogue.util.ZipFileUtils;

import static uk.ac.ceh.gateway.catalogue.util.FilenameContainsFilterUtils.*;

public class UploadDocumentValidator {

    public static void validate(DocumentRepository documentRepository, UploadDocument document) {
        val uploadFilesMap = document.getUploadFiles();
        uploadFilesMap.values().stream()
            .forEach(uploadFiles -> {
                val directory = new File(uploadFiles.getPath());
                ZipFileUtils.archive(directory, unarchived -> {
                    boolean unknown = unknown(unarchived, uploadFiles);
                    boolean missing = missing(unarchived, uploadFiles);
                    boolean invalidHash = invalidHash(uploadFiles);
                    if (unknown || missing || invalidHash) save(documentRepository, document);
                });
            });
    }

    @SneakyThrows
    private static void save (DocumentRepository documentRepository, UploadDocument document) {
        documentRepository.save(CatalogueUser.PUBLIC_USER, document, "something changed whilst validating");
    }

    private static boolean unknown (File folder, UploadFiles uploadFiles) {
        boolean changed = false;
        val found = FileListUtils.absolutePathsTree(folder, doesNotContain(".hash"), doesNotContain(".hash"));
        for (val filename : found) {
            if (!uploadFiles.getDocuments().containsKey(filename) && !uploadFiles.getInvalid().containsKey(filename)) {
                uploadFiles.getInvalid().put(filename, UploadFileBuilder.create(new File(filename), UploadType.UNKNOWN_FILE)); 
                changed = true;
            }
        }
        return changed;
    }

    private static boolean missing (File folder, UploadFiles uploadFiles) {
        val missingInvalid = missingInvalid(folder, uploadFiles);
        val missingDocuments = missingDocuments(folder, uploadFiles);
        return missingInvalid || missingDocuments;
    }

    private static boolean missingInvalid (File folder, UploadFiles uploadFiles) {
        boolean changed = false;
        val found = FileListUtils.absolutePathsTree(folder, doesNotContain(".hash"), doesNotContain(".hash"));
        val invalid = uploadFiles.getInvalid();
        for (val entry : invalid.entrySet()) {
            val filename = entry.getKey();
            val uploadFile = entry.getValue();
            if (!found.contains(filename)) {
                changed = true;
                uploadFile.setType(UploadType.MISSING_FILE);
            }
        }
        return changed;
    }

    private static boolean missingDocuments (File folder, UploadFiles uploadFiles) {
        boolean changed = false;
        val found = FileListUtils.absolutePathsTree(folder, doesNotContain(".hash"), doesNotContain(".hash"));
        val documents = uploadFiles.getDocuments();
        val invalid = uploadFiles.getInvalid();
        for (val entry : documents.entrySet()) {
            val filename = entry.getKey();
            val uploadFile = entry.getValue();
            if (!found.contains(filename)) {
                changed = true;
                uploadFile.setType(UploadType.MISSING_FILE);
                invalid.put(filename, uploadFile);
            }
        }
        removeInvalid(uploadFiles);
        return changed;
    }

    private static boolean invalidHash(UploadFiles uploadFiles) {
        boolean changed = false;
        val documents = uploadFiles.getDocuments();
        val invalid = uploadFiles.getInvalid();
        for (val entry : documents.entrySet()) {
            val filename = entry.getKey();
            val uploadFile = entry.getValue();
            val actualHash = HashUtils.hash(new File(filename));
            if (!actualHash.equals(uploadFile.getHash())) {
                changed = true;
                uploadFile.setHash(actualHash);
                uploadFile.setType(UploadType.INVALID_HASH);
                invalid.put(filename, uploadFile);
            }
        }
        removeInvalid(uploadFiles);
        return changed;
    }

    private static void removeInvalid(UploadFiles uploadFiles) {
        val documents = uploadFiles.getDocuments();
        val invalid = uploadFiles.getInvalid();
        for (val filename : invalid.keySet()) {
            documents.remove(filename);
        }
    }
}