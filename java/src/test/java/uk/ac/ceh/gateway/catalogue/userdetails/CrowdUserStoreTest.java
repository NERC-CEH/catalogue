package uk.ac.ceh.gateway.catalogue.userdetails;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class CrowdUserStoreTest {
    private UserStore<CatalogueUser> userStore;
    private MockRestServiceServer mockServer;

    private final String username = "foo";

    @BeforeEach
    void setup() {
        val restTemplate = new RestTemplate();
        userStore = new CrowdUserStore(
                "https://example.com/latest",
                "abc",
                "1234",
                restTemplate
                );
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @SneakyThrows
    void getUser() {
        //given
        val response = """
        {
            "name": "foo",
                "email": "foo@example.com"
        }
        """;
        mockServer.expect(requestTo("https://example.com/latest/user?username=foo&expand=attributes"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("authorization", "Basic YWJjOjEyMzQ="))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        //when
        val user = userStore.getUser(username);

        //then
        assertThat(user.getUsername(), equalTo(username));
        assertThat(user.getEmail(), equalTo("foo@example.com"));
    }

    @Test
    void userNotFound() {
        //given
        mockServer.expect(requestTo("https://example.com/latest/user?username=foo&expand=attributes"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        //when
        assertThrows(UnknownUserException.class, () ->
                userStore.getUser(username)
                );
    }

    @Test
    void otherError() {
        //given
        mockServer.expect(requestTo("https://example.com/latest/user?username=foo&expand=attributes"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withServerError());

        //when
        assertThrows(HttpServerErrorException.InternalServerError.class, () ->
                userStore.getUser(username)
                );
    }

    @Test
    void userExistsError() {
        //when
        assertThrows(NotImplementedException.class, () ->
                userStore.userExists(username)
                );
    }

    @Test
    void authenticationError() {
        //when
        assertThrows(NotImplementedException.class, () ->
                userStore.authenticate(username, "password")
                );
    }

}
