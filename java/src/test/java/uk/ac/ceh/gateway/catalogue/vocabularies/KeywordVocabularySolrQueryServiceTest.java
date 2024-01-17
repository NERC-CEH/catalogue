package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeywordVocabularySolrQueryServiceTest {

    public static final String LABEL_1 = "assist-topics";
    public static final String LABEL_2 = "label";
    public static final String VOCAB_ID_1 = "ASSIST-TOPICS";
    public static final String VOCAB_ID_2 = "vocab";
    public static final String URL_1 = "www.example.com";
    public static final String URL_2 = "www.example2.com";
    public static final String QUERY = "queryTest";
    private static final String COLLECTION = "keywords";

    @Mock
    private SolrClient solrClient;

    @InjectMocks
    private KeywordVocabularySolrQueryService service;

    @Test
    @SneakyThrows
    public void successfullyGetKeywords() {
        //Given
        val solrQuery = new SolrQuery();
        solrQuery.setQuery(QUERY);


        val response = mock(QueryResponse.class);

        val keywordSolrIndex1 = new Keyword(LABEL_1, VOCAB_ID_1, URL_1);
        val keywordSolrIndex2 = new Keyword(LABEL_2, VOCAB_ID_2, URL_2);

        given(solrClient.query(eq(COLLECTION), any(SolrQuery.class), eq(POST)))
                .willReturn(response);
        given(response.getBeans(Keyword.class))
                .willReturn(Arrays.asList(
                        keywordSolrIndex1,
                        keywordSolrIndex2
                ));
        List<String> vocabularyIds = new ArrayList<>();
        vocabularyIds.add(VOCAB_ID_1);
        vocabularyIds.add(VOCAB_ID_2);

        //When
        List<Keyword> result = service.query(QUERY, vocabularyIds);

        //Then
        assertThat(result, containsInAnyOrder(keywordSolrIndex1, keywordSolrIndex2));
    }


    @Test
    @SneakyThrows
    public void ThrowSolrServerException() {
        //Given
        when(solrClient.query(eq(COLLECTION), any(SolrParams.class), eq(POST))).thenThrow(new SolrServerException("Test"));

        //When
        List<String> vocabularyIds = new ArrayList<>();
        Assertions.assertThrows(SolrServerException.class, () ->
                service.query(QUERY,vocabularyIds)
        );
    }
}
