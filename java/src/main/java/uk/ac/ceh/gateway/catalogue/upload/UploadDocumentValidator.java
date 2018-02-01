package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.val;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.util.FileListUtils;
import uk.ac.ceh.gateway.catalogue.util.FilenameContainsFilterUtils;
import uk.ac.ceh.gateway.catalogue.util.ZipFileUtils;

public class UploadDocumentValidator {
    public static void validate(DocumentRepository documentRepository, UploadDocument document) {
        val uploadFilesMap = document.getUploadFiles();
        uploadFilesMap.values().stream()
            .forEach(uploadFiles -> {
                val directory = new File(uploadFiles.getPath());
                ZipFileUtils.archive(directory, folder -> {
                    val found = FileListUtils.absolutePathsTree(folder, FilenameContainsFilterUtils.doesNotContain(".hash"), FilenameContainsFilterUtils.doesNotContain(".hash"));
                    found.removeAll(uploadFiles.getDocuments().keySet());
                    found.removeAll(uploadFiles.getInvalid().keySet());
                    found.stream().forEach(filename -> {
                        // uploadFiles.getInvalid().put(filename, value);
                    });
                });
                
                // first extract all the files
                // then see if any file is not in documents or invalid
                // after that go through all known invalid files and check it exists
                // if it exists check it's hash has not changed
            });
    }
}