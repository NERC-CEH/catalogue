package uk.ac.ceh.gateway.catalogue.sparql;

import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SparqlVocabularyRetrieverTest {
    @Mock
    RestTemplate template;

    @Test
    public void successfullyRetrieveVocabulary() {
        //given
        SparqlQueryResponse.Uri uriDef = new SparqlQueryResponse.Uri();
        uriDef.setValue("http://example.com/abc/def");

        SparqlQueryResponse.Uri uriGhi = new SparqlQueryResponse.Uri();
        uriGhi.setValue("http://example.com/abc/ghi");

        SparqlQueryResponse.Binding bindingDef = new SparqlQueryResponse.Binding();
        bindingDef.setUri(uriDef);

        SparqlQueryResponse.Binding bindingGhi = new SparqlQueryResponse.Binding();
        bindingGhi.setUri(uriGhi);

        List<SparqlQueryResponse.Binding> bindings = Arrays.asList(bindingDef, bindingGhi);

        SparqlQueryResponse.Results results = new SparqlQueryResponse.Results();
        results.setBindings(bindings);

        SparqlQueryResponse sparqlQueryResponse = new SparqlQueryResponse();
        sparqlQueryResponse.setResults(results);

        String sparqlEndpoint = "https://example.com/sparql";
        VocabularyRetriever vocabularyRetriever = new SparqlVocabularyRetriever(this.template, sparqlEndpoint);

        given(template.getForEntity(any(URI.class), eq(SparqlQueryResponse.class)))
            .willReturn(ResponseEntity.ok(sparqlQueryResponse));

        //when
        Multimap<String, String> retrieve = vocabularyRetriever.retrieve();

        //then
        verify(template, times(VocabularyFacet.values().length)).getForEntity(any(URI.class), eq(SparqlQueryResponse.class));


        assertThat(retrieve.containsEntry("wp", "http://example.com/abc/def"), is(true));
        assertThat(retrieve.containsEntry("dt", "http://example.com/abc/ghi"), is(true));
        assertThat(retrieve.containsEntry("wp", "http://example.com/abc/xyz"), is(false));
        assertThat(retrieve.containsEntry("dt", "http://example.com/abc/xyz"), is(false));
        assertThat(retrieve.containsEntry("UnrealFacet", "http://example.com/abc/ghi"), is(false));
    }

    @Test
    public void unsuccessfulRetrieveDoesNotCauseException() {
        // Given
        SparqlQueryResponse sparqlQueryResponse = new SparqlQueryResponse();
        SparqlQueryResponse.Results results = new SparqlQueryResponse.Results();
        results.setBindings(Collections.emptyList());
        sparqlQueryResponse.setResults(results);

        String sparqlEndpoint = "https://example.com/sparql";
        VocabularyRetriever vocabularyRetriever = new SparqlVocabularyRetriever(this.template, sparqlEndpoint);

        given(template.getForEntity(any(URI.class), eq(SparqlQueryResponse.class)))
            .willReturn(ResponseEntity.ok(sparqlQueryResponse));

        // When
        Multimap<String, String> retrieve = vocabularyRetriever.retrieve();

        // Then
        verify(template, times(VocabularyFacet.values().length)).getForEntity(any(URI.class), eq(SparqlQueryResponse.class));

        assertThat(retrieve.isEmpty(), is(true));
    }

    @Test
    public void retrieveWithNullResponseBody() {
        //given
        String sparqlEndpoint = "https://example.com/sparql";
        VocabularyRetriever vocabularyRetriever = new SparqlVocabularyRetriever(this.template, sparqlEndpoint);

        given(template.getForEntity(any(URI.class), eq(SparqlQueryResponse.class)))
            .willReturn(ResponseEntity.ok(null));

        //when
        Multimap<String, String> retrieve = vocabularyRetriever.retrieve();

        //then
        verify(template, times(12)).getForEntity(any(URI.class), eq(SparqlQueryResponse.class));
        assertThat(retrieve.isEmpty(), is(true));
    }

    @Test
    public void querySelectorThrowsIllegalArgumentExceptionForTooFewArguments() {
        //given
        VocabularyFacet vocabularyFacet = VocabularyFacet.ASSIST_TOPICS;
        String sparqlEndpoint = "https://example.com/sparql";
        SparqlVocabularyRetriever vocabularyRetriever = new SparqlVocabularyRetriever(this.template, sparqlEndpoint);
        //then
        assertThrows(IllegalArgumentException.class, () -> {
            vocabularyRetriever.querySelector(vocabularyFacet);
        });
    }

    @Test
    public void querySelectorThrowsArrayIndexOutOfBoundsExceptionForTooManyArguments() {
        //given
        VocabularyFacet vocabularyFacet = VocabularyFacet.WATER_POLLUTANT;
        String sparqlEndpoint = "https://example.com/sparql";
        SparqlVocabularyRetriever vocabularyRetriever = new SparqlVocabularyRetriever(this.template, sparqlEndpoint);
        //then
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            vocabularyRetriever.querySelector(vocabularyFacet, "https://example.com", "vocab", "extra");
        });
    }

    @Test
    public void querySelectorReturnsValidUriWithOneArgument() {
        //given
        VocabularyFacet vocabularyFacet = VocabularyFacet.WATER_POLLUTANT;
        String sparqlEndpoint = "https://example.com";
        SparqlVocabularyRetriever vocabularyRetriever = new SparqlVocabularyRetriever(this.template, sparqlEndpoint);
        //when
        URI result = vocabularyRetriever.querySelector(vocabularyFacet, sparqlEndpoint);

        //then
        assertNotNull(result);
        assertTrue(result.toString().contains(sparqlEndpoint));
    }

    @Test
    public void querySelectorReturnsValidUriWithTwoArguments() {
        //given
        VocabularyFacet vocabularyFacet = VocabularyFacet.INMS_PROJECT;
        String sparqlEndpoint = "https://example.com";
        SparqlVocabularyRetriever vocabularyRetriever = new SparqlVocabularyRetriever(this.template, sparqlEndpoint);
        String vocab = "inms";

        //when
        URI result = vocabularyRetriever.querySelector(vocabularyFacet, sparqlEndpoint, vocab);

        //then
        assertNotNull(result);
        assertTrue(result.toString().contains(sparqlEndpoint));
        assertTrue(result.toString().contains(vocab));
    }

}
