package uk.ac.ceh.gateway.catalogue.people;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("OrcidRegistry")
class OrcidRegistryTest {

    private static final String PERSON = """
        {"name": {"given-names": {"value": "Clare"}, "family-name": {"value": "Rowland"}, "credit-name": null, "visibility": "public"},
         "other-names": {"other-name": [{"content": "C. S. Rowland"}, {"content": ""}]},
         "emails": {"email": []}}
        """;

    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    @DisplayName("reads whose an iD is from the public registry, anonymously by default")
    void readsPerson() {
        server.expect(requestTo("https://pub.orcid.org/v3.0/0000-0002-0459-506X/person"))
            .andExpect(header("Accept", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(headerDoesNotExist("Authorization"))
            .andRespond(withSuccess(PERSON, MediaType.APPLICATION_JSON));

        var person = new OrcidRegistry(restTemplate, "https://pub.orcid.org/v3.0/", "").person("0000-0002-0459-506X");

        assertThat(person, equalTo(Optional.of(new OrcidPerson(
            "https://orcid.org/0000-0002-0459-506X", "Clare", "Rowland", "", List.of("C. S. Rowland")))));
        server.verify();
    }

    @Test
    @DisplayName("names nobody when the person keeps their name private")
    void privateName() {
        server.expect(requestTo("https://pub.orcid.org/v3.0/0000-0002-1825-0097/person"))
            .andRespond(withSuccess("{\"name\": null}", MediaType.APPLICATION_JSON));

        var person = new OrcidRegistry(restTemplate, "https://pub.orcid.org/v3.0", "").person("0000-0002-1825-0097");

        assertThat(person, equalTo(Optional.of(new OrcidPerson("https://orcid.org/0000-0002-1825-0097", "", "", "", List.of()))));
    }

    @Test
    @DisplayName("sends a token when one is configured")
    void sendsToken() {
        server.expect(requestTo("https://pub.orcid.org/v3.0/0000-0002-0459-506X/person"))
            .andExpect(header("Authorization", "Bearer secret"))
            .andRespond(withSuccess(PERSON, MediaType.APPLICATION_JSON));

        new OrcidRegistry(restTemplate, "https://pub.orcid.org/v3.0/", "secret").person("0000-0002-0459-506X");

        server.verify();
    }

    @Test
    @DisplayName("is empty for an iD ORCID never issued")
    void notIssued() {
        server.expect(requestTo("https://pub.orcid.org/v3.0/0000-0002-1825-0097/person"))
            .andRespond(withResourceNotFound());

        assertThat(new OrcidRegistry(restTemplate, "https://pub.orcid.org/v3.0/", "").person("0000-0002-1825-0097"), equalTo(Optional.empty()));
    }

    @Test
    @DisplayName("says ORCID couldn't be asked, rather than that the iD doesn't exist, when ORCID fails")
    void unavailable() {
        server.expect(requestTo("https://pub.orcid.org/v3.0/0000-0002-1825-0097/person"))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        var registry = new OrcidRegistry(restTemplate, "https://pub.orcid.org/v3.0/", "");

        assertThrows(OrcidRegistry.OrcidUnavailableException.class, () -> registry.person("0000-0002-1825-0097"));
    }

    @Test
    @DisplayName("never builds a URL from anything but a valid bare iD")
    void refusesOtherText() {
        var registry = new OrcidRegistry(restTemplate, "https://pub.orcid.org/v3.0/", "");

        assertThrows(IllegalArgumentException.class, () -> registry.person("../0000-0002-1825-0097"));
        assertThrows(IllegalArgumentException.class, () -> registry.person("https://orcid.org/0000-0002-1825-0097"));
        assertThrows(IllegalArgumentException.class, () -> registry.person("0000-0002-1825-0098"));
        server.verify();
    }
}
