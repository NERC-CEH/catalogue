package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class FileInfo {
    private static String TRUNCATE_PATH = 	"^/.*/.{36}/(.*)";
    Long bytes;
    String hash;
    String name;
    String path;
    String status;
    Long time;
    String truncatedPath;

    public FileInfo(JsonNode node) {
        this.bytes = node.get("bytes").asLong();
        if (node.has("hash")) {
            this.hash = node.get("hash").asText();
        } else {
            this.hash = "";
        }
        this.name = node.get("name").asText();
        this.path = node.get("path").asText();
        this.status = node.get("status").asText();
        this.time = node.get("time").asLong();
        this.truncatedPath = this.path.replaceAll(TRUNCATE_PATH, "$1");
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
