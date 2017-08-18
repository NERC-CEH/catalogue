package uk.ac.ceh.gateway.catalogue.services;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.model.JiraIssue;
import uk.ac.ceh.gateway.catalogue.model.JiraSearchResults;

@Slf4j
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

    public void getIssue (@NonNull String guid) {
        guid = "b902e25a-ffec-446f-a270-03cc2501fe1d";
        String key = getKey(guid);
        log.error("This key is {}", key);
        key = "EIDCHELP-19181";
        comment(key, "The documents have been uploaded to dropbox and have been finialized by the user");
        scheduleStart(key);
    }
}