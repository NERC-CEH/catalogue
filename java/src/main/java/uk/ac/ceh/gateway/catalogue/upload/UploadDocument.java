package uk.ac.ceh.gateway.catalogue.upload;

import com.fasterxml.jackson.databind.JsonNode;
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
            @NonNull JsonNode dropboxNode,
            @NonNull JsonNode datastoreNode,
            @NonNull JsonNode supportingDocumentsNode
    ) {
        this.id = id;
        uploadFiles.put("documents", new UploadFiles("dropbox", id, dropboxNode));
        uploadFiles.put("datastore", new UploadFiles("eidchub", id, datastoreNode));
        uploadFiles.put("supporting-documents", new UploadFiles("supporting-documents", id, supportingDocumentsNode));
        log.debug("Creating {}", this);
    }

    @Value
    public static class UploadFiles {
        Map<String, UploadFile> documents = Maps.newHashMap();
        Map<String, UploadFile> invalid = Maps.newHashMap();
        Pagination pagination;

        public UploadFiles(String directory, String id, JsonNode uploadFilesNode) {
            this.pagination = new Pagination(uploadFilesNode.get("pagination"));
            val folder = format("%s/%s", directory, id);
            val data = uploadFilesNode.get("data");
            data.forEach(item -> {
                val uploadFile = new UploadFile(item, folder);
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
        String destination;

        public UploadFile(JsonNode uploadFileNode, String folder) {
            this.path = uploadFileNode.get("path").asText();
            this.name = path.replace(format("/%s/", folder), "");
            this.id = name.replaceAll("[^\\w?]", "-");

            this.time = getLong(uploadFileNode, "time");
            this.physicalLocation = getString(uploadFileNode,"physicalLocation");
            this.format = getString(uploadFileNode, "format");
            this.mediatype = getString(uploadFileNode, "mediatype");
            this.bytes = getLong(uploadFileNode, "bytes");
            this.hash = getString(uploadFileNode, "hash");
            this.type =  getString(uploadFileNode, "status");
            this.destination = getString(uploadFileNode, "destination");
        }

        private String getString(JsonNode node, String fieldName) {
            if (node.has(fieldName)) {
                return node.get(fieldName).asText();
            } else {
                return "";
            }
        }

        private Long getLong(JsonNode node, String fieldName) {
            if (node.has(fieldName)) {
                return node.get(fieldName).asLong();
            } else {
                return null;
            }
        }
    }

    @Value
    public static class Pagination {
        int page;
        int size;
        int total;

        public Pagination(JsonNode paginationNode) {
            this.page = paginationNode.get("page").asInt();
            this.size = paginationNode.get("size").asInt();
            this.total = paginationNode.get("total").asInt();
        }
    }
}