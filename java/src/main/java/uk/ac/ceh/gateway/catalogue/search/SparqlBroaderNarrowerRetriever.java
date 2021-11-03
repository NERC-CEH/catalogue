package uk.ac.ceh.gateway.catalogue.search;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.vocabularies.Keyword;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Profile("search:enhanced")
@Service
public class SparqlBroaderNarrowerRetriever  implements BroaderNarrowerRetriever {
    private final RestTemplate restTemplate;
    private final String sparqlEndpoint;
    private final List<KeywordVocabulary> keywordVocabularies;

    public SparqlBroaderNarrowerRetriever(
        @Qualifier("sparql") RestTemplate restTemplate,
        @Value("${sparql.endpoint}") String sparqlEndpoint,
        List<KeywordVocabulary> keywordVocabularies
    ) {
        this.restTemplate = restTemplate;
        this.sparqlEndpoint = sparqlEndpoint;
        this.keywordVocabularies = keywordVocabularies;
    }

    @Override
    public List<Link> retrieve(Keyword keyword) {
        val graph = keywordVocabularies.stream()
            .filter(keywordVocabulary -> keywordVocabulary.getId().equals(keyword.getVocabId()))
            .map(KeywordVocabulary::getGraph)
            .findFirst()
            .orElseThrow(() -> new SearchException("No graph for: " + keyword.getUrl()));

        // create sparql query
        val query = format("""
            PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
            SELECT ?label
            FROM <%1$s>
            WHERE {
                {
                    ?b skos:broader <%2$s> ;
                        skos:prefLabel ?label .
                }
                UNION
                {
                    <%2$s> skos:broader ?b .
                    ?b skos:prefLabel ?label .
                }
            }
            """, graph, keyword.getUrl());

        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        val requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("query", query);

        val formEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, headers);

        val response = restTemplate.exchange(
            sparqlEndpoint + "?format=json-simple",
            HttpMethod.POST,
            formEntity,
            JsonNode.class
        );
        val resultsNode = Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new SearchException("Cannot get response body"));
        // convert results to a list of links
        return Collections.emptyList();
    }
}
