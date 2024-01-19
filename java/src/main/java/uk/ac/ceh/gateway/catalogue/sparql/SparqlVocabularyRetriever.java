package uk.ac.ceh.gateway.catalogue.sparql;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@ToString
public class SparqlVocabularyRetriever implements VocabularyRetriever {
    /*
     * This was used to retrieve the Natural Capital vocabulary.
     * The Natural Capital vocabulary no longer exists on the new vocab server.
     * This class needs modifying to work with multiple graphs so that it
     * can be used in the SolrIndexMetadataDocumentGenerator in place of
     * getKeywordsFilteredByUrlFragment
     */
    private final RestTemplate template;
    private final URI query;

    @SneakyThrows
    public SparqlVocabularyRetriever(RestTemplate template, String sparqlEndpoint, String graph) {
        this.template = template;
        this.query = URI.create(sparqlEndpoint +
            "?query=" +
            URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", StandardCharsets.UTF_8) +
            URLEncoder.encode("SELECT ?concept ?topConcept ", StandardCharsets.UTF_8) +
            URLEncoder.encode("WHERE {GRAPH <", StandardCharsets.UTF_8) +
            URLEncoder.encode(graph, StandardCharsets.UTF_8) +
            URLEncoder.encode("> {?concept skos:broader ?topConcept .}}", StandardCharsets.UTF_8) +
            "&format=json"
        );
        log.info("Creating {}", this);
    }

    @Override
    @SneakyThrows
    public Multimap<String, String> retrieve() {
        Multimap<String, String> toReturn = ArrayListMultimap.create();
        try {
            val response = template.getForEntity(
                this.query,
                Concept[].class
            );
            Arrays.stream(response.getBody())
                .forEach(concept -> toReturn.put(concept.topConcept, concept.concept));
            log.info("Vocabulary top concepts retrieved: {}", toReturn.keySet());
        } catch (Exception ex) {
            log.info("Failed to retrieve vocabulary - status: {}, query: {}", ex.getMessage(), this.query);
        }
        return toReturn;
    }

    @Data
    @Accessors(chain = true)
    public static class Concept {
        String concept, topConcept;
    }
}
