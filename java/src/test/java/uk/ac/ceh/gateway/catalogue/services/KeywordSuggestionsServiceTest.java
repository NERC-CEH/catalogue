package uk.ac.ceh.gateway.catalogue.services;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class KeywordSuggestionsServiceTest {
    private KeywordSuggestionsService service;
    private MockRestServiceServer mockServer;

    private static final String LEGILO_URL = "http://legilo.invalid/";
    private static final String LEGILO_USERNAME = "username";
    private static final String LEGILO_PASSWORD = "password";
    private static final String FILE_ID = "360ffb95-97c9-4f76-8859-eb1a83543270";

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        service = new KeywordSuggestionsService(
            restTemplate,
            LEGILO_URL,
            LEGILO_USERNAME,
            LEGILO_PASSWORD
        );
    }

    @Test
    @SneakyThrows
    void getSuggestions() {
        //given
        String keywordsResponse = IOUtils.toString(getClass().getResource("legilo-keywords-response.json"), UTF_8);
        String variablesResponse = IOUtils.toString(getClass().getResource("legilo-variables-response.json"), UTF_8);
        mockServer
            .expect(requestTo(equalTo(LEGILO_URL + FILE_ID + "/keywords")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
            .andRespond(withSuccess(keywordsResponse, MediaType.APPLICATION_JSON));
        mockServer
            .expect(requestTo(equalTo(LEGILO_URL + FILE_ID + "/variables")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
            .andRespond(withSuccess(variablesResponse, MediaType.APPLICATION_JSON));

        //when
        List<KeywordSuggestionsService.Suggestion> suggestions = service.getSuggestions(FILE_ID);

        //then
        mockServer.verify();
        assertThat(suggestions, allOf(
            hasItem(name(is("bears"))),
            hasItem(name(is("temp"))),
            hasItem(name(is("absorbance")))
        ));
    }

    private FeatureMatcher<KeywordSuggestionsService.Suggestion, String> name(Matcher<String> matcher) {
        return new FeatureMatcher<>(matcher, "name", "name") {
            @Override
            protected String featureValueOf(KeywordSuggestionsService.Suggestion actual) {
                return actual.name();
            }
        };
    }

}
