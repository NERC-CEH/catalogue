package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpKeywordVocabularyTest {
    private HttpKeywordVocabulary vocabularyService;

    private static final String COLLECTION = "keywords";
    private static final String VOCABULARY_ID = "testVocabId";
    private static final String VOCABULARY_NAME = "testVocabName";
    private static final String CONCEPTS_JSON = "concepts.json";
    private static final String THEMES_JSON = "themes.json";

    @Mock
    private SolrClient solrClient;

    @Test
    @SneakyThrows
    public void getSingleUrlVocabulary() {
        // Setup
        String endpointUrl = getClass().getResource(CONCEPTS_JSON).toString();
        val catalogues = Arrays.asList("test-0", "test-1");

        vocabularyService = new HttpKeywordVocabulary(
                VOCABULARY_ID,
                VOCABULARY_NAME,
                endpointUrl,
                "",
                "/uri",
                "/preferredLabel/string",
                solrClient,
                catalogues
                );

        // Given

        // When
        vocabularyService.retrieve();

        // Then
        verify(solrClient).deleteByQuery(COLLECTION, "vocabId:" + VOCABULARY_ID);
        verify(solrClient, times(10)).addBean(
            eq("keywords"),
            any(Keyword.class)
        );
        verify(solrClient).commit(COLLECTION);
    }

    @Test
    @SneakyThrows
    public void getMultipleUrlVocabulary() {
        // Setup
        String firstEndpointUrl = getClass().getResource(CONCEPTS_JSON).toString();
        String secondEndpointUrl = getClass().getResource(THEMES_JSON).toString();
        val catalogues = Arrays.asList("test-0", "test-1");
        val endpoints = Arrays.asList(firstEndpointUrl, secondEndpointUrl);

        vocabularyService = new HttpKeywordVocabulary(
                VOCABULARY_ID,
                VOCABULARY_NAME,
                endpoints,
                "",
                "/uri",
                "/preferredLabel/string",
                solrClient,
                catalogues
                );

        // Given

        // When
        vocabularyService.retrieve();

        // Then
        verify(solrClient).deleteByQuery(COLLECTION, "vocabId:" + VOCABULARY_ID);
        verify(solrClient, times(20)).addBean(
            eq("keywords"),
            any(Keyword.class)
        );
        verify(solrClient).commit(COLLECTION);
    }

    @Test
    @SneakyThrows
    public void failToCommunicateWithSolr() {
        // Setup
        String endpointUrl = getClass().getResource(CONCEPTS_JSON).toString();
        val catalogues = Arrays.asList("test-0", "test-1");

        vocabularyService = new HttpKeywordVocabulary(
                VOCABULARY_ID,
                VOCABULARY_NAME,
                endpointUrl,
                "",
                "/uri",
                "/preferredLabel/string",
                solrClient,
                catalogues
                );

        // Given
        given(solrClient.addBean(eq("keywords"), any(Keyword.class)))
                .willThrow(new SolrServerException("Test"));

        // When
        assertThrows(
            KeywordVocabularyException.class,
            () -> vocabularyService.retrieve()
        );

        // Then
        verify(solrClient).deleteByQuery(COLLECTION, "vocabId:" + VOCABULARY_ID);
        verify(solrClient, never()).commit(COLLECTION);
    }

    @Test
    @SneakyThrows
    public void failToCommunicateWithVocabServer() {
        // Setup
        val catalogues = Arrays.asList("test-0", "test-1");

        vocabularyService = new HttpKeywordVocabulary(
                VOCABULARY_ID,
                VOCABULARY_NAME,
                "this is not a URL",
                "",
                "/uri",
                "/preferredLabel/string",
                solrClient,
                catalogues
                );

        // Given

        // When
        assertThrows(
            KeywordVocabularyException.class,
            () -> vocabularyService.retrieve()
        );

        // Then
        verify(solrClient).deleteByQuery(COLLECTION, "vocabId:" + VOCABULARY_ID);
        verify(solrClient, never()).commit(COLLECTION);
    }

    @Test
    void getCatalogueIds() {
        // Setup
        String endpointUrl = getClass().getResource(CONCEPTS_JSON).toString();
        val catalogues = Arrays.asList("test-0", "test-1");

        vocabularyService = new HttpKeywordVocabulary(
                VOCABULARY_ID,
                VOCABULARY_NAME,
                endpointUrl,
                "",
                "/uri",
                "/preferredLabel/string",
                solrClient,
                catalogues
                );


        // Given

        // When
        assertTrue(vocabularyService.usedInCatalogue("test-0"));
        assertFalse(vocabularyService.usedInCatalogue("not"));

        // Then
    }
}
