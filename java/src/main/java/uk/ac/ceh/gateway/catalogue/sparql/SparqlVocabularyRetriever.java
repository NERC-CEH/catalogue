package uk.ac.ceh.gateway.catalogue.sparql;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;

@Slf4j
@AllArgsConstructor
public class SparqlVocabularyRetriever implements VocabularyRetriever {
    private final RestTemplate template;
    private final String query;

    @SneakyThrows
    public SparqlVocabularyRetriever(RestTemplate template, String sparqlEndpoint, String graph) {
        this.template = template;
        this.query = new StringBuilder(sparqlEndpoint)
            .append("?query=")
            .append(URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", "UTF-8"))
            .append(URLEncoder.encode("SELECT ?concept ?topConcept ", "UTF-8"))
            .append(URLEncoder.encode("WHERE {GRAPH <", "UTF-8"))
            .append(URLEncoder.encode(graph, "UTF-8"))
            .append(URLEncoder.encode("> {?concept skos:broader ?topConcept .}}", "UTF-8"))
            .append("&format=json-simple")
            .toString();
    }

    @Override
    @SneakyThrows
    public Multimap<String, String> retrieve() {
        Multimap<String, String> toReturn = ArrayListMultimap.create();
        try {
            ResponseEntity<SparqlVocabularyRetriever.Concept[]> responseEntity = template.getForEntity(
                URI.create(this.query),
                SparqlVocabularyRetriever.Concept[].class
            );
            Arrays.stream(responseEntity.getBody())
                .forEach(concept -> toReturn.put(concept.topConcept, concept.concept));
            log.info("Vocabulary top concepts retrieved: {}", toReturn.keySet());
        } catch (HttpStatusCodeException ex) {
            log.info("Failed to retrieve vocabulary: {} status: {}, query: {}", ex.getMessage(), ex.getStatusCode(), this.query);
        }
        return toReturn;
    }

    @Data
    @Accessors(chain = true)
    public static class Concept {
        String concept, topConcept;
    }
}
