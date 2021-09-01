package uk.ac.ceh.gateway.catalogue.config;

import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;
import uk.ac.ceh.gateway.catalogue.vocabularies.SparqlKeywordVocabulary;

import java.util.Arrays;
import java.util.List;

@Configuration
public class KeywordVocabulariesConfig {

    @Bean
    public KeywordVocabulary assistTopicsVocabulary(
        @Qualifier("sparql") RestTemplate restTemplate,
        SolrClient solrClient,
        @Value("${sparql.endpoint}") String sparqlEndpoint
    ) {
        val catalogueIds = List.of("assist");
        return new SparqlKeywordVocabulary(
            restTemplate,
            solrClient,
            sparqlEndpoint,
            "urn:x-evn-master:cehmd",
            "<http://onto.nerc.ac.uk/CEHMD/assist-topics> skos:hasTopConcept ?uri . ?uri skos:prefLabel ?label .",
            "assist-topics",
            "ASSIST Topics",
            catalogueIds
        );
    }

    @Bean
    public KeywordVocabulary assistResearchThemesVocabulary(
        @Qualifier("sparql") RestTemplate restTemplate,
        SolrClient solrClient,
        @Value("${sparql.endpoint}") String sparqlEndpoint
    ) {
        val catalogueIds = List.of("assist");
        return new SparqlKeywordVocabulary(
            restTemplate,
            solrClient,
            sparqlEndpoint,
            "urn:x-evn-master:cehmd",
            "<http://onto.nerc.ac.uk/CEHMD/assist-research-themes> skos:hasTopConcept ?uri . ?uri skos:prefLabel ?label .",
            "assist-research-themes",
            "ASSIST Research Themes",
            catalogueIds
        );
    }

    @Bean
    public KeywordVocabulary castVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${sparql.endpoint}") String sparqlEndpoint
    ) {
        val catalogueIds = Arrays.asList("assist", "eidc", "elter");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "urn:x-evn-master:CAST",
                "?uri skos:prefLabel ?label .",
                "cast",
                "CAST",
                catalogueIds
        );
    }

    @Bean
    public KeywordVocabulary envThesVocabulary(
        @Qualifier("sparql") RestTemplate restTemplate,
        SolrClient solrClient,
        @Value("${sparql.endpoint}") String sparqlEndpoint
    ) {
        val catalogueIds = List.of("elter");
        // Filters out deprecated concepts
        val where = "?uri skos:prefLabel ?label . FILTER NOT EXISTS { ?uri skos:broader <http://vocabs.lter-europe.net/EnvThes/1> }";
        return new SparqlKeywordVocabulary(
            restTemplate,
            solrClient,
            sparqlEndpoint,
            "urn:x-evn-master:EnvThes",
            where,
            "envThes",
            "EnvThes",
            catalogueIds
        );
    }

    @Bean
    public KeywordVocabulary inmsVocabulary(
        @Qualifier("sparql") RestTemplate restTemplate,
        SolrClient solrClient,
        @Value("${sparql.endpoint}") String sparqlEndpoint
    ) {
        val catalogueIds = List.of("inms");
        return new SparqlKeywordVocabulary(
            restTemplate,
            solrClient,
            sparqlEndpoint,
            "urn:x-evn-master:inms",
            "?uri skos:prefLabel ?label .",
            "inms",
            "INMS",
            catalogueIds
        );
    }

}
