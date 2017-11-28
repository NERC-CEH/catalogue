package uk.ac.ceh.gateway.catalogue.services;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.model.JiraIssue;
import uk.ac.ceh.gateway.catalogue.model.JiraSearchResults;

import javax.ws.rs.core.MediaType;
import java.util.List;

@AllArgsConstructor
public class JiraService {
    private final WebResource resource;

    public void comment (String key, String comment) {
        String path = String.format("issue/%s", key);
        String input = String.format("{\"update\":{\"comment\":[{\"add\":{\"body\":\"%s\"}}]}}", comment);
        resource
            .path(path)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .put(input);
    }

    public void transition (String key, String id) {        
        String path = String.format("issue/%s/transitions", key);
        String input = String.format("{\"transition\":{\"id\":\"%s\"}}", id);
        resource
            .path(path)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(input);
    }

    public List<JiraIssue> search (String jql) {
        return resource
            .path("search")
            .queryParam("jql", jql)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .get(ClientResponse.class)
            .getEntity(JiraSearchResults.class)
            .getIssues();
    }
}