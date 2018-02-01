package uk.ac.ceh.gateway.catalogue.upload;

import lombok.val;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

public class UploadDocumentValidator {
    public static void validate(DocumentRepository documentRepository, UploadDocument document) {
        val uploadFilesMap = document.getUploadFiles();
        uploadFilesMap.values().stream()
            .forEach(uploadFiles -> {
                uploadFiles.getPath();
                // first extract all the files
                // then see if any file is not in documents or invalid
                // after that go through all known invalid files and check it exists
                // if it exists check it's hash has not changed
            });
    }
}