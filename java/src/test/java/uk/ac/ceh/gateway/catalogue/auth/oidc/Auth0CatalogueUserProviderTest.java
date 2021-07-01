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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

class Auth0CatalogueUserProviderTest {

    private CatalogueUserProvider provider;

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    private final String userInfoEndpoint = "https://example.com/userinfo";
    private final String jwtToken = "token";

    @BeforeEach
    void init() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        provider = new Auth0CatalogueUserProvider(restTemplate, userInfoEndpoint);
    }

    @Test
    @SneakyThrows
    void successfullyGetCatalogueUserFromAuth0() {
        //given
        val response = IOUtils.toByteArray(getClass().getResource("userInfo.json"));
        val expected = new CatalogueUser();
        expected
            .setUsername("foo@example.com")
            .setEmail("foo@example.com");

        mockServer.expect(requestTo(userInfoEndpoint))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "bearer " + jwtToken))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        //when
        val actual = provider.provide(jwtToken);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    void invalidCredentials() {
        //given
        val invalidToken = "sflcsdjvmxfsdlkadad";

        mockServer.expect(requestTo(userInfoEndpoint))
            .andRespond(withUnauthorizedRequest());

        //when
        assertThrows(HttpClientErrorException.Unauthorized.class, () ->
            provider.provide(invalidToken)
        );

        //then

    }
}