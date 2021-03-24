package uk.ac.ceh.gateway.catalogue.services;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.model.JiraIssue;
import uk.ac.ceh.gateway.catalogue.model.JiraSearchResults;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JiraServiceTest {

    @Mock
    private WebResource resource;

    @Mock
    private WebResource.Builder builder;

    @Mock
    private ClientResponse response;

    @InjectMocks
    private JiraService jiraService;

    private JiraSearchResults jiraSearchResults;

    @BeforeEach
    public void before() {
        jiraSearchResults = new JiraSearchResults();
        JiraIssue issue = new JiraIssue();
        issue.setKey("key");
        val issues = new ArrayList<JiraIssue>();
        issues.add(issue);
        jiraSearchResults.setIssues(issues);
    }

    @Test
    public void addingACommentToIssueWithKey() {
        // Given
        doReturn(resource).when(resource).path(anyString());
        doReturn(builder).when(resource).accept(any(MediaType.class));
        doReturn(builder).when(builder).type(any(MediaType.class));

        // When
        jiraService.comment("key", "comment");

        // Then
        verify(resource).path(eq("issue/key"));
        verify(resource).accept(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).type(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).put(eq("{\"update\":{\"comment\":[{\"add\":{\"body\":\"comment\"}}]}}"));
    }

    @Test
    public void transitioningIssueWithKeyToTransitionId() {
        // Given
        doReturn(resource).when(resource).path(anyString());
        doReturn(builder).when(resource).accept(any(MediaType.class));
        doReturn(builder).when(builder).type(any(MediaType.class));

        // When
        jiraService.transition("key", "id");

        // Then
        verify(resource).path(eq("issue/key/transitions"));
        verify(resource).accept(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).type(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).post(eq("{\"transition\":{\"id\":\"id\"}}"));
    }

    @Test
    public void searchingUsingJql() {
        // Given
        doReturn(resource).when(resource).path(anyString());
        doReturn(resource).when(resource).queryParam(anyString(),anyString());
        doReturn(builder).when(resource).accept(any(MediaType.class));
        doReturn(response).when(builder).get(ClientResponse.class);
        doReturn(jiraSearchResults).when(response).getEntity(JiraSearchResults.class);

        // When
        List<JiraIssue> actual = jiraService.search("jql-query");

        // Then
        verify(resource).path(eq("search"));
        verify(resource).queryParam(eq("jql"), eq("jql-query"));
        verify(resource).accept(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).get(eq(ClientResponse.class));
        verify(response).getEntity(eq(JiraSearchResults.class));
        assertThat(actual, equalTo(jiraSearchResults.getIssues()));
    }
}
