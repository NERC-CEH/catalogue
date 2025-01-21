package uk.ac.ceh.gateway.catalogue.services;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
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
    void getKeywordsSuggestions() {
        //given
        String keywordsResponse = IOUtils.toString(getClass().getResource("legilo-keywords-response.json"), UTF_8);
        mockServer
            .expect(requestTo(equalTo(LEGILO_URL + FILE_ID + "/keywords")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
            .andRespond(withSuccess(keywordsResponse, MediaType.APPLICATION_JSON));

        //when
        List<KeywordSuggestionsService.KeywordsSuggestion> keywordsSuggestions = service.getKeywordsSuggestions(FILE_ID);

        //then
        mockServer.verify();
        assertThat(keywordsSuggestions, allOf(
            hasItem(name(is("pools"))),
            hasItem(name(is("sample collection"))),
            hasItem(name(is("absorbance")))
        ));
    }

    @Test
    @SneakyThrows
    void getSuggestionsWithException() {
        //given
        mockServer
            .expect(requestTo(equalTo(LEGILO_URL + FILE_ID + "/keywords")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
            .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        //when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getKeywordsSuggestions(FILE_ID));

        //then
        assertEquals("422 UNPROCESSABLE_ENTITY \"Unprocessable Entity\"", exception.getMessage());
    }

    @Test
    @SneakyThrows
    void getVariablesSuggestions() {
        //given
        String variablesResponse = IOUtils.toString(getClass().getResource("legilo-variables-response.json"), UTF_8);
        mockServer
            .expect(requestTo(equalTo(LEGILO_URL + FILE_ID + "/variables")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
            .andRespond(withSuccess(variablesResponse, MediaType.APPLICATION_JSON));

        //when
        List<KeywordSuggestionsService.VariablesSuggestion> variablesSuggestions = service.getVariablesSuggestions(FILE_ID);

        //then
        mockServer.verify();
        assertThat(variablesSuggestions, allOf(
            hasItem(variableName(is("bears"))),
            hasItem(variableName(is("temp"))),
            hasItem(variableName(is("Units")))
        ));
    }

    @Test
    @SneakyThrows
    void getVariablesSuggestionsWithException() {
        //given
        mockServer
            .expect(requestTo(equalTo(LEGILO_URL + FILE_ID + "/variables")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        //when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getVariablesSuggestions(FILE_ID));

        //then
        assertEquals("422 UNPROCESSABLE_ENTITY \"Not Found\"", exception.getMessage());
    }

    private FeatureMatcher<KeywordSuggestionsService.KeywordsSuggestion, String> name(Matcher<String> matcher) {
        return new FeatureMatcher<>(matcher, "name", "name") {
            @Override
            protected String featureValueOf(KeywordSuggestionsService.KeywordsSuggestion actual) {
                return actual.name();
            }
        };
    }

    private FeatureMatcher<KeywordSuggestionsService.VariablesSuggestion, String> variableName(Matcher<String> matcher) {
        return new FeatureMatcher<>(matcher, "variableName", "variableName") {
            @Override
            protected String featureValueOf(KeywordSuggestionsService.VariablesSuggestion actual) {
                return actual.name();
            }
        };
    }

}
