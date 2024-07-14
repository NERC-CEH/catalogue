package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.ac.ceh.gateway.catalogue.util.Headers.withBasicAuth;

@Service
@Profile({"upload:hubbub", "service-agreement"})
@Slf4j
@ToString(exclude = {"restTemplate", "password"})
public class JiraService {
    private final RestTemplate restTemplate;
    private final String jiraEndpoint;
    private final String jqlTemplate;
    private final String username;
    private final String password;

    public JiraService(
        @Qualifier("normal") RestTemplate restTemplate,
        @Value("${jira.username}") String jiraUsername,
        @Value("${jira.password}") String jiraPassword,
        @Value("${jira.address}") String jiraAddress,
        @Value("${jira.jqlTemplate}") String jqlTemplate
    ) {
        this.jiraEndpoint = jiraAddress;
        this.jqlTemplate = jqlTemplate;
        this.restTemplate = restTemplate;
        this.username = jiraUsername;
        this.password = jiraPassword;
        log.info("Creating {}", this);
    }

    public void comment(String key, String comment) throws RestClientResponseException {
        log.info("Commenting on {}: {}", key, comment);
        val url = UriComponentsBuilder
                .fromHttpUrl(jiraEndpoint)
                .path("issue/{key}")
                .buildAndExpand(key)
                .toUri();
        val requestBody = String.format("{\"update\":{\"comment\":[{\"add\":{\"body\":\"%s\"}}]}}", comment);
        val headers = withBasicAuth(username, password);
        headers.setContentType(APPLICATION_JSON);
        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(requestBody, headers),
                    Void.class
            );
        }catch (RestClientResponseException ex) {
            log.error(
                    "Error communicating with supplied URL: (statusCode={}, status={}, headers={}, body={})",
                    ex.getStatusCode().value(),
                    ex.getStatusText(),
                    ex.getResponseHeaders(),
                    ex.getResponseBodyAsString()
            );
            throw ex;
        }
    }

    public void transition (String key, String id) {
        log.info("Transitioning {} for data transfer {}", key, id);
        val url = UriComponentsBuilder
            .fromHttpUrl(jiraEndpoint)
            .path("issue/{key}/transitions")
            .buildAndExpand(key)
            .toUri();
        val transitionRequest = String.format("{\"transition\":{\"id\":\"%s\"}}", id);;
        log.debug("Transition url: {}", url);
        log.debug("Transition request body: {}", transitionRequest);
        val headers = withBasicAuth(username, password);
        headers.setContentType(APPLICATION_JSON);
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            new HttpEntity<>(transitionRequest, headers),
            Void.class
        );
    }

    public Optional<JiraIssue> retrieveDataTransferIssue(String id) {
        log.info("Retrieving data transfer issues for {}", id);
        val url = UriComponentsBuilder
            .fromHttpUrl(jiraEndpoint)
            .path("search")
            .queryParam("jql", jqlTemplate)
            .buildAndExpand(id)
            .toUri();
        log.debug("retrieval url: {}", url);
        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(withBasicAuth(username, password)),
            JiraSearchResults.class
        ).getBody();
        val issues = Optional.ofNullable(response.getIssues())
            .orElse(Collections.emptyList());
        log.debug("For {} found {} issues", id, issues.size());
        if (issues.size() != 1) {
            issues.forEach(issue -> log.error("Jira issue: {}", issue.getKey()));
            throw new NonUniqueJiraIssue();
        }
        return issues.stream().findFirst();
    }

    @ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "Can not finish, contact admin to resolve issue clash"
    )
    static class NonUniqueJiraIssue extends RuntimeException {
        static final long serialVersionUID = 1L;
    }
}
