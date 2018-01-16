package uk.ac.ceh.gateway.catalogue.model;

import java.io.File;
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
    private final ConcurrentMap<String, DocumentUploadFile> documents;
    private final ConcurrentMap<String, DocumentUploadFile> invalid;
    
    @SuppressWarnings("unused")
    private boolean zipped;

    public DocumentUpload (String title, String type, String guid, String path) {
        this.title = title;
        this.type = type;
        this.guid = guid;
        this.path = path;
        this.documents = Maps.newConcurrentMap();
        this.invalid = Maps.newConcurrentMap();
    }

    @JsonCreator
    public DocumentUpload (
        @JsonProperty("title") String title,
        @JsonProperty("type") String type,
        @JsonProperty("guid") String guid,
        @JsonProperty("path") String path,
        @JsonProperty("documents") ConcurrentMap<String, DocumentUploadFile> documents,
        @JsonProperty("invalid") ConcurrentMap<String, DocumentUploadFile> invalid
    ) {
        this.title = title;
        this.type = type;
        this.guid = guid;
        this.path = path;
        this.documents = documents;
        this.invalid = invalid;
    }

    public List<DocumentUploadFile> getFiles () {
        val files = Lists.newArrayList(documents.values());
        files.sort((left, right) -> left.getId().compareTo(right.getId()));
        return files;
    }

    public boolean isZipped() {
        return new File(path, String.format("%s.zip", guid)).exists();
    }

    @JsonIgnore
    public ConcurrentMap<String, DocumentUploadFile> getFiles(String type) {
        return getFiles(Type.valueOf(type));
    }

    @JsonIgnore
    public ConcurrentMap<String, DocumentUploadFile> getFiles(Type type) {
        if (type == Type.DOCUMENTS) return documents;
        return invalid;
    }

    public enum Type {
        DOCUMENTS, INVALID_HASH, MISSING_FILE, UNKNOWN_FILE, INVALID
    }
}