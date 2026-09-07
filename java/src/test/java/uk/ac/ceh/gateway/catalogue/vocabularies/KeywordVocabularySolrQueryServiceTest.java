package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
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
import java.util.Optional;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
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

    /*
     * dri-one #321: promoting a bare dcterms:subject literal to a concept IRI is only
     * safe if the lookup is exact and unambiguous, so these pin down all four outcomes.
     * Note the exactness is enforced in Java, not by the Solr query: the keywords core
     * indexes `label` as an edge-ngrammed, lower-casing text field, so Solr can only ever
     * hand back candidates.
     */

    @Test
    @SneakyThrows
    void resolveExactLabelReturnsTheOneExactlyMatchingConcept() {
        //Given
        val response = mock(QueryResponse.class);
        given(solrClient.query(eq(COLLECTION), any(SolrQuery.class), eq(POST))).willReturn(response);
        given(response.getBeans(Keyword.class)).willReturn(List.of(
            new Keyword("Soil moisture", "GEMET", "http://www.eionet.europa.eu/gemet/concept/7842"),
            new Keyword("Soil moisture content", "GEMET", "http://www.eionet.europa.eu/gemet/concept/9999")
        ));

        //When
        val resolved = service.resolveExactLabel("Soil moisture");

        //Then
        assertThat(resolved.map(Keyword::getUrl), equalTo(Optional.of("http://www.eionet.europa.eu/gemet/concept/7842")));
    }

    @Test
    @SneakyThrows
    void resolveExactLabelIgnoresCandidatesDifferingOnlyInCase() {
        //Given
        val response = mock(QueryResponse.class);
        given(solrClient.query(eq(COLLECTION), any(SolrQuery.class), eq(POST))).willReturn(response);
        given(response.getBeans(Keyword.class)).willReturn(List.of(
            new Keyword("soil moisture", "GEMET", "http://www.eionet.europa.eu/gemet/concept/7842")
        ));

        //When
        val resolved = service.resolveExactLabel("Soil moisture");

        //Then
        assertThat(resolved, is(Optional.empty()));
    }

    @Test
    @SneakyThrows
    void resolveExactLabelRefusesToGuessBetweenTwoConceptsSharingALabel() {
        //Given: the same label in two vocabularies is not one concept, so nothing can be promoted
        val response = mock(QueryResponse.class);
        given(solrClient.query(eq(COLLECTION), any(SolrQuery.class), eq(POST))).willReturn(response);
        given(response.getBeans(Keyword.class)).willReturn(List.of(
            new Keyword("Soil moisture", "GEMET", "http://www.eionet.europa.eu/gemet/concept/7842"),
            new Keyword("Soil moisture", "NVS", "http://vocab.nerc.ac.uk/collection/P01/current/SOILM/")
        ));

        //When
        val resolved = service.resolveExactLabel("Soil moisture");

        //Then
        assertThat(resolved, is(Optional.empty()));
    }

    @Test
    @SneakyThrows
    void resolveExactLabelTreatsOneConceptIndexedTwiceAsUnambiguous() {
        //Given: the same concept URI reached through two vocabularies is still one concept
        val response = mock(QueryResponse.class);
        given(solrClient.query(eq(COLLECTION), any(SolrQuery.class), eq(POST))).willReturn(response);
        given(response.getBeans(Keyword.class)).willReturn(List.of(
            new Keyword("Soil moisture", "GEMET", "http://www.eionet.europa.eu/gemet/concept/7842"),
            new Keyword("Soil moisture", "CEHMD", "http://www.eionet.europa.eu/gemet/concept/7842")
        ));

        //When
        val resolved = service.resolveExactLabel("Soil moisture");

        //Then
        assertThat(resolved.map(Keyword::getUrl), equalTo(Optional.of("http://www.eionet.europa.eu/gemet/concept/7842")));
    }

    @Test
    @SneakyThrows
    void resolveExactLabelReturnsNothingWhenNoCandidateMatches() {
        //Given
        val response = mock(QueryResponse.class);
        given(solrClient.query(eq(COLLECTION), any(SolrQuery.class), eq(POST))).willReturn(response);
        given(response.getBeans(Keyword.class)).willReturn(List.of());

        //When
        val resolved = service.resolveExactLabel("Something nobody has catalogued");

        //Then
        assertThat(resolved, is(Optional.empty()));
    }

    @Test
    @SneakyThrows
    void resolveExactLabelDoesNotQuerySolrForTextThatCannotIdentifyAnything() {
        //When
        val resolved = service.resolveExactLabel("   ");

        //Then
        assertThat(resolved, is(Optional.empty()));
        verifyNoInteractions(solrClient);
    }

    @Test
    @SneakyThrows
    void resolveExactLabelIgnoresACandidateWithNoUrl() {
        //Given
        val response = mock(QueryResponse.class);
        given(solrClient.query(eq(COLLECTION), any(SolrQuery.class), eq(POST))).willReturn(response);
        given(response.getBeans(Keyword.class)).willReturn(List.of(
            new Keyword("Soil moisture", "GEMET", "")
        ));

        //When
        val resolved = service.resolveExactLabel("Soil moisture");

        //Then
        assertThat(resolved, is(Optional.empty()));
    }
}
