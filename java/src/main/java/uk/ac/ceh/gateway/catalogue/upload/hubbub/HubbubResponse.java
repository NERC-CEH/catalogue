package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
public class HubbubResponse {
    List<FileInfo> data;
    Links links;
    Meta meta;

    @JsonCreator
    public HubbubResponse(
        @JsonProperty("data") List<FileInfo> data,
        @JsonProperty("links") Links links,
        @JsonProperty("meta") Meta meta
    ) {
        this.data = data;
        this.links = links;
        this.meta = meta;
    }

    @Value
    public static class FileInfo {
        Long bytes;
        String datasetId;
        String datastore;
        String format;
        String hash;
        Double hashingTime;
        LocalDateTime lastModified;
        LocalDateTime lastValidated;
        String path;
        String status;
        String mimeType;

        @JsonCreator
        public FileInfo(
            @JsonProperty("bytes") Long bytes,
            @JsonProperty("datasetId") String datasetId,
            @JsonProperty("datastore") String datastore,
            @JsonProperty("format") String format,
            @JsonProperty("hash") String hash,
            @JsonProperty("hashingTime") Double hashingTime,
            @JsonProperty("lastModified") LocalDateTime lastModified,
            @JsonProperty("lastValidated") LocalDateTime lastValidated,
            @JsonProperty("path") String path,
            @JsonProperty("status") String status,
            @JsonProperty("mimeType") String mimeType
        ) {
            this.bytes = bytes;
            this.datasetId = datasetId;
            this.datastore = datastore;
            this.format = format;
            this.hash = hash;
            this.hashingTime = hashingTime;
            this.lastModified = lastModified;
            this.lastValidated = lastValidated;
            this.path = path;
            this.status = status;
            this.mimeType = mimeType;
        }
    }

    @Value
    public static class Links {
        String self;
        String first;
        String prev;
        String next;
        String last;

        @JsonCreator
        public Links(
            @JsonProperty("self") String self,
            @JsonProperty("first") String first,
            @JsonProperty("prev") String prev,
            @JsonProperty("next") String next,
            @JsonProperty("last") String last
        ) {
            this.self = self;
            this.first = first;
            this.prev = prev;
            this.next = next;
            this.last = last;
        }
    }

    @Value
    public static class Meta {
        int currentPage;
        int lastPage;
        int pageSize;
        int totalFiles;

        @JsonCreator
        public Meta(
            @JsonProperty("currentPage") int currentPage,
            @JsonProperty("lastPage") int lastPage,
            @JsonProperty("pageSize") int pageSize,
            @JsonProperty("totalFiles") int totalFiles
        ) {
            this.currentPage = currentPage;
            this.lastPage = lastPage;
            this.pageSize = pageSize;
            this.totalFiles = totalFiles;
        }
    }
}
