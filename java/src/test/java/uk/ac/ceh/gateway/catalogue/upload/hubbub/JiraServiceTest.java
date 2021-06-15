package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class JiraServiceTest {
    private MockRestServiceServer mockServer;
    private final String id = "b85f74c6-767b-468c-b6c9-3c14fd92f4fd";
    private final String key = "TEST-123";

    private JiraService jiraService;

    @BeforeEach
    public void setup() {
        val restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        jiraService = new JiraService(
            restTemplate,
            "jira",
            "password",
            "https://example.com/api/",
            "issue={id}"
        );
    }

    @Test
    @SneakyThrows
    void getDataTransferIssue() {
        //given
        val success = IOUtils.toByteArray(getClass().getResource("issues.json"));
        mockServer
            .expect(requestTo(startsWith("https://example.com/api/search")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(queryParam("jql", "issue=" + id))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic amlyYTpwYXNzd29yZA=="))
            .andRespond(withSuccess(success, APPLICATION_JSON));

        //when
        val dataTransfer = jiraService.retrieveDataTransferIssue(id).get();

        //then
        assertThat(dataTransfer.getKey(), equalTo(key));
        assertThat(dataTransfer.getStatus(), equalTo("open"));
    }

    @Test
    @SneakyThrows
    void getDataTransferIssueFindsMultipleIssues() {
        //given
        val multiple = IOUtils.toByteArray(getClass().getResource("multiple.json"));
        mockServer
            .expect(requestTo(startsWith("https://example.com/api/search")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(queryParam("jql", "issue=" + id))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic amlyYTpwYXNzd29yZA=="))
            .andRespond(withSuccess(multiple, APPLICATION_JSON));

        //when
        assertThrows(
            JiraService.NonUniqueJiraIssue.class,
            () -> jiraService.retrieveDataTransferIssue(id).get()
        );
    }

    @Test
    public void addingACommentToIssueWithKey() {
        // Given
        mockServer
            .expect(requestTo("https://example.com/api/issue/TEST-123"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic amlyYTpwYXNzd29yZA=="))
            .andRespond(withSuccess("", APPLICATION_JSON));

        // When
        jiraService.comment(key, "this is a comment");
    }

    @Test
    public void transitioningIssueWithKeyToTransitionId() {
        // Given
        mockServer
            .expect(requestTo("https://example.com/api/issue/TEST-123/transitions"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic amlyYTpwYXNzd29yZA=="))
            .andRespond(withSuccess("", APPLICATION_JSON));

        // When
        jiraService.transition(key, id);
    }
}
