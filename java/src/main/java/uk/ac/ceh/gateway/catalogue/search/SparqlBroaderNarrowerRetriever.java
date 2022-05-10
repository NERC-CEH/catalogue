package uk.ac.ceh.gateway.catalogue.search;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.vocabularies.Keyword;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.String.format;

@Slf4j
@Profile("search:enhanced")
@Service
public class SparqlBroaderNarrowerRetriever  implements BroaderNarrowerRetriever {
    private final RestTemplate restTemplate;
    private final String sparqlEndpoint;
    private final List<KeywordVocabulary> keywordVocabularies;

    public SparqlBroaderNarrowerRetriever(
        @Qualifier("sparql") RestTemplate restTemplate,
        @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint,
        List<KeywordVocabulary> keywordVocabularies
    ) {
        this.restTemplate = restTemplate;
        this.sparqlEndpoint = sparqlEndpoint;
        this.keywordVocabularies = keywordVocabularies;
    }

    @Override
    public List<Keyword> retrieve(Keyword keyword) {
        val requestUrl = prepareRequestUrl(keyword);

        val response = restTemplate.getForEntity(
            requestUrl,
            JsonNode.class
        );
        val resultsNode = Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new SearchException("Cannot get response body"));

        if (resultsNode.isArray()) {
            log.debug("Retrieved {} keywords", resultsNode.size());
            return StreamSupport.stream(resultsNode.spliterator(), false)
                .map(node -> {
                    val url = node.get("uri").asText();
                    val label = node.get("label").asText();
                    val vocabId = keyword.getVocabId();
                    return new Keyword(label, vocabId, url);
                })
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private URI prepareRequestUrl(Keyword keyword) {
        val graph = keywordVocabularies.stream()
            .filter(keywordVocabulary -> keywordVocabulary.getId().equals(keyword.getVocabId()))
            .map(KeywordVocabulary::getGraph)
            .findFirst()
            .orElseThrow(() -> new SearchException("No graph for: " + keyword.getUrl()));

        // create sparql query
        val query = format("""
            PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
            SELECT ?uri ?label
            FROM <%1$s>
            WHERE {
                {
                    ?uri skos:broader <%2$s> ;
                        skos:prefLabel ?label .
                }
                UNION
                {
                    <%2$s> skos:broader ?uri .
                    ?uri skos:prefLabel ?label .
                }
            }
            """, graph, keyword.getUrl());
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        val url = format("%s?query=%s&format=json", sparqlEndpoint, encodedQuery);
        log.debug("SPARQL url: {}", url);
        return URI.create(url);
    }
}
