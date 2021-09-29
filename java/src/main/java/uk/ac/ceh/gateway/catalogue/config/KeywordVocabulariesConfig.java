package uk.ac.ceh.gateway.catalogue.config;

import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;
import uk.ac.ceh.gateway.catalogue.vocabularies.SparqlKeywordVocabulary;

import java.util.Arrays;
import java.util.List;

@Configuration
public class KeywordVocabulariesConfig {

    @Profile("server:eidc")
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
            "?uri skos:broader <http://onto.nerc.ac.uk/CEHMD/assist-topics> . ?uri skos:prefLabel ?label .",
            "assist-topics",
            "Topics",
            catalogueIds
        );
    }

    @Profile("server:eidc")
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
            "?uri skos:broader <http://onto.nerc.ac.uk/CEHMD/assist-research-themes> . ?uri skos:prefLabel ?label .",
            "assist-research-themes",
            "Research Themes",
            catalogueIds
        );
    }

    @Profile("server:eidc")
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

    @Profile("server:elter")
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

    @Profile("server:inms")
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

    @Profile("server:eidc")
    @Bean
    public KeywordVocabulary ukscapeResearchProjectVocabulary(
        @Qualifier("sparql") RestTemplate restTemplate,
        SolrClient solrClient,
        @Value("${sparql.endpoint}") String sparqlEndpoint
    ) {
        val catalogueIds = List.of("ukscape");
        return new SparqlKeywordVocabulary(
            restTemplate,
            solrClient,
            sparqlEndpoint,
            "urn:x-evn-master:cehmd",
            "?uri skos:broader <http://onto.nerc.ac.uk/CEHMD/ukscape/research-project> . ?uri skos:prefLabel ?label .",
            "ukscape-research-project",
            "Research Projects",
            catalogueIds
        );
    }

    @Profile("server:eidc")
    @Bean
    public KeywordVocabulary ukscapeResearchThemeVocabulary(
        @Qualifier("sparql") RestTemplate restTemplate,
        SolrClient solrClient,
        @Value("${sparql.endpoint}") String sparqlEndpoint
    ) {
        val catalogueIds = List.of("ukscape");
        return new SparqlKeywordVocabulary(
            restTemplate,
            solrClient,
            sparqlEndpoint,
            "urn:x-evn-master:cehmd",
            "?uri skos:broader <http://onto.nerc.ac.uk/CEHMD/ukscape/research-theme> . ?uri skos:prefLabel ?label .",
            "ukscape-research-theme",
            "Research Themes",
            catalogueIds
        );
    }

    @Bean
    public KeywordVocabulary ukscapeScienceChallengeVocabulary(
        @Qualifier("sparql") RestTemplate restTemplate,
        SolrClient solrClient,
        @Value("${sparql.endpoint}") String sparqlEndpoint
    ) {
        val catalogueIds = List.of("ukscape");
        return new SparqlKeywordVocabulary(
            restTemplate,
            solrClient,
            sparqlEndpoint,
            "urn:x-evn-master:cehmd",
            "?uri skos:broader <http://onto.nerc.ac.uk/CEHMD/ukscape/science-challenge> . ?uri skos:prefLabel ?label .",
            "ukscape-science-challenge",
            "Science Challenges",
            catalogueIds
        );
    }

    @Profile("server:eidc")
    @Bean
    public KeywordVocabulary ukscapeServiceVocabulary(
        @Qualifier("sparql") RestTemplate restTemplate,
        SolrClient solrClient,
        @Value("${sparql.endpoint}") String sparqlEndpoint
    ) {
        val catalogueIds = List.of("ukscape");
        return new SparqlKeywordVocabulary(
            restTemplate,
            solrClient,
            sparqlEndpoint,
            "urn:x-evn-master:cehmd",
            "?uri skos:broader <http://onto.nerc.ac.uk/CEHMD/ukscape/service> . ?uri skos:prefLabel ?label .",
            "ukscape-service",
            "Services",
            catalogueIds
        );
    }

}