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
import uk.ac.ceh.gateway.catalogue.vocabularies.HttpKeywordVocabulary;

import java.util.List;

@Configuration
public class KeywordVocabulariesConfig {

    @Profile("server:eidc")
    @Bean
    public KeywordVocabulary assistTopicsVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("assist");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://onto.nerc.ac.uk/CEHMD/>",
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
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("assist");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://onto.nerc.ac.uk/CEHMD/>",
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
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("assist", "eidc", "elter", "nm");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://onto.nerc.ac.uk/CAST/>",
                "?uri skos:prefLabel ?label .",
                "cast",
                "CAST",
                catalogueIds
                );
            }

    @Profile("server:datalabs")
    @Bean
    public KeywordVocabulary dukemsPollutant(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("datalabs");
        val where = "?uri skos:broader+ <http://vocabs.ceh.ac.uk/dukems#16> . ?uri skos:prefLabel ?label .";
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/dukems#>",
                where,
                "dukems-pollutant",
                "Pollutants",
                catalogueIds
                );
            }

    @Profile("server:datalabs")
    @Bean
    public KeywordVocabulary dukemsSector(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("datalabs");
        val where = "?uri skos:broader+ <http://vocabs.ceh.ac.uk/dukems#50> . ?uri skos:prefLabel ?label .";
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/dukems#>",
                where,
                "dukems-sector",
                "Sectors",
                catalogueIds
                );
            }

    @Profile("server:elter")
    @Bean
    public KeywordVocabulary envThesVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${elter.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("elter");
        // Filters out deprecated concepts
        val where = "?uri skos:prefLabel ?label . FILTER NOT EXISTS { ?uri <http://www.w3.org/2002/07/owl#deprecated> true}";
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.lter-europe.net/EnvThes/>",
                where,
                "envThes",
                "EnvThes",
                catalogueIds
                );
            }

    @Profile("server:elter")
    @Bean
    public KeywordVocabulary elterCLVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${elter.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("elter");
        // Filters out deprecated concepts
        val where = "?uri skos:prefLabel ?label . FILTER NOT EXISTS { ?uri <http://www.w3.org/2002/07/owl#deprecated> true}";
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.lter-europe.net/elter_cl/>",
                where,
                "elterCL",
                "elterCL",
                catalogueIds
                );
            }

    @Profile("server:eidc")
    @Bean
    public KeywordVocabulary gemetVocabulary(
            SolrClient solrClient,
            @Value("${gemet.concepturl}") String gemetConceptUrl
            ) {
        /* GEMET is the GEneral Multilingual Environmental Thesaurus
         *
         * This vocabulary was implemented using the documentation located at
         * https://www.eionet.europa.eu/gemet/en/webservices/
         *
         * Its purpose is to harvest the GEMET concepts only - so NOT themes,
         * groups and supergroups.
         *
         * See EMC-6 in Jira for details.
         */
        val catalogueIds = List.of("eidc","ukeof");
        return new HttpKeywordVocabulary(
                "gemet",
                "GEMET",
                gemetConceptUrl,
                "",
                "/uri",
                "/preferredLabel/string",
                solrClient,
                catalogueIds
                );
            }

    @Profile("server:inms")
    @Bean
    public KeywordVocabulary inmsVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("inms");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/inms/>",
                "?uri skos:prefLabel ?label .",
                "inms",
                "INMS",
                catalogueIds
                );
            }

    @Profile("server:pimfe")
    @Bean
    public KeywordVocabulary pimfeCastVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("pimfe");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://onto.nerc.ac.uk/CAST/>",
                "?uri skos:prefLabel ?label .",
                "cast",
                "CAST",
                catalogueIds
                );
            }

    @Profile("server:pimfe")
    @Bean
    public KeywordVocabulary pimfeResearchThemeVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("pimfe");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/ukscape/>",
                "?uri skos:broader <http://vocabs.ceh.ac.uk/ukscape/research-theme> . ?uri skos:prefLabel ?label .",
                "ukscape-research-theme",
                "Research Themes",
                catalogueIds
                );
            }

    @Profile("server:eidc")
    @Bean
    public KeywordVocabulary ukcehResearchProjectVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("ukscape");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/ukscape/>",
                "?uri skos:broader <http://vocabs.ceh.ac.uk/ukscape/research-project> . ?uri skos:prefLabel ?label .",
                "research-project",
                "Research Projects",
                catalogueIds
                );
            }

    @Profile("server:eidc")
    @Bean
    public KeywordVocabulary ukcehResearchThemeVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("ukscape");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/ukscape/>",
                "?uri skos:broader <http://vocabs.ceh.ac.uk/ukscape/research-theme> . ?uri skos:prefLabel ?label .",
                "research-theme",
                "Research Themes",
                catalogueIds
                );
            }

    @Profile("server:eidc")
    @Bean
    public KeywordVocabulary ukcehScienceChallengeVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("ukscape");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/ukscape/>",
                "?uri skos:broader <http://vocabs.ceh.ac.uk/ukscape/science-challenge> . ?uri skos:prefLabel ?label .",
                "science-challenge",
                "Science Challenges",
                catalogueIds
                );
            }

    @Profile("server:eidc")
    @Bean
    public KeywordVocabulary ukcehServiceVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val catalogueIds = List.of("ukscape");
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/ukscape/>",
                "?uri skos:broader <http://vocabs.ceh.ac.uk/ukscape/service> . ?uri skos:prefLabel ?label .",
                "service",
                "Services",
                catalogueIds
                );
            }

}
