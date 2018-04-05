package uk.ac.ceh.gateway.catalogue.upload;

import static uk.ac.ceh.gateway.catalogue.util.FilenameContainsFilterUtils.doesNotContain;

import java.io.File;
import java.util.Map;

import lombok.val;
import uk.ac.ceh.gateway.catalogue.util.FileListUtils;
import uk.ac.ceh.gateway.catalogue.util.HashUtils;
import uk.ac.ceh.gateway.catalogue.util.ZipFileUtils;

public class UploadDocumentValidator {

    public static void validate(UploadDocument document) {
        val uploadFilesMap = document.getUploadFiles();
        if (uploadFilesMap != null) {
            uploadFilesMap.values().stream()
                .forEach(uploadFiles -> {
                    val directory = new File(uploadFiles.getPath());
                    ZipFileUtils.archive(directory, unarchived -> {
                        unknown(document.getId()  , unarchived, uploadFiles);
                        missing(unarchived, uploadFiles);
                        invalidHash(uploadFiles);
                    });
                });
        }

        document.getUploadFiles().forEach((key, uploadFiles) -> {
            updatePhysicalLocation(document, uploadFiles, uploadFiles.getDocuments());
            updatePhysicalLocation(document, uploadFiles, uploadFiles.getInvalid());
        });
    }

    private static void updatePhysicalLocation(UploadDocument document, UploadFiles uploadFiles, Map<String, UploadFile> documents) {
        documents.forEach((docKey, uploadFile) -> {
            UploadFileBuilder.update(
                document.getParentId(),
                uploadFile,
                new File(uploadFiles.getPath()),
                uploadFiles.getPhysicalLocation(),
                new File(uploadFile.getPath()),
                uploadFile.getType(),
                uploadFile.getHash()
            );
        });
    }

    private static void unknown (String guid, File folder, UploadFiles uploadFiles) {
        val found = FileListUtils.absolutePathsTree(folder, doesNotContain(".hash"), doesNotContain(".hash"));
        found
        .parallelStream()
        .forEach(filename -> {
            if (!uploadFiles.getDocuments().containsKey(filename) && !uploadFiles.getInvalid().containsKey(filename)) {
                uploadFiles.getInvalid().put(filename, UploadFileBuilder.create(guid, folder, uploadFiles.getPhysicalLocation(), new File(filename), UploadType.UNKNOWN_FILE)); 
            }    
        });
    }

    private static void missing (File folder, UploadFiles uploadFiles) {
        missingInvalid(folder, uploadFiles);
        missingDocuments(folder, uploadFiles);
    }

    private static void missingInvalid (File folder, UploadFiles uploadFiles) {
        val found = FileListUtils.absolutePathsTree(folder, doesNotContain(".hash"), doesNotContain(".hash"));
        val invalid = uploadFiles.getInvalid();
        invalid.entrySet().parallelStream().forEach(entry -> {
            val filename = entry.getKey();
            val uploadFile = entry.getValue();
            if (!found.contains(filename)) {
                uploadFile.setType(UploadType.MISSING_FILE);
            }
        });
    }

    private static void missingDocuments (File folder, UploadFiles uploadFiles) {
        val found = FileListUtils.absolutePathsTree(folder, doesNotContain(".hash"), doesNotContain(".hash"));
        val documents = uploadFiles.getDocuments();
        val invalid = uploadFiles.getInvalid();
        documents.entrySet().parallelStream().forEach(entry -> {
            val filename = entry.getKey();
            val uploadFile = entry.getValue();
            if (!found.contains(filename)) {
                uploadFile.setType(UploadType.MISSING_FILE);
                invalid.put(filename, uploadFile);
            }
        });
        removeInvalid(uploadFiles);
    }

    private static void invalidHash(UploadFiles uploadFiles) {
        val documents = uploadFiles.getDocuments();
        val invalid = uploadFiles.getInvalid();
        documents.entrySet().parallelStream().forEach(entry -> {
            val filename = entry.getKey();
            val uploadFile = entry.getValue();
            val actualHash = HashUtils.hash(new File(filename));
            if (!actualHash.equals(uploadFile.getHash())) {
                uploadFile.setHash(actualHash);
                uploadFile.setType(UploadType.INVALID_HASH);
                invalid.put(filename, uploadFile);
            }
        });
        removeInvalid(uploadFiles);
    }

    private static void removeInvalid(UploadFiles uploadFiles) {
        val documents = uploadFiles.getDocuments();
        val invalid = uploadFiles.getInvalid();
        invalid.keySet().parallelStream()
            .forEach(filename -> documents.remove(filename));
    }
}