package uk.ac.ceh.gateway.catalogue.upload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

import static java.lang.String.format;

@Value
public class HubbubResponse {
    List<FileInfo> data;
    Pagination pagination;

    @JsonCreator
    public HubbubResponse(
            @JsonProperty("data") List<FileInfo> data,
            @JsonProperty("pagination") Pagination pagination
    ) {
        this.data = data;
        this.pagination = pagination;
    }

    @Value
    public static class FileInfo {
        Long bytes;
        String format;
        String hash;
        String id;
        String mediatype;
        String mtime;
        String name;
        String path;
        String physicalLocation;
        String status;
        Long time;

        @JsonCreator
        public FileInfo(
                @JsonProperty("bytes") Long bytes,
                @JsonProperty("format") String format,
                @JsonProperty("hash") String hash,
                @JsonProperty("id") String id,
                @JsonProperty("mediatype") String mediatype,
                @JsonProperty("mtime") String mtime,
                @JsonProperty("name") String name,
                @JsonProperty("path") String path,
                @JsonProperty("physicalLocation") String physicalLocation,
                @JsonProperty("status") String status,
                @JsonProperty("time") Long time
        ) {
            this.bytes = bytes;
            this.format = format;
            this.hash = hash;
            this.id = id;
            this.mediatype = mediatype;
            this.mtime = mtime;
            this.name = name;
            this.path = path;
            this.physicalLocation = physicalLocation;
            this.status = status;
            this.time = time;
        }

        public String getTruncatedPath(String folder) {
            return path.replace(format("/%s/%s/", folder, id), "");
        }
    }

    @Value
    public static class Pagination {
        int page;
        int size;
        int total;

        @JsonCreator
        public Pagination(
                @JsonProperty("page") int page,
                @JsonProperty("size") int size,
                @JsonProperty("total") int total
        ) {
            this.page = page;
            this.size = size;
            this.total = total;
        }
    }
}
