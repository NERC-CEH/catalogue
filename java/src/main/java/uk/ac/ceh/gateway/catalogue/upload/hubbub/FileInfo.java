package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class FileInfo {
    private static String TRUNCATE_PATH = 	"^/.*/.{36}/(.*)";
//    TODO: check what fields need extracting from Hubbub response
    Long bytes;
    String hash;
    String name;
    String path;
    String status;
    Long time;
    String truncatedPath;

    public FileInfo(JsonNode node) {
        this(
            node.get("bytes").asLong(),
            node.get("hash").asText(),
            node.get("name").asText(),
            node.get("path").asText(),
            node.get("status").asText(),
            node.get("time").asLong()
        );
    }

    public FileInfo(Long bytes, String hash, String name, String path, String status, Long time) {
        this.bytes = bytes;
        this.hash = hash;
        this.name = name;
        this.path = path;
        this.status = status;
        this.time = time;
        this.truncatedPath = path.replaceAll(TRUNCATE_PATH, "$1");
    }
}
