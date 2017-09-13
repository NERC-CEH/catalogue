package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.apache.jena.ext.com.google.common.collect.Maps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import lombok.Data;
import lombok.val;

@Data
public class DocumentUpload {

    private final String title;
    private final String type;
    private final String guid;
    private final String path;
    private final ConcurrentMap<String, DocumentUploadFile> meta;
    private final ConcurrentMap<String, DocumentUploadFile> data;
    private final ConcurrentMap<String, DocumentUploadFile> invalid;

    public DocumentUpload (String title, String type, String guid, String path) {
        this.title = title;
        this.type = type;
        this.guid = guid;
        this.path = path;
        this.meta = Maps.newConcurrentMap();
        this.data = Maps.newConcurrentMap();
        this.invalid = Maps.newConcurrentMap();
    }

    @JsonCreator
    public DocumentUpload (
        @JsonProperty("title") String title,
        @JsonProperty("type") String type,
        @JsonProperty("guid") String guid,
        @JsonProperty("path") String path,
        @JsonProperty("meta") ConcurrentMap<String, DocumentUploadFile> meta,
        @JsonProperty("data") ConcurrentMap<String, DocumentUploadFile> data,
        @JsonProperty("invalid") ConcurrentMap<String, DocumentUploadFile> invalid
    ) {
        this.title = title;
        this.type = type;
        this.guid = guid;
        this.path = path;
        this.meta = meta;
        this.data = data;
        this.invalid = invalid;
    }

    public List<DocumentUploadFile> getFiles () {
        val files = Lists.newArrayList(meta.values());
        files.addAll(data.values());
        files.sort((left, right) -> left.getName().compareTo(right.getName()));
        return files;
    }

    @JsonIgnore
    public ConcurrentMap<String, DocumentUploadFile> getFiles(String type) {
        return getFiles(Type.valueOf(type));
    }

    @JsonIgnore
    public ConcurrentMap<String, DocumentUploadFile> getFiles(Type type) {
        if (type == Type.META) return meta;
        else if (type == Type.DATA) return data;
        return invalid;
    }

    public enum Type {
        META, DATA, INVALID_HASH, MISSING_FILE, UNKNOWN_FILE
    }
}