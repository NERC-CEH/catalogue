package uk.ac.ceh.gateway.catalogue.sparql;

import com.google.common.collect.Multimap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SparqlVocabularyRetrieverTest {
    @Mock
    RestTemplate template;

    @Test
    public void successfullyRetrieveVocabulary() {
        //given
        SparqlVocabularyRetriever.Concept def = new SparqlVocabularyRetriever.Concept()
                .setConcept("http://example.com/abc/def")
                .setTopConcept("http://example.com/abc/top");
        SparqlVocabularyRetriever.Concept ghi =   new SparqlVocabularyRetriever.Concept()
                .setConcept("http://example.com/abc/ghi")
                .setTopConcept("http://example.com/abc/top");
        SparqlVocabularyRetriever.Concept[] concepts = {def, ghi};

        String sparqlEndpoint = "https://example.com/sparql";
        String graph = "urn:x-evn-pub:natCap";

        VocabularyRetriever vocabularyRetriever = new SparqlVocabularyRetriever(this.template);

        given(
            template.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                isNull(),
                eq(SparqlVocabularyRetriever.Concept[].class),
                eq(graph)
            )
        ).willReturn(ResponseEntity.ok(concepts));

        //when
        Multimap<String, String> retrieve = vocabularyRetriever.retrieve(sparqlEndpoint, graph);

        //then
        verify(template).exchange(any(String.class), eq(HttpMethod.GET), isNull(), eq(SparqlVocabularyRetriever.Concept[].class), eq(graph));
        assertThat(retrieve.containsEntry("http://example.com/abc/top", "http://example.com/abc/def"), is(true));
        assertThat(retrieve.containsEntry("http://example.com/abc/top", "http://example.com/abc/ghi"), is(true));
        assertThat(retrieve.containsEntry("http://example.com/abc/top", "http://example.com/abc/xyz"), is(false));
        assertThat(retrieve.containsEntry("http://example.com/zyx/top", "http://example.com/abc/xyz"), is(false));
    }
}
