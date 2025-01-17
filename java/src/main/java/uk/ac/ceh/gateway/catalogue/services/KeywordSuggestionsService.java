package uk.ac.ceh.gateway.catalogue.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import org.springframework.web.server.ResponseStatusException;
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
    private static final double VARIABLE_CONFIDENCE = 0.2;
    private RestClient restClient;

    public record KeywordsSuggestion(String name, double confidence, String matched_url) { }
    public record VariablesSuggestion(String name, String standardName, String longName, String units) { }
    record KeywordsResponse(List<KeywordsSuggestion> summary) { }
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

    public List<KeywordsSuggestion> getKeywordsSuggestions(String file) {
        List<Map<String, Object>> errorList = new ArrayList<>();

        List<KeywordsSuggestion> keywords = getKeywords(restClient, file, errorList)
            .flatMap(kw -> Optional.ofNullable(kw.summary()))
            .orElseGet(Collections::emptyList);

        List<KeywordsSuggestion> variables = getVariables(restClient, file, errorList)
            .flatMap(r -> Optional.ofNullable(r.summary()))
            .flatMap(s -> Optional.ofNullable(s.variables()))
            .map(vars ->
                vars.keySet().stream()
                    .map(varName -> new KeywordsSuggestion(varName, VARIABLE_CONFIDENCE, null))
                    .toList()
            )
            .orElseGet(Collections::emptyList);

        if (keywords.isEmpty() && variables.isEmpty()) {
            if (!errorList.isEmpty()) {
                int statusCode = (int) errorList.get(errorList.size() - 1).get("statusCode");
                StringJoiner statusTxt = new StringJoiner(", ");
                for (Map<String, Object> status : errorList) {
                    statusTxt.add((String) status.get("statusTxt"));
                }
                throw new ResponseStatusException(HttpStatus.valueOf(statusCode), statusTxt.toString());
            }
        }
        return Stream.concat(keywords.stream(), variables.stream())
            .sorted(Comparator.comparing(KeywordsSuggestion::confidence).reversed())
            .toList();
    }

    public List<VariablesSuggestion> getVariablesSuggestions(String file) {
        List<Map<String, Object>> errorList = new ArrayList<>();

        Optional<VariablesResponse> variablesResponse = getVariables(restClient, file, errorList);
        if (!errorList.isEmpty()) {
            int statusCode = (int) errorList.get(0).get("statusCode");
            String statusTxt = (String) errorList.get(0).get("statusTxt");
            throw new ResponseStatusException(HttpStatus.valueOf(statusCode), statusTxt);
        }

        return variablesResponse
            .flatMap(r -> Optional.ofNullable(r.summary()))
            .flatMap(s -> Optional.ofNullable(s.variables()))
            .map(vars ->
                vars.keySet().stream()
                    .map(varName -> {
                        ArrayList<Object> dataArray = (ArrayList) vars.get(varName);
                        Map<String, Object> dataMap = (Map) dataArray.get(0);
                        String standardName = dataMap.containsKey("standard_name")? (String) dataMap.get("standard_name"): "";
                        String longName = dataMap.containsKey("long_name")? (String) dataMap.get("long_name"): "";
                        String units = dataMap.containsKey("units")? (String) dataMap.get("units"): "";
                        return new VariablesSuggestion(varName, standardName, longName, units);
                    })
                    .toList()
            )
            .orElseGet(Collections::emptyList);
    }

    private Optional<KeywordsResponse> getKeywords(RestClient restClient, String file, List<Map<String, Object>> errorList) {
        return Optional.ofNullable(
            restClient
                .get()
                .uri("/{file}/keywords", file)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    int code = response.getStatusCode().value();
                    if (code == 404) code = 422;
                    errorList.add(Map.of("statusCode", code, "statusTxt", response.getStatusText()));
                })
                .toEntity(KeywordsResponse.class)
                .getBody()
        );
    }

    private Optional<VariablesResponse> getVariables(RestClient restClient, String file, List<Map<String, Object>> errorList) {
        return Optional.ofNullable(
            restClient
                .get()
                .uri("/{file}/variables", file)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    int code = response.getStatusCode().value();
                    if (code == 404) code = 422;
                    errorList.add(Map.of("statusCode", code, "statusTxt", response.getStatusText()));
                })
                .toEntity(VariablesResponse.class)
                .getBody()
        );
    }
}
