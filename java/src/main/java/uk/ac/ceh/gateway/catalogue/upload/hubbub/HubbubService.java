package uk.ac.ceh.gateway.catalogue.upload.hubbub;

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

import java.util.Set;

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
    private final Set<String> guidQueryParameterPaths = ImmutableSet.of("/move_all", "/unzip", "/zip");

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

    public HubbubResponse get(String path, Integer page, Integer size, String... status) {

        val uriBuilder = UriComponentsBuilder.fromHttpUrl(address)
                .queryParam("data", true)
                .queryParam("path", path)
                .queryParam("page", page.toString())
                .queryParam("size", size.toString())
                .queryParam("status", (Object[]) status);

        return request(uriBuilder.toUriString(), HttpMethod.GET);
    }

    public HubbubResponse get(String path, Integer page, Integer size) {
        return get(path, page, size, new String[0]);
    }

    public HubbubResponse get(String path, Integer page, String... status) {
        return get(path, page, 20, status);
    }

    public HubbubResponse get(String path) {
        return get(path, 1, 20, new String[0]);
    }

    public HubbubResponse delete(String id) {
        log.info("Deleting: {}", id);
        val uriBuilder = UriComponentsBuilder.fromHttpUrl(address)
                .path("delete")
                .queryParam("path", id);

        return request(uriBuilder.toUriString(), HttpMethod.DELETE);
    }

    public HubbubResponse post(String path, String id) {
        log.info("Posting to {}: {}", path, id);
        val uriBuilder = UriComponentsBuilder.fromHttpUrl(address)
                .path(path)
                .queryParam(queryParamName(path), id);

        return request(uriBuilder.toUriString(), HttpMethod.POST);
    }

    public HubbubResponse postQuery(String path, String id, String queryKey, String queryValue) {
        log.info("Post query to {}: {} with key: {} value: {}", path, id, queryKey, queryValue);
        val uriBuilder = UriComponentsBuilder.fromHttpUrl(address)
                .path(path)
                .queryParam(queryParamName(path), id)
                .queryParam(queryKey, queryValue);

        return request(uriBuilder.toUriString(), HttpMethod.POST);
    }

    private HubbubResponse request(String url, HttpMethod method) {
        log.debug("{} {}", method, url);
        try {
            val response = restTemplate.exchange(
                    url,
                    method,
                    new HttpEntity<>(withBasicAuth(username, password)),
                    HubbubResponse.class
            );
            log.debug("Response {}", response);
            return response.getBody();
        } catch (RestClientResponseException ex) {
            log.error(
                    "Error communicating with Hubbub: (statusCode={}, status={}, headers={}, body={})",
                    ex.getRawStatusCode(),
                    ex.getStatusText(),
                    ex.getResponseHeaders(),
                    ex.getResponseBodyAsString()
            );
            throw ex;
        } catch (Exception ex) {
            log.error("Some other error", ex);
            throw ex;
        }
    }

    private String queryParamName(String path) {
        if (guidQueryParameterPaths.contains(path)) {
            return "guid";
        } else {
            return "path";
        }
    }
}