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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalKeywordVocabularyTest {
    private LocalKeywordVocabulary vocabularyService;

    private static final String COLLECTION = "keywords";
    private static final String VOCABULARY_ID = "testVocabId";
    private static final String VOCABULARY_NAME = "testVocabName";
    private static final String LOCAL_JSON_FILE = "concepts.json"; // Adjust to use the test file path

    @Mock
    private SolrClient solrClient;

    @Test
    @SneakyThrows
    public void testRetrieveLocalVocabulary() {
        // Setup
        String filePath = Objects.requireNonNull(getClass().getResource(LOCAL_JSON_FILE)).getFile();
        val catalogues = Arrays.asList("test-0", "test-1");

        vocabularyService = new LocalKeywordVocabulary(
            VOCABULARY_ID,
            VOCABULARY_NAME,
            filePath,
            "",
            "/uri",
            "/preferredLabel/string",
            solrClient,
            catalogues
        );

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
    public void failToCommunicateWithSolr() {
        // Setup
        String filePath = Objects.requireNonNull(getClass().getResource(LOCAL_JSON_FILE)).getFile();
        val catalogues = Arrays.asList("test-0", "test-1");

        vocabularyService = new LocalKeywordVocabulary(
            VOCABULARY_ID,
            VOCABULARY_NAME,
            filePath,
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
    void getCatalogueIds() {
        // Setup
        String filePath = Objects.requireNonNull(getClass().getResource(LOCAL_JSON_FILE)).getFile();
        val catalogues = Arrays.asList("test-0", "test-1");

        vocabularyService = new LocalKeywordVocabulary(
            VOCABULARY_ID,
            VOCABULARY_NAME,
            filePath,
            "",
            "/uri",
            "/preferredLabel/string",
            solrClient,
            catalogues
        );

        // When
        assertTrue(vocabularyService.usedInCatalogue("test-0"));
        assertFalse(vocabularyService.usedInCatalogue("not"));
    }
}

