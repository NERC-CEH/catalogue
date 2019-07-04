package uk.ac.ceh.gateway.catalogue.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.JiraIssue;
import uk.ac.ceh.gateway.catalogue.model.JiraIssueBuilder;
import uk.ac.ceh.gateway.catalogue.model.JiraIssueCreate;
import uk.ac.ceh.gateway.catalogue.model.JiraSearchResults;

@RunWith(MockitoJUnitRunner.class)
public class JiraServiceTest {

    @Mock
    private WebResource resource;

    @Mock
    private WebResource.Builder builder;

    @Mock
    private ClientResponse response;

    @Mock
    private JiraIssueBuilder jiraIssueBuilder;

    @InjectMocks
    private JiraService jiraService;

    private JiraSearchResults jiraSearchResults;

    @Before
    public void before() {
        jiraSearchResults = new JiraSearchResults();
        JiraIssue issue = new JiraIssue();
        issue.setKey("key");
        val issues = new ArrayList<JiraIssue>();
        issues.add(issue);
        jiraSearchResults.setIssues(issues);

        doReturn(resource).when(resource).path(anyString());
        doReturn(resource).when(resource).queryParam(anyString(), anyString());

        doReturn(builder).when(resource).accept(any(MediaType.class));
        doReturn(builder).when(builder).type(any(MediaType.class));

        doReturn(response).when(builder).get(ClientResponse.class);

        doReturn(jiraSearchResults).when(response).getEntity(JiraSearchResults.class);

        doReturn("jira issue").when(jiraIssueBuilder).build();
    }

    @Test
    public void createingAnIssue() {
        val created = new JiraIssueCreate();
        created.setId("id");
        doReturn(created).when(builder).post(eq(JiraIssueCreate.class), anyString());

        val issue = jiraService.create(jiraIssueBuilder);
        verify(resource).path(eq("issue"));
        verify(resource).accept(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).type(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).post(eq(JiraIssueCreate.class), eq("jira issue"));
        assertThat(issue, equalTo(created));
    }

    @Test
    public void addingACommentToIssueWithKey() {
        jiraService.comment("key", "comment");

        verify(resource).path(eq("issue/key"));
        verify(resource).accept(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).type(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).put(eq("{\"update\":{\"comment\":[{\"add\":{\"body\":\"comment\"}}]}}"));
    }

    @Test
    public void transitioningIssueWithKeyToTransitionId() {
        jiraService.transition("key", "id");

        verify(resource).path(eq("issue/key/transitions"));
        verify(resource).accept(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).type(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).post(eq("{\"transition\":{\"id\":\"id\"}}"));
    }

    @Test
    public void searchingUsingJql() {
        List<JiraIssue> actual = jiraService.search("jql-query");

        verify(resource).path(eq("search"));
        verify(resource).queryParam(eq("jql"), eq("jql-query"));
        verify(resource).accept(eq(MediaType.APPLICATION_JSON_TYPE));
        verify(builder).get(eq(ClientResponse.class));
        verify(response).getEntity(eq(JiraSearchResults.class));

        assertThat(actual, equalTo(jiraSearchResults.getIssues()));
    }
}
