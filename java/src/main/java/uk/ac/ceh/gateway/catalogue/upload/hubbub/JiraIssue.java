package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.val;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssue {
    private String key;
    private Map<String, Object> fields;

    @SuppressWarnings("unchecked")
    public String getStatus() {
        if (fields.containsKey("status")) {
            val status = (Map<String, Object>) fields.get("status");
            val name = (String) status.get("name");
            return name.toLowerCase();
        } else {
            return "";
        }
    }

    public boolean isOpen() {
        return getStatus().equals("open");
    }

    public boolean isScheduled() {
        return getStatus().equals("scheduled");
    }

    public boolean isInProgress() {
        return getStatus().equals("in progress");
    }
}
