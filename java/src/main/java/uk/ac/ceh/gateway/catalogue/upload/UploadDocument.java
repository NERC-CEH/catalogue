package uk.ac.ceh.gateway.catalogue.upload;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
@Value
public class UploadDocument {
    String id;
    Map<String, UploadFiles> uploadFiles = Maps.newHashMap();

    private static Set<String> VALID_TYPES = Sets.newHashSet(
            "MOVING_FROM",
            "MOVING_TO",
            "NO_HASH",
            "VALID",
            "VALIDATING_HASH",
            "WRITING"
    );

    public UploadDocument(
            @NonNull String id,
            @NonNull HubbubResponse dropboxResponse,
            @NonNull HubbubResponse datastoreResponse,
            @NonNull HubbubResponse supportingDocumentsResponse
    ) {
        this.id = id;
        uploadFiles.put("documents", new UploadFiles("dropbox", id, dropboxResponse));
        uploadFiles.put("datastore", new UploadFiles("eidchub", id, datastoreResponse));
        uploadFiles.put("supporting-documents", new UploadFiles("supporting-documents", id, supportingDocumentsResponse));
        log.debug("Creating {}", this);
    }

    @Value
    public static class UploadFiles {
        Map<String, UploadFile> documents = Maps.newHashMap();
        Map<String, UploadFile> invalid = Maps.newHashMap();
        HubbubResponse.Pagination pagination;

        public UploadFiles(String directory, String id, HubbubResponse hubbubResponse) {
            this.pagination = hubbubResponse.getPagination();
            val folder = format("%s/%s", directory, id);
            hubbubResponse.getData()
                    .forEach(fileInfo -> {
                        val uploadFile = new UploadFile(fileInfo, folder);
                        if (VALID_TYPES.contains(uploadFile.getType()))
                            documents.put(uploadFile.getPath(), uploadFile);
                        else
                            invalid.put(uploadFile.getPath(), uploadFile);
                    });
        }
    }

    @Value
    public static class UploadFile {
        String path;
        String name;
        String id;
        Long time;
        String physicalLocation;
        String format;
        String mediatype;
        String encoding = "utf-8";
        Long bytes;
        String hash;
        String type;

        public UploadFile(HubbubResponse.FileInfo fileInfo, String folder) {
            this.path = fileInfo.getPath();
            this.name = fileInfo.getTruncatedPath();
            this.id = name.replaceAll("[^\\w?]", "-");

            this.time = fileInfo.getTime();
            this.physicalLocation = fileInfo.getPhysicalLocation();
            this.format = fileInfo.getFormat();
            this.mediatype = fileInfo.getMediatype();
            this.bytes = fileInfo.getBytes();
            this.hash = fileInfo.getHash();
            this.type =  fileInfo.getStatus();
        }
    }
}