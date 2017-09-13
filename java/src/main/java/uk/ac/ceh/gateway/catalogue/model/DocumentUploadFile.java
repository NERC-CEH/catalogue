package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import lombok.Data;

@Data
public class DocumentUploadFile {
    private String name;
    private String path;
    private String format;
    private String mediatype;
    private String encoding;
    private long bytes;
    private String hash;
    private List<String> comments = Lists.newArrayList();
    private String type = "DATA";

    public void addComment(String comment) {
        comments.add(comment);
    }

    @JsonIgnore
    public String getCommentsAsString() {
        return comments.stream().collect(Collectors.joining("\n"));
    }

    @JsonIgnore
    public String getLatestComment() {
        return comments.get(comments.size() - 1);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setType(DocumentUpload.Type type) {
        this.type = type.toString();
    }
}
