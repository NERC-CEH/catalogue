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
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class SparqlKeywordVocabularyTest {
    private SparqlKeywordVocabulary target;

    private MockRestServiceServer mockServer;
    private static final String SPARQLENDPOINT = "https://example.com";
    private static final String GRAPH = "graph";
    private static final String WHERE = "where";
    private static final String COLLECTION = "keywords";
    private static final String VOCABULARY_ID = "VocabularyId";
    private static final String JSON = "vocabularies.json";

    @Mock
    private SolrClient solrClient;

    @BeforeEach
    public void init() {
        val restTemplate = new RestTemplate();
        target = new SparqlKeywordVocabulary(restTemplate, solrClient, SPARQLENDPOINT,GRAPH,WHERE,
                VOCABULARY_ID, "vocabularyName", new ArrayList<>());
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }


    @Test
    @SneakyThrows
    public void successfullyGetVocabularies() {
        //Given
        val response = IOUtils.toString(getClass().getResource(JSON), StandardCharsets.UTF_8);
        mockServer.expect(requestTo(getURI(GRAPH, WHERE)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        //When
        target.retrieve();

        //Then
        mockServer.verify();
        verify(solrClient).deleteByQuery(COLLECTION, "vocabId:" + VOCABULARY_ID);
        verify(solrClient, times(5)).addBean(eq("keywords"),
                any(VocabularyKeyword.class));
    }

    @Test
    @SneakyThrows
    public void ThrowKeywordVocabularyException() {

        //Given
        val response = IOUtils.toString(getClass().getResource(JSON), StandardCharsets.UTF_8);
        mockServer.expect(requestTo(getURI(GRAPH, WHERE)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        when(solrClient.addBean(eq("keywords"), any(VocabularyKeyword.class)))
                .thenThrow(new SolrServerException("Test"));

        //When
        Assertions.assertThrows(KeywordVocabularyException.class, () -> {
            target.retrieve();
        });
    }

    URI getURI(String graph, String where){
        return URI.create(
                SPARQLENDPOINT + "?query=" +
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