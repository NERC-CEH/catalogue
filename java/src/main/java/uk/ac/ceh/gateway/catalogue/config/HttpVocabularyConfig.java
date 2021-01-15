package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.stereotype.Component;
import uk.ac.ceh.components.vocab.Vocabulary;
import uk.ac.ceh.components.vocab.sparql.SparqlVocabulary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class HttpVocabularyConfig implements VocabularyConfig {

    @Override
    public List<Vocabulary> keywords() {
        return Arrays.asList(
            new SparqlVocabulary(SPARQL_ENDPOINT, "EnvThes", String.format(HARVEST_QUERY, "urn:x-evn-pub:envthes")),
            new SparqlVocabulary(SPARQL_ENDPOINT, "Cast", String.format(HARVEST_QUERY, "urn:x-evn-pub:cast"))
        );
    }

    @Override
    public List<Vocabulary> codelists() {
        return Collections.emptyList();
    }

    private static final String SPARQL_ENDPOINT = "http://vocabs.ceh.ac.uk/edg/tbl/sparql/";
    private static final String HARVEST_QUERY = "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> SELECT ?concept ?prefLabel WHERE {GRAPH <%s> { ?concept a skos:Concept ; skos:prefLabel ?prefLabel . FILTER(langMatches(lang(?prefLabel), \"EN\")) }}";
}
