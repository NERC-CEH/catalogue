package uk.ac.ceh.gateway.catalogue.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import uk.ac.ceh.gateway.catalogue.util.Headers;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
@Profile("keyword-suggestions")
public class KeywordSuggestionsService {
    // The confidence to tag a variable name extracted from the data
    // files.  For now, it's completely plucked from thin air to give
    // variables a reasonably high confidence as compared to keywords
    // extracted from supporting documents by text mining.
    private static final double VARIABLE_CONFIDENCE = 0.75;
    private RestClient restClient;

    public record Suggestion(String name, double confidence, String matched_url) { }
    record KeywordsResponse(List<Suggestion> summary) { }
    record VariablesResponse(VariablesSummary summary) { }
    record VariablesSummary(Map<String, Object> variables) { }

    public KeywordSuggestionsService(
        @Qualifier("normal") RestTemplate template,
        @Value("${legilo.url}") String legiloUrl,
        @Value("${legilo.user}") String legiloUser,
        @Value("${legilo.password}") String legiloPassword
    ) {
        val authHeaders = Headers.withBasicAuth(legiloUser, legiloPassword);
        this.restClient = RestClient.builder(template)
            .baseUrl(legiloUrl)
            .defaultHeaders(headers -> headers.addAll(authHeaders))
            .build();
        log.info("Creating");
    }

    public List<Suggestion> getSuggestions(String file) {
        List<Suggestion> keywordSuggestions = Optional.ofNullable(
            restClient
                .get()
                .uri("/{file}/keywords", file)
                .retrieve()
                .toEntity(KeywordsResponse.class)
                .getBody()
        )
            .flatMap(kw -> Optional.ofNullable(kw.summary()))
            .orElseGet(Collections::emptyList);

        List<Suggestion> variables = Optional.ofNullable(
            restClient
                .get()
                .uri("/{file}/variables", file)
                .retrieve()
                .toEntity(VariablesResponse.class)
                .getBody()
        )
            .flatMap(r -> Optional.ofNullable(r.summary()))
            .flatMap(s -> Optional.ofNullable(s.variables()))
            .map(vars ->
                vars.keySet().stream()
                    .map(varName -> new Suggestion(varName, VARIABLE_CONFIDENCE, null))
                    .toList()
            )
            .orElseGet(Collections::emptyList);

        return Stream.concat(keywordSuggestions.stream(), variables.stream()).toList();
    }
}
