package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse.FileInfo;

import java.util.Map;
import java.util.Set;

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
        uploadFiles.put("documents", new UploadFiles(dropboxResponse));
        uploadFiles.put("datastore", new UploadFiles(datastoreResponse));
        uploadFiles.put("supporting-documents", new UploadFiles(supportingDocumentsResponse));
        log.debug("Creating {}", this);
    }

    @Value
    public static class UploadFiles {
        Map<String, UploadFile> documents = Maps.newHashMap();
        Map<String, UploadFile> invalid = Maps.newHashMap();
        HubbubResponse.Pagination pagination;

        public UploadFiles(HubbubResponse hubbubResponse) {
            this.pagination = hubbubResponse.getPagination();
            hubbubResponse.getData()
                .forEach(fileInfo -> {
                    val uploadFile = new UploadFile(fileInfo);
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

        public UploadFile(FileInfo fileInfo) {
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