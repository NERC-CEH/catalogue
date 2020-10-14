package uk.ac.ceh.gateway.catalogue.upload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class HubbubResponse {
    List<FileInfo> data;
    Pagination pagination;

    /*
    Regex assumes e.g. /eidchub/444bd227-b934-412e-8863-326afb77063b/dataset.csv
    or /dropbox/444bd227-b934-412e-8863-326afb77063b/PET/data.csv
    Looking to match on first two directory levels
     */
    private static String TRUNCATE_PATH = "^\\/.*\\/.{36}\\/(.*)";

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
        String truncatedPath;

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
            this.truncatedPath = path.replaceAll(TRUNCATE_PATH, "$1");
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
