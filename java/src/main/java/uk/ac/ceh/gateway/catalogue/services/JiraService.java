package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;
import javax.ws.rs.core.MediaType;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.model.JiraIssue;
import uk.ac.ceh.gateway.catalogue.model.JiraIssueCreate;
import uk.ac.ceh.gateway.catalogue.model.JiraSearchResults;

@AllArgsConstructor
public class JiraService {
    private final WebResource resource;

    public JiraIssueCreate create (String project, String issueTypeName, String summary) {
        String path = "issue";
        String input = "{\"fields\": {\"project\": {\"key\": \"EIDCHELP\"},\"issuetype\": {\"name\": \"Job\"},\"summary\": \"my summary\"}}";
        return resource
            .path(path)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(JiraIssueCreate.class, input);
    }

    // components:10771
    // labels:guid
    // issuetype:79 <- job    
    // summary:summary 1 <- title
    // description: <- some sort of summary?

    // customfield_11950:depositor name
    // customfield_11951:depositor email
    // customfield_11952:contact details
    // customfield_11953:project name
    // customfield_11955:planning documentation
    // customfield_11956:other planning documentation
    // customfield_11957:yes
    // customfield_11958:yes
    // customfield_11961:doi1,doi2,doi3
    // customfield_11962:scientific domain
    // customfield_11963:other scientific domain
    // customfield_11968:yes
    // customfield_11970:related data sets

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