package uk.ac.ceh.gateway.catalogue.sparql;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.Arrays;

@AllArgsConstructor
public class SparqlVocabularyRetriever implements VocabularyRetriever {
    private final RestTemplate template;
    private final String queryTemplate;

    @SneakyThrows
    public SparqlVocabularyRetriever(RestTemplate template) {
        this.template = template;
        this.queryTemplate = new StringBuilder()
            .append("http://vocabs.ceh.ac.uk/evn/tbl/sparql?query=")
            .append(URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", "UTF-8"))
            .append(URLEncoder.encode("SELECT ?concept ?topConcept ", "UTF-8"))
            .append(URLEncoder.encode("WHERE {GRAPH <", "UTF-8"))
            .append("{graph}")
            .append(URLEncoder.encode("> {?concept skos:broader ?topConcept .}}", "UTF-8"))
            .append("&format=json-simple")
            .toString();
    }

    @Override
    @SneakyThrows
    public Multimap<String, String> retrieve(String sparqlEndpoint, String graph) {
        // TODO: wrap in try catch
        ResponseEntity<Concept[]> responseEntity = template.exchange(
            this.queryTemplate,
            HttpMethod.GET,
            null,
            Concept[].class,
            graph
        );
        Multimap<String, String> toReturn = ArrayListMultimap.create();
        Arrays.stream(responseEntity.getBody())
            .forEach(concept -> toReturn.put(concept.topConcept, concept.concept));
        return toReturn;
    }

    @Data
    @Accessors(chain = true)
    public static class Concept {
        String concept, topConcept;
    }
}
