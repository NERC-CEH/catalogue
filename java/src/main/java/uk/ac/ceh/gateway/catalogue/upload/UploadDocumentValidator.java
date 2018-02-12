package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;

import lombok.val;
import uk.ac.ceh.gateway.catalogue.util.FileListUtils;
import uk.ac.ceh.gateway.catalogue.util.HashUtils;
import uk.ac.ceh.gateway.catalogue.util.ZipFileUtils;

import static uk.ac.ceh.gateway.catalogue.util.FilenameContainsFilterUtils.*;

public class UploadDocumentValidator {

    public static void validate(UploadDocument document) {
        val uploadFilesMap = document.getUploadFiles();
        uploadFilesMap.values().stream()
            .forEach(uploadFiles -> {
                val directory = new File(uploadFiles.getPath());
                ZipFileUtils.archive(directory, unarchived -> {
                    unknown(unarchived, uploadFiles);
                    missing(unarchived, uploadFiles);
                    invalidHash(uploadFiles);
                });
            });
    }

    private static void unknown (File folder, UploadFiles uploadFiles) {
        val found = FileListUtils.absolutePathsTree(folder, doesNotContain(".hash"), doesNotContain(".hash"));
        for (val filename : found) {
            if (!uploadFiles.getDocuments().containsKey(filename) && !uploadFiles.getInvalid().containsKey(filename)) {
                uploadFiles.getInvalid().put(filename, UploadFileBuilder.create(folder, uploadFiles.getPhysicalLocation(), new File(filename), UploadType.UNKNOWN_FILE)); 
            }
        }
    }

    private static void missing (File folder, UploadFiles uploadFiles) {
        missingInvalid(folder, uploadFiles);
        missingDocuments(folder, uploadFiles);
    }

    private static void missingInvalid (File folder, UploadFiles uploadFiles) {
        val found = FileListUtils.absolutePathsTree(folder, doesNotContain(".hash"), doesNotContain(".hash"));
        val invalid = uploadFiles.getInvalid();
        for (val entry : invalid.entrySet()) {
            val filename = entry.getKey();
            val uploadFile = entry.getValue();
            if (!found.contains(filename)) {
                uploadFile.setType(UploadType.MISSING_FILE);
            }
        }
    }

    private static void missingDocuments (File folder, UploadFiles uploadFiles) {
        val found = FileListUtils.absolutePathsTree(folder, doesNotContain(".hash"), doesNotContain(".hash"));
        val documents = uploadFiles.getDocuments();
        val invalid = uploadFiles.getInvalid();
        for (val entry : documents.entrySet()) {
            val filename = entry.getKey();
            val uploadFile = entry.getValue();
            if (!found.contains(filename)) {
                uploadFile.setType(UploadType.MISSING_FILE);
                invalid.put(filename, uploadFile);
            }
        }
        removeInvalid(uploadFiles);
    }

    private static void invalidHash(UploadFiles uploadFiles) {
        val documents = uploadFiles.getDocuments();
        val invalid = uploadFiles.getInvalid();
        for (val entry : documents.entrySet()) {
            val filename = entry.getKey();
            val uploadFile = entry.getValue();
            val actualHash = HashUtils.hash(new File(filename));
            if (!actualHash.equals(uploadFile.getHash())) {
                uploadFile.setHash(actualHash);
                uploadFile.setType(UploadType.INVALID_HASH);
                invalid.put(filename, uploadFile);
            }
        }
        removeInvalid(uploadFiles);
    }

    private static void removeInvalid(UploadFiles uploadFiles) {
        val documents = uploadFiles.getDocuments();
        val invalid = uploadFiles.getInvalid();
        for (val filename : invalid.keySet()) {
            documents.remove(filename);
        }
    }
}