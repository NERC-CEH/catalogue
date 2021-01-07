package uk.ac.ceh.gateway.catalogue.config;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.components.vocab.Vocabulary;
import uk.ac.ceh.components.vocab.rdf.RdfVocabulary;
import uk.ac.ceh.components.vocab.sparql.SparqlVocabulary;

public class HttpVocabularyConfig {

    public List<Vocabulary> keywords() {
        List<Vocabulary> keywords = new ArrayList<>();
        keywords.add(new SparqlVocabulary(SPARQL_ENDPOINT, "EnvThes", String.format(HARVEST_QUERY, "urn:x-evn-pub:envthes")));
        keywords.add(new SparqlVocabulary(SPARQL_ENDPOINT, "Cast", String.format(HARVEST_QUERY, "urn:x-evn-pub:cast")));
        keywords.add(new SparqlVocabulary(SPARQL_ENDPOINT, "ECV", String.format(ECV_QUERY, "urn:x-evn-pub:ef")));
//        keywords.add(new RdfVocabulary("https://www.eionet.europa.eu/gemet/exports/latest/en/gemet-definitions.rdf", "Gemet"));
//        keywords.add(new RdfVocabulary("http://dd.eionet.europa.eu/vocabulary/aq/pollutant/rdf", "pollutant"));
        return keywords;
    }

    public List<Vocabulary> codelists() {
        List<Vocabulary> codeLists = new ArrayList<>();
        codeLists.add(new SparqlVocabulary(SPARQL_ENDPOINT, "environmentalDomain", String.format(EF_QUERY,  "http://onto.nerc.ac.uk/EF/environmentalDomain")));
        codeLists.add(new SparqlVocabulary(SPARQL_ENDPOINT, "purposeOfCollection", String.format(EF_QUERY, "http://onto.nerc.ac.uk/EF/purposeOfCollection")));
        codeLists.add(new SparqlVocabulary(SPARQL_ENDPOINT, "responsiblePartyRole", String.format(EF_QUERY, "http://onto.nerc.ac.uk/EF/responsiblePartyRole")));
        codeLists.add(new SparqlVocabulary(SPARQL_ENDPOINT, "fundingCategory", String.format(EF_QUERY, "http://onto.nerc.ac.uk/EF/fundingCategory")));
        codeLists.add(new SparqlVocabulary(SPARQL_ENDPOINT, "resultAcquisitionSource", String.format(EF_QUERY, "http://onto.nerc.ac.uk/EF/resultAcquisitionSource")));
        codeLists.add(new SparqlVocabulary(SPARQL_ENDPOINT, "resultNature", String.format(EF_QUERY, "http://onto.nerc.ac.uk/EF/resultNature")));
        codeLists.add(new SparqlVocabulary(SPARQL_ENDPOINT, "measurementRegime", String.format(EF_QUERY, "http://onto.nerc.ac.uk/EF/measurementRegime")));
        codeLists.add(new SparqlVocabulary(SPARQL_ENDPOINT, "facilityType", String.format(EF_QUERY, "http://onto.nerc.ac.uk/EF/facilityType")));
        codeLists.add(new SparqlVocabulary(SPARQL_ENDPOINT, "topicCategory", TOPIC_CATEGORY_QUERY));
        return codeLists;
    }

    private static final String SPARQL_ENDPOINT = "http://vocabs.ceh.ac.uk/edg/tbl/sparql/";
    private static final String HARVEST_QUERY = "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> SELECT ?concept ?prefLabel WHERE {GRAPH <%s> { ?concept a skos:Concept ; skos:prefLabel ?prefLabel . FILTER(langMatches(lang(?prefLabel), \"EN\")) }}";
    private static final String EF_QUERY = "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> SELECT ?concept ?prefLabel WHERE {GRAPH <urn:x-evn-pub:ef> { ?concept a skos:Concept ; skos:broader <%s> ; skos:prefLabel ?prefLabel . }} ORDER BY lcase(?prefLabel)";
    private static final String TOPIC_CATEGORY_QUERY = "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> SELECT ?concept ?prefLabel WHERE {GRAPH <urn:x-evn-pub:ef> {?concept a skos:Concept ; skos:prefLabel ?prefLabel . FILTER(STRSTARTS(STR(?concept), 'http://inspire.ec.europa.eu/codelist/TopicCategory/')) .} } ORDER BY lcase(?prefLabel)";
    private static final String ECV_QUERY = "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> SELECT ?concept ?prefLabel WHERE{GRAPH <urn:x-evn-pub:ef> {<http://onto.nerc.ac.uk/EF/ECV> skos:hasTopConcept ?tc.?tc skos:narrower* ?concept.?concept skos:prefLabel ?prefLabel.}}";
}
