package uk.ac.ceh.gateway.catalogue.templateHelpers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpMethod.GET;

@Service
@Slf4j
public class FileListService {

    private final String baseUri;
    private final RestTemplate restTemplate;

    public FileListService(
        @Value("${documents.baseUri}") String baseUri,
        @Qualifier("normal") RestTemplate restTemplate
    ) {
        this.baseUri = baseUri;
        this.restTemplate = restTemplate;
        log.info("Creating");
    }

    @lombok.Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileListInfo {
        String name;
        String type;
    }

    public List<String> getFileList(String datasetId) {
        log.info("Getting file list for dataset: {}", datasetId);
        return getFileListRecursive(datasetId, "");
    }

    /**
     * Method to traverse directories and collect all file names
     * @param datasetId the dataset ID
     * @param path the current path
     * @return list of all file names found
     */
    private List<String> getFileListRecursive(String datasetId, String path) {
        List<String> allFiles = new ArrayList<>();

        String urlTemplate = path.isEmpty()
            ? format("%s/datastore/eidchub/%s/?format=json", baseUri, datasetId)
            : format("%s/datastore/eidchub/%s/%s/?format=json", baseUri, datasetId, path);

        log.info("Getting files from url: {}", urlTemplate);

        try {
            ResponseEntity<List<FileListService.FileListInfo>> response = restTemplate.exchange(
                urlTemplate,
                GET,
                null,
                new ParameterizedTypeReference<>() {}
            );

            List<FileListService.FileListInfo> items = response.getBody();
            if (items == null || items.isEmpty()) {
                return allFiles;
            }

            for (FileListService.FileListInfo item : items) {
                if ("file".equalsIgnoreCase(item.getType())) {
                    allFiles.add(item.getName());

                } else if ("directory".equalsIgnoreCase(item.getType())) {
                    log.info("Directory found: {}, traversing it looking for files and sub-directories", item.getName());
                    String newPath = path.isEmpty()
                        ? item.getName()
                        : path + "/" + item.getName();

                    List<String> filesInDirectory = getFileListRecursive(datasetId, newPath);
                    allFiles.addAll(filesInDirectory);
                }
            }

        } catch (Exception e) {
            log.error("Error fetching file list from {}: {}", urlTemplate, e.getMessage());
        }

        return allFiles;
    }
}
