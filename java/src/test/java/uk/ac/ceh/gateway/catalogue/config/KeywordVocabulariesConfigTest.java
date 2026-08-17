package uk.ac.ceh.gateway.catalogue.config;

import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KeywordVocabulariesConfigTest {

    private static final String SPARQL_ENDPOINT = "https://example.com/sparql";

    @Mock private RestTemplate restTemplate;
    @Mock private SolrClient solrClient;

    /*
     * The FDRI graph holds both the grouping concepts a facet is built from
     * (catchment, category, spatial-scale, timeseries) and the terms beneath
     * them. Only the terms belong in the editor's keyword picker: tagging a
     * record with a grouping concept populates no facet, because the index-time
     * membership query only matches concepts asserting skos:broader.
     */
    @Test
    void fdriVocabularyHarvestsOnlyConceptsBeneathAGroupingConcept() {
        //given
        val vocabulary = new KeywordVocabulariesConfig()
            .fdriVocabulary(restTemplate, solrClient, SPARQL_ENDPOINT);

        given(restTemplate.getForEntity(any(URI.class), eq(JsonNode.class)))
            .willReturn(ResponseEntity.ok(JsonMapper.builder().build().createObjectNode()));

        //when
        vocabulary.retrieve();

        //then
        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).getForEntity(captor.capture(), eq(JsonNode.class));
        val query = URLDecoder.decode(captor.getValue().toString(), StandardCharsets.UTF_8);

        assertThat(query, containsString("GRAPH <https://digital.ceh.ac.uk/vocab/fdri/>"));
        assertThat(query, containsString("?uri skos:broader ?concept ."));
        assertThat(query, containsString("?uri skos:prefLabel ?label ."));
    }

    @Test
    void fdriVocabularyIsIdentifiedAsFdri() {
        //given
        val vocabulary = new KeywordVocabulariesConfig()
            .fdriVocabulary(restTemplate, solrClient, SPARQL_ENDPOINT);

        //then
        assertThat(vocabulary.getId(), containsString("fdri"));
        assertThat(vocabulary.getGraph(), containsString("https://digital.ceh.ac.uk/vocab/fdri/"));
        assertThat(vocabulary.getGraph(), not(containsString("vocabs.ceh.ac.uk")));
    }
}
