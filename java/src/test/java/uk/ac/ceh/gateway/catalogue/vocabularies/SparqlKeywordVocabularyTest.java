package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class SparqlKeywordVocabularyTest {
    private SparqlKeywordVocabulary target;
    private MockRestServiceServer mockServer;

    private static final String SPARQL_ENDPOINT = "https://example.com";
    private static final String GRAPH = "urn:x-evn-master:cehmd";
    @SuppressWarnings("HttpUrlsUsage")
    private static final String WHERE = "<http://onto.nerc.ac.uk/CEHMD/assist-research-themes> skos:hasTopConcept ?uri . ?uri skos:prefLabel ?label .";
    private static final String COLLECTION = "keywords";
    private static final String VOCABULARY_ID = "assist-topics";
    private static final String JSON = "vocabulary.json";

    @Mock
    private SolrClient solrClient;

    @BeforeEach
    public void init() {
        val restTemplate = new RestTemplate();
        val catalogues = Arrays.asList("test-0", "test-1");
        target = new SparqlKeywordVocabulary(restTemplate, solrClient, SPARQL_ENDPOINT,GRAPH,WHERE,
                VOCABULARY_ID, "vocabularyName", catalogues);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @SneakyThrows
    public void successfullyGetVocabulary() {
        //Given
        val response = IOUtils.toString(
            Objects.requireNonNull(
                getClass().getResource(JSON)
            ),
            UTF_8
        );
        mockServer.expect(requestTo(getURI(GRAPH, WHERE)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        //When
        target.retrieve();

        //Then
        verify(solrClient).deleteByQuery(COLLECTION, "vocabId:" + VOCABULARY_ID);
        verify(solrClient, times(5)).addBean(
            eq("keywords"),
            any(Keyword.class)
        );
        verify(solrClient).commit(COLLECTION);
    }

    @Test
    @SneakyThrows
    public void throwKeywordVocabularyException() {

        //Given
        val response = IOUtils.toString(
            Objects.requireNonNull(
                getClass().getResource(JSON)
            ),
            UTF_8
        );
        mockServer.expect(requestTo(getURI(GRAPH, WHERE)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        given(solrClient.addBean(eq("keywords"), any(Keyword.class)))
                .willThrow(new SolrServerException("Test"));

        //When
        Assertions.assertThrows(KeywordVocabularyException.class, () ->
            target.retrieve()
        );
    }

    @Test
    void getCatalogueIds() {
        //given


        ///when
        assertTrue(target.usedInCatalogue("test-0"));
        assertFalse(target.usedInCatalogue("not"));

        //then
    }

    @SuppressWarnings({"HttpUrlsUsage", "SameParameterValue"})
    URI getURI(String graph, String where){
        return URI.create(
                SPARQL_ENDPOINT + "?query=" +
                        URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", UTF_8) +
                        URLEncoder.encode("SELECT ?uri ?label ", UTF_8) +
                        URLEncoder.encode("WHERE {GRAPH <", UTF_8) +
                        URLEncoder.encode(graph, UTF_8) +
                        URLEncoder.encode("> {", UTF_8) +
                        URLEncoder.encode(where, UTF_8) +
                        URLEncoder.encode("}}", UTF_8) +
                        "&format=json-simple");
    }

}