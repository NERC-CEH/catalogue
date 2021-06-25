package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraSearchResults {
    private int total;
    private List<JiraIssue> issues;

}
