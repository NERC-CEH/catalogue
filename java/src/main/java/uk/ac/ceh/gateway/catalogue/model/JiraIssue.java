package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssue {
    private String key;
    private Map<String, Object> fields;

    public String getStatus () {
        @SuppressWarnings("unchecked")
        Map<String, Object> status = (Map<String, Object>) fields.get("status");
        String name = (String) status.get("name");
        return name.toLowerCase();
    };
}
