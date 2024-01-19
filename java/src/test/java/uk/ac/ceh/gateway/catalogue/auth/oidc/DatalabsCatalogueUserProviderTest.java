package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

class DatalabsCatalogueUserProviderTest {

    private CatalogueUserProvider provider;
    private MockRestServiceServer mockServer;

    private final String usersEndpointEncoded = "https://example.com/api?query=%7Busers%7BuserId,name%7D%7D";
    private final String subject = "auth0|af53b2";
    private final String jwtToken = "token";

    @BeforeEach
    @SneakyThrows
    void init() {
        val restTemplate = new RestTemplate();
        val usersEndpoint = "https://example.com/api?query={users{userId,name}}";
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        provider = new DatalabsCatalogueUserProvider(restTemplate, usersEndpoint);
    }

    @Test
    @SneakyThrows
    void successfullyGetCatalogueUserFromDatalabs() {
        //given
        val response = IOUtils.toByteArray(
            Objects.requireNonNull(getClass().getResource("users.json"))
        );
        val expected = new CatalogueUser();
        expected
            .setUsername("foo@example.com")
            .setEmail("foo@example.com");

        mockServer.expect(requestTo(usersEndpointEncoded))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "bearer " + jwtToken))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        //when
        val actual = provider.provide(subject, jwtToken);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @SneakyThrows
    void unknownSubject() {
        //given
        val response = IOUtils.toByteArray(
            Objects.requireNonNull(getClass().getResource("users.json"))
        );
        mockServer.expect(requestTo(usersEndpointEncoded))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        //when
        assertThrows(AuthenticationException.class, () ->
            provider.provide("unknown", jwtToken)
        );
    }

    @Test
    void invalidCredentials() {
        //given
        val invalidToken = "sflcsdjvmxfsdlkadad";

        mockServer.expect(requestTo(usersEndpointEncoded))
            .andRespond(withUnauthorizedRequest());

        //when
        assertThrows(HttpClientErrorException.Unauthorized.class, () ->
            provider.provide(subject, invalidToken)
        );
    }
}
