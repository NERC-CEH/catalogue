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

    @Profile("server-eidc")
    @Bean
    public KeywordVocabulary envThesVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${elter.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<http://vocabs.lter-europe.net/EnvThes/>",
                "?uri skos:prefLabel ?label . FILTER NOT EXISTS { ?uri <http://www.w3.org/2002/07/owl#deprecated> true}",
                "envThes",
                "EnvThes"
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
    public KeywordVocabulary fdriVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        /* FDRI (Floods and Droughts Research Infrastructure)
         *
         * Harvests the FDRI terms - catchments, categories, spatial scales and
         * the timeseries flag - so cataloguers can select them in the editor's
         * keyword picker. The all-catalogue search facets built from these
         * keywords are defined in CatalogueServiceConfig.
         *
         * Requiring a skos:broader excludes the four grouping concepts the
         * facets are built from, which have none. They would otherwise be
         * offered in the picker as if they were terms, and tagging a record
         * with one populates no facet: SparqlVocabularyRetriever only treats a
         * concept as a member of a facet if it declares that facet as broader.
         *
         * See EMC-885 / dri-one #149.
         */
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<https://digital.ceh.ac.uk/vocab/fdri/>",
                "?uri skos:broader ?concept . ?uri skos:prefLabel ?label .",
                "fdri",
                "FDRI"
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
    public KeywordVocabulary ukcehResearchActivitiesVocabulary(
            @Qualifier("sparql") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
            ) {
        return new SparqlKeywordVocabulary(
                restTemplate,
                solrClient,
                sparqlEndpoint,
                "<https://digital.ceh.ac.uk/vocab/ra/>",
                "?uri skos:broader+ <https://digital.ceh.ac.uk/vocab/ra/1>; skos:prefLabel ?label. OPTIONAL { ?uri <http://www.w3.org/2002/07/owl#deprecated> ?deprecated.}FILTER (!BOUND(?deprecated) || ?deprecated = false)",
                "research-activity",
                "Research activities"
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
