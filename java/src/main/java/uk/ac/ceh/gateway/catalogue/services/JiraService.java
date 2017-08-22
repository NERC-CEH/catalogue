package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.model.JiraIssue;
import uk.ac.ceh.gateway.catalogue.model.JiraSearchResults;

@AllArgsConstructor
public class JiraService {
    private final WebResource resource;

    private WebResource.Builder jira (String path) {
        return resource
            .path(path)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE);
    }

    private ClientResponse comment (String key, String comment) {
        String path = String.format("issue/%s", key);
        String input = String.format("{\"update\":{\"comment\":[{\"add\":{\"body\":\"%s\"}}]}}", comment);

        return jira(path).put(ClientResponse.class, input);
    }

    private ClientResponse scheduleStart (String key) {
        String path = String.format("issue/%s", key);
        String input = "{\"update\":{\"transition\":{\"id\":\"751\"}}";

        return jira(path).post(ClientResponse.class, input);
    }

    private String getKey (String guid) {
        String jql = String.format("project=eidchelp and status=scheduled and component=\"data transfer\" and labels=%s", guid);
        ClientResponse response = resource
            .path("search")
            .queryParam("jql", jql)        
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .get(ClientResponse.class);
        JiraSearchResults results = response.getEntity(JiraSearchResults.class);
        JiraIssue issue = results.getIssues().get(0);
        return issue.getKey();
    }
    
    public List<JiraIssue> getIssues (String guid) {
        String jql = "project=eidchelp and component='data transfer' and labels=%s";

        ClientResponse response = resource
            .path("search")
            .queryParam("jql", String.format(jql, guid))
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .get(ClientResponse.class);
        JiraSearchResults results = response.getEntity(JiraSearchResults.class);
        return results.getIssues();
    }
}