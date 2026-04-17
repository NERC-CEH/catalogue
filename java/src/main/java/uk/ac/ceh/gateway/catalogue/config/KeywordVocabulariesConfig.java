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
import uk.ac.ceh.gateway.catalogue.vocabularies.LocalKeywordVocabulary;
import uk.ac.ceh.gateway.catalogue.vocabularies.SparqlKeywordVocabulary;

@Configuration
public class KeywordVocabulariesConfig {

    @Profile("server-eidc")
    @Bean
    public KeywordVocabulary assistTopicsVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://onto.nerc.ac.uk/CEHMD/>",
                "?uri skos:broader <http://onto.nerc.ac.uk/CEHMD/assist-topics> . ?uri skos:prefLabel ?label .",
                "assist-topics",
                "Topics"
                );
            }

    @Profile("server-eidc")
    @Bean
    public KeywordVocabulary assistResearchThemesVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://onto.nerc.ac.uk/CEHMD/>",
                "?uri skos:broader <http://onto.nerc.ac.uk/CEHMD/assist-research-themes> . ?uri skos:prefLabel ?label .",
                "assist-research-themes",
                "Research Themes"
                );
            }

    @Profile("server-eidc")
    @Bean
    public KeywordVocabulary castVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://onto.nerc.ac.uk/CAST/>",
                "?uri skos:prefLabel ?label .",
                "cast",
                "CAST"
                );
            }

    @Profile("server-datalabs")
    @Bean
    public KeywordVocabulary dukemsPollutant(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val where = "?uri skos:broader+ <http://vocabs.ceh.ac.uk/dukems#16> . ?uri skos:prefLabel ?label .";
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/dukems#>",
                where,
                "dukems-pollutant",
                "Pollutants"
                );
            }

    @Profile("server-datalabs")
    @Bean
    public KeywordVocabulary dukemsSector(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        val where = "?uri skos:broader+ <http://vocabs.ceh.ac.uk/dukems#50> . ?uri skos:prefLabel ?label .";
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/dukems#>",
                where,
                "dukems-sector",
                "Sectors"
                );
            }

    @Profile("server-eidc")
    @Bean
    public KeywordVocabulary gemetVocabulary(
            SolrClient solrClient,
            @Value("${gemet.local}") String gemetLocalPath
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
        return new LocalKeywordVocabulary(
                "gemet",
                "GEMET",
                gemetLocalPath,
                "",
                "/uri",
                "/preferredLabel/string",
                solrClient
                );
            }

    @Profile("server-inms")
    @Bean
    public KeywordVocabulary inmsVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/inms/>",
                "?uri a skos:Concept; skos:prefLabel ?label .",
                "inms",
                "INMS"
                );
            }

    @Profile("server-pimfe")
    @Bean
    public KeywordVocabulary pimfeCastVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://onto.nerc.ac.uk/CAST/>",
                "?uri skos:prefLabel ?label .",
                "cast",
                "CAST"
                );
            }

    @Profile("server-pimfe")
    @Bean
    public KeywordVocabulary pimfeResearchThemeVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/ukscape/>",
                "?uri skos:broader <http://vocabs.ceh.ac.uk/ukscape/research-theme> . ?uri skos:prefLabel ?label .",
                "research-theme",
                "Research themes"
                );
            }

    @Profile("server-eidc")
    @Bean
    public KeywordVocabulary ukcehResearchProjectVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/ukscape/>",
                "?uri skos:broader <http://vocabs.ceh.ac.uk/ukscape/research-project> . ?uri skos:prefLabel ?label .",
                "research-project",
                "Research projects"
                );
            }

    @Profile("server-eidc")
    @Bean
    public KeywordVocabulary ukcehResearchThemeVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/ukscape/>",
                "?uri skos:broader <http://vocabs.ceh.ac.uk/ukscape/research-theme> . ?uri skos:prefLabel ?label .",
                "research-theme",
                "Research themes"
                );
            }

    @Profile("server-eidc")
    @Bean
    public KeywordVocabulary ukcehScienceChallengeVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/ukscape/>",
                "?uri skos:broader <http://vocabs.ceh.ac.uk/ukscape/science-challenge> . ?uri skos:prefLabel ?label .",
                "science-challenge",
                "Science challenges"
                );
            }

    @Profile("server-eidc")
    @Bean
    public KeywordVocabulary ukcehServiceVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.ceh.ac.uk/ukscape/>",
                "?uri skos:broader <http://vocabs.ceh.ac.uk/ukscape/service> . ?uri skos:prefLabel ?label .",
                "service",
                "Services"
                );
            }

}
