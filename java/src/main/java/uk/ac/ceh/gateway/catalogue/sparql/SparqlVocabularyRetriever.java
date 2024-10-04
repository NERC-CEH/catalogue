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
//    private final URI query;
    private final String sparqlEndpoint;


    @SneakyThrows
    public SparqlVocabularyRetriever(RestTemplate template, String sparqlEndpoint, String graph) {
        this.template = template;
        this.sparqlEndpoint = sparqlEndpoint;
//        this.query = URI.create(sparqlEndpoint +
//            "?query=" +
//            URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", StandardCharsets.UTF_8) +
//            URLEncoder.encode("SELECT ?concept ?topConcept ", StandardCharsets.UTF_8) +
//            URLEncoder.encode("WHERE {GRAPH <", StandardCharsets.UTF_8) +
//            URLEncoder.encode(graph, StandardCharsets.UTF_8) +
//            URLEncoder.encode("> {?concept skos:broader ?topConcept .}}", StandardCharsets.UTF_8) +
//            "&format=json"
//        );
        log.info("Creating {}", this);
    }


//    @Override
//    @SneakyThrows
//    public Multimap<String, String> retrieve() {
//        Multimap<String, String> toReturn = ArrayListMultimap.create();
//        try {
//            val response = template.getForEntity(
//                this.query,
//                Concept[].class
//            );
//            Arrays.stream(response.getBody())
//                .forEach(concept -> toReturn.put(concept.topConcept, concept.concept));
//            log.info("Vocabulary top concepts retrieved: {}", toReturn.keySet());
//        } catch (Exception ex) {
//            log.info("Failed to retrieve vocabulary - status: {}, query: {}", ex.getMessage(), this.query);
//        }
//        return toReturn;
//    }

    @Override
    @SneakyThrows
    public Multimap<String, String> retrieve() {
        Multimap<String, String> toReturn = ArrayListMultimap.create();
        String[] facets = {"assist-research-themes", "assist-topics", "dt", "topic", "wp", "region", "project",
            "Model_Type", "research-theme", "research-project", "science-challenge", "service"};
        try {
            log.info("Retrieving Vocabulary Facets");
            for (String facet : facets) {
                String vocab = vocabSelector(facet);
                URI query = vocab.isEmpty() ? querySelector(sparqlEndpoint, facet)
                    : querySelector(sparqlEndpoint, facet, vocab);
                log.info("Facet {} being retrieved using Query: {}", facet, query);
                val response = template.getForEntity(
                    query,
                    Concept[].class
                );
                Arrays.stream(response.getBody())
                    .forEach(concept -> toReturn.put(concept.topConcept, concept.concept));
            }
            log.info("Vocabulary top concepts retrieved: {}", toReturn.keySet());
        } catch (Exception ex) {
            log.info("Failed to retrieve vocabulary - status: {}", ex.getMessage());
        }
        return toReturn;
    }

    @Data
    @Accessors(chain = true)
    public static class Concept {
        String concept, topConcept;
    }

    public URI queryBuilderUnion(String sparqlEndpoint, String facet){

        return URI.create(sparqlEndpoint +
            "?query=" +
            URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", StandardCharsets.UTF_8) +
            URLEncoder.encode("PREFIX inms: <http://vocabs.ceh.ac.uk/inms/> ", StandardCharsets.UTF_8) +
            URLEncoder.encode("PREFIX imp: <http://vocabs.ceh.ac.uk/imp> ", StandardCharsets.UTF_8) +
            URLEncoder.encode("SELECT DISTINCT ?uri ?label FROM imp: FROM inms:", StandardCharsets.UTF_8) +
            URLEncoder.encode("WHERE {", StandardCharsets.UTF_8) +
            URLEncoder.encode(String.format("{imp:%s skos:narrower ?uri. ?uri skos:prefLabel ?label}", facet) , StandardCharsets.UTF_8) +
            URLEncoder.encode("UNION", StandardCharsets.UTF_8) +
            URLEncoder.encode(String.format("{?uri skos:topConceptOf inms:%s. ?uri skos:prefLabel ?label}", facet), StandardCharsets.UTF_8) +
            URLEncoder.encode("}", StandardCharsets.UTF_8) +
            "&format=json"
        );
    }

    public URI queryBuilderInms(String sparqlEndpoint, String facet){

        return URI.create(sparqlEndpoint +
            "?query=" +
            URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", StandardCharsets.UTF_8) +
            URLEncoder.encode("PREFIX vocab: <http://vocabs.ceh.ac.uk/inms/> ", StandardCharsets.UTF_8) +
            URLEncoder.encode("SELECT DISTINCT ?uri ?label FROM vocab: ", StandardCharsets.UTF_8) +
            URLEncoder.encode(String.format("WHERE {?uri skos:topConceptOf vocab:%s. ?uri skos:prefLabel ?label}", facet), StandardCharsets.UTF_8) +
            "&format=json"
        );
    }

    public URI queryBuilderOther(String sparqlEndpoint, String facet, String vocab){
        String narrow;
        switch (facet) {
            case "assist-research-themes", "assist-topics", "dt" -> narrow = "narrower+";
            default -> narrow = "narrower";
        }

        return URI.create(sparqlEndpoint +
            "?query=" +
            URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", StandardCharsets.UTF_8) +
            URLEncoder.encode(String.format("PREFIX vocab: %s ", vocab), StandardCharsets.UTF_8) +
            URLEncoder.encode("SELECT DISTINCT ?uri ?label FROM vocab: ", StandardCharsets.UTF_8) +
            URLEncoder.encode(String.format("WHERE {vocab:%s skos:%s ?uri. ?uri skos:prefLabel ?label}", facet, narrow), StandardCharsets.UTF_8) +
            "&format=json"
        );
    }

    public URI querySelector(String sparqlEndpoint, String... queryArgs) {
        if (queryArgs.length == 0) {
            log.info("Too few args");
            throw new IllegalArgumentException();
        } else if (queryArgs.length > 2) {
            log.info("Too much info");
            throw new ArrayIndexOutOfBoundsException();
        }

        String facet = queryArgs[0];
        String vocab = (queryArgs.length == 2) ? queryArgs[1] : "";

        return switch (facet) {
            case "wp", "topic"                     -> queryBuilderUnion(sparqlEndpoint, facet);
            case "region", "project", "Model_Type" -> queryBuilderInms(sparqlEndpoint, facet);
            default                                -> queryBuilderOther(sparqlEndpoint, facet, vocab);
        };
    }

    public String vocabSelector(String facet){
        return switch (facet) {
            case "assist-research-themes", "assist-topics" -> "<http://onto.nerc.ac.uk/CEHMD/>";
            case "dt" -> "<http://vocabs.ceh.ac.uk/imp/>";
            case "research-theme", "research-project", "science-challenge", "service" -> "<http://vocabs.ceh.ac.uk/ukscape/>";
            default -> ""; // No vocab needed for others
        };
    }
}
