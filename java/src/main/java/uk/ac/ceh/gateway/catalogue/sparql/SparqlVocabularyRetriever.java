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
import java.util.Map;
import java.util.Objects;

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
    private final String sparqlEndpoint;


    @SneakyThrows
    public SparqlVocabularyRetriever(RestTemplate template, String sparqlEndpoint) {
        this.template = template;
        this.sparqlEndpoint = sparqlEndpoint;
        log.info("Creating {}", this);
    }

    @Override
    @SneakyThrows
    public Multimap<String, String> retrieve() {
        Multimap<String, String> toReturn = ArrayListMultimap.create();

            log.info("Retrieving Vocabulary Facets ");
            for (VocabularyFacet vocabularyFacet : VocabularyFacet.values()) {
                String facet = vocabularyFacet.getFacetName();
                try {
                String vocab = vocabSelector(vocabularyFacet);
                URI query = vocab.isEmpty()
                    ? querySelector(vocabularyFacet, sparqlEndpoint)
                    : querySelector(vocabularyFacet, sparqlEndpoint, vocab);
                log.info("Facet {} being retrieved using Query: {}", facet, query);

                val response = template.getForEntity(query, SparqlQueryResponse.class);
                Objects.requireNonNull(response.getBody()).getResults().getBindings().forEach(binding -> toReturn.put(facet, binding.getUri().getValue()));

                log.info("The query has run with facet " + facet);
                } catch (Exception ex) {
                    log.info("Failed to retrieve vocabulary - status: {}", ex.getMessage());
                }
            }
            //remove this logging before PR
        log.info("Printing out the contents of toReturn object which forms the vocabulary should appear as Key : value");
        for (Map.Entry<String, String> entry : toReturn.entries()) {
            String key = entry.getKey(), value = entry.getValue();
            log.info( key + " : " + value);
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
            URLEncoder.encode("PREFIX imp: <http://vocabs.ceh.ac.uk/imp/> ", StandardCharsets.UTF_8) +
            URLEncoder.encode("SELECT DISTINCT ?uri FROM imp: FROM inms: ", StandardCharsets.UTF_8) +
            URLEncoder.encode("WHERE {", StandardCharsets.UTF_8) +
            URLEncoder.encode(String.format("{imp:%s skos:narrower ?uri.}", facet) , StandardCharsets.UTF_8) +
            URLEncoder.encode("UNION", StandardCharsets.UTF_8) +
            URLEncoder.encode(String.format("{?uri skos:topConceptOf inms:%s.}", facet), StandardCharsets.UTF_8) +
            URLEncoder.encode("}", StandardCharsets.UTF_8) +
            "&format=json"
        );
    }

    public URI queryBuilderInms(String sparqlEndpoint, String facet, String vocab){

        return URI.create(sparqlEndpoint +
            "?query=" +
            URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", StandardCharsets.UTF_8) +
            URLEncoder.encode(String.format("PREFIX vocab: %s ", vocab), StandardCharsets.UTF_8) +
            URLEncoder.encode("SELECT DISTINCT ?uri FROM vocab: ", StandardCharsets.UTF_8) +
            URLEncoder.encode(String.format("WHERE {?uri skos:topConceptOf vocab:%s.}", facet), StandardCharsets.UTF_8) +
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
            URLEncoder.encode("SELECT DISTINCT ?uri FROM vocab: ", StandardCharsets.UTF_8) +
            URLEncoder.encode(String.format("WHERE {vocab:%s skos:%s ?uri.}", facet, narrow), StandardCharsets.UTF_8) +
            "&format=json"
        );
    }

    public URI querySelector(VocabularyFacet vocabularyFacet, String... queryArgs) {
        if (queryArgs.length == 0) {
            log.info("Too few args");
            throw new IllegalArgumentException();
        } else if (queryArgs.length > 2) {
            log.info("Too much info");
            throw new ArrayIndexOutOfBoundsException();
        }

        String sparqlEndpoint = queryArgs[0];
        String vocab = (queryArgs.length == 2) ? queryArgs[1] : "";
        String facet = vocabularyFacet.getFacetName();

        return switch (vocabularyFacet) {
            case WATER_POLLUTANT, TOPIC                              -> queryBuilderUnion(sparqlEndpoint, facet);
            case INMS_DEMONSTRATION_REGION, INMS_PROJECT, MODEL_TYPE -> queryBuilderInms(sparqlEndpoint, facet, vocab);
            default                                                  -> queryBuilderOther(sparqlEndpoint, facet, vocab);
        };
    }

    public String vocabSelector(VocabularyFacet facet){
        return switch (facet) {
            case ASSIST_RESEARCH_THEMES, ASSIST_TOPICS                                                -> "<http://onto.nerc.ac.uk/CEHMD/>";
            case IMP_DATE_TYPE                                                                        -> "<http://vocabs.ceh.ac.uk/imp/>";
            case UKCEH_RESEARCH_THEME, UKCEH_RESEARCH_PROJECT, UKCEH_SCIENCE_CHALLENGE, UKCEH_SERVICE -> "<http://vocabs.ceh.ac.uk/ukscape/>";
            case INMS_DEMONSTRATION_REGION, INMS_PROJECT, MODEL_TYPE                                  -> "<http://vocabs.ceh.ac.uk/inms/>";
            default -> ""; // No vocab needed for others
        };
    }
}
