package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static org.springframework.http.HttpMethod.GET;
import static uk.ac.ceh.gateway.catalogue.util.Headers.withBasicAuth;

@Slf4j
@Service
@Profile("upload:hubbub")
@ToString(exclude = "password")
public class HubbubService {
    private final RestTemplate restTemplate;
    private final String address;
    private final String username;
    private final String password;
    private final Set<String> guidQueryParameterEndpoints = ImmutableSet.of("/move_all", "/unzip", "/zip");

    public HubbubService(
            @Qualifier("normal") RestTemplate restTemplate,
            @Value("${hubbub.url}") String address,
            @Value("${hubbub.username}") String username,
            @Value("${hubbub.password}") String password
    ) {
        this.restTemplate = restTemplate;
        this.address = address;
        this.username = username;
        this.password = password;
        log.info("Creating {}", this);
    }

    public List<FileInfo> get(String path, int page, int size, String... status) {
        val uriBuilder = UriComponentsBuilder.fromHttpUrl(address)
                .queryParam("data", true)
                .queryParam("path", path)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("status", (Object[]) status);
        return request(uriBuilder.toUriString(), GET);
    }

    public List<FileInfo> delete(String path) {
        log.info("Deleting: {}", path);
        val uriBuilder = UriComponentsBuilder.fromHttpUrl(address)
                .path("delete")
                .queryParam("path", path);

        return request(uriBuilder.toUriString(), HttpMethod.DELETE);
    }

    public List<FileInfo> post(String endpoint, String path) {
        log.info("Posting to {}: {}", endpoint, path);
        val uriBuilder = UriComponentsBuilder.fromHttpUrl(address)
                .path(endpoint)
                .queryParam(queryParamName(endpoint), path);

        return request(uriBuilder.toUriString(), HttpMethod.POST);
    }

    public void postQuery(String endpoint, String path, String queryKey, String queryValue) {
        log.info("Post query to {}: {} with query param: {}={}", endpoint, path, queryKey, queryValue);
        val uriBuilder = UriComponentsBuilder.fromHttpUrl(address)
                .path(endpoint)
                .queryParam(queryParamName(endpoint), path)
                .queryParam(queryKey, queryValue);

        request(uriBuilder.toUriString(), HttpMethod.POST);
    }

    private List<FileInfo> request(String url, HttpMethod method) {
        log.debug("{} {}", method, url);
        try {
            val response = restTemplate.exchange(
                    url,
                    method,
                    new HttpEntity<>(withBasicAuth(username, password)),
                    JsonNode.class
            );
            log.debug("Response {}", response.getStatusCode());

            val fileInfoNodes = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new UploadException(format("No body for %s %s", method, url)))
                .at("/data");

            if (method.equals(GET)) {
                if (fileInfoNodes.isArray()) {
                    return StreamSupport.stream(fileInfoNodes.spliterator(), false)
                        .map(FileInfo::new)
                        .collect(Collectors.toList());
                } else {
                    throw new UploadException(format("No file info for %s %s", method, url));
                }
            } else {
                return Collections.emptyList();
            }
        } catch (RestClientResponseException ex) {
            log.error(
                    "Error communicating with Hubbub: (statusCode={}, status={}, headers={}, body={})",
                    ex.getRawStatusCode(),
                    ex.getStatusText(),
                    ex.getResponseHeaders(),
                    ex.getResponseBodyAsString()
            );
            throw ex;
        }
    }

    private String queryParamName(String endpoint) {
        if (guidQueryParameterEndpoints.contains(endpoint)) {
            return "guid";
        } else {
            return "path";
        }
    }
}