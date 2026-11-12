package uk.ac.ceh.gateway.catalogue.people;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

/**
 * Reads a person's name from ORCID's public registry, so the editor can show whose an ORCID iD is
 * beside the name typed for them. ORCID allows iDs a person hasn't authenticated only where each is
 * "checked for existence in the ORCID registry" and against what we hold (dri-one #534); this is that
 * check.
 *
 * <p>Anonymous by default, which ORCID allows 25,000 reads a day from one address. A public or member
 * API token in {@code orcid.token} is sent if set. Free public API credentials belong to one person's
 * ORCID account and can't be handed on, which is why none is required.
 */
@Slf4j
@Service
public class OrcidRegistry {
    public static final String ORCID_PERSON_CACHE = "orcid-person";

    private final RestTemplate restTemplate;
    private final String api;
    private final String token;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public OrcidRegistry(
        @Qualifier("authorities") RestTemplate restTemplate,
        @Value("${orcid.api:https://pub.orcid.org/v3.0/}") String api,
        @Value("${orcid.token:}") String token
    ) {
        this.restTemplate = restTemplate;
        this.api = api.endsWith("/") ? api : api + "/";
        this.token = token;
    }

    /**
     * The person an iD belongs to, or empty for one ORCID has never issued. Both are kept a day.
     *
     * @param id a bare iD whose check character is right ({@link Orcid#id}), as the URL is built from it
     * @throws OrcidUnavailableException when ORCID can't be asked, which is not kept, so the next look asks again
     */
    @Cacheable(cacheNames = ORCID_PERSON_CACHE, key = "#id")
    public Optional<OrcidPerson> person(String id) {
        if (Orcid.id(id).filter(id::equals).isEmpty()) {
            throw new IllegalArgumentException("Not an ORCID iD: " + id);
        }
        var headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (!token.isBlank()) headers.setBearerAuth(token);
        try {
            var body = restTemplate.exchange(api + id + "/person", HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
            return Optional.of(read(id, mapper.readTree(body == null ? "{}" : body)));
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        } catch (RestClientException | tools.jackson.core.JacksonException ex) {
            log.warn("Couldn't read {} from ORCID: {}", id, ex.getMessage());
            throw new OrcidUnavailableException(id, ex);
        }
    }

    private static OrcidPerson read(String id, JsonNode person) {
        var name = person.path("name");
        var otherNames = person.path("other-names").path("other-name").valueStream()
            .map(other -> other.path("content").asString(""))
            .filter(s -> !s.isBlank())
            .toList();
        return new OrcidPerson(
            Orcid.uri(id),
            name.path("given-names").path("value").asString(""),
            name.path("family-name").path("value").asString(""),
            name.path("credit-name").path("value").asString(""),
            otherNames
        );
    }

    public static class OrcidUnavailableException extends RuntimeException {
        public OrcidUnavailableException(String id, Throwable cause) {
            super("ORCID couldn't be asked about " + id, cause);
        }
    }
}
