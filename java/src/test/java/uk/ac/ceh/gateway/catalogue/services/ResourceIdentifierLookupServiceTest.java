package uk.ac.ceh.gateway.catalogue.services;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ResourceIdentifierLookupServiceTest {

    private SolrClient solrClient;
    private ResourceIdentifierLookupService service;

    @BeforeEach
    void setup() {
        solrClient = mock(SolrClient.class);
        service = new ResourceIdentifierLookupService(solrClient);
    }

    @Test
    void returnsUuidWhenFound() throws Exception {
        String identifier = "eidc:my-test-code";
        String uuid = "b7567cab-2ecb-41ef-bac3-d37e71924ee2";

        // Build fake Solr response
        SolrDocumentList results = new SolrDocumentList();
        SolrDocument doc = new SolrDocument();
        doc.setField("identifier", uuid);
        results.add(doc);

        QueryResponse response = mock(QueryResponse.class);
        when(response.getResults()).thenReturn(results);

        when(solrClient.query(eq("documents"), any())).thenReturn(response);

        Optional<String> result = service.resolveToUuid(identifier);

        assertTrue(result.isPresent());
        assertEquals(uuid, result.get());
    }

    @Test
    void returnsEmptyWhenNotFound() throws Exception {
        String identifier = "eidc:not-found";

        SolrDocumentList empty = new SolrDocumentList();

        QueryResponse response = mock(QueryResponse.class);
        when(response.getResults()).thenReturn(empty);

        when(solrClient.query(eq("documents"), any())).thenReturn(response);

        Optional<String> result = service.resolveToUuid(identifier);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyOnSolrError() throws Exception {
        String identifier = "eidc:error-case";

        when(solrClient.query(eq("documents"), any())).thenThrow(new RuntimeException("Solr down"));

        Optional<String> result = service.resolveToUuid(identifier);

        assertTrue(result.isEmpty(), "Service should swallow Solr errors and return Optional.empty()");
    }

    @Test
    void findDocumentIdsByRiReturnsAllOwners() throws Exception {
        SolrDocumentList results = new SolrDocumentList();
        results.add(docWithIdentifier("id-one"));
        results.add(docWithIdentifier("id-two"));

        QueryResponse response = mock(QueryResponse.class);
        when(response.getResults()).thenReturn(results);
        when(solrClient.query(eq("documents"), any())).thenReturn(response);

        List<String> result = service.findDocumentIdsByRi("doi:10.5285/abc");

        assertEquals(List.of("id-one", "id-two"), result);
    }

    @Test
    void findDocumentIdsByRiReturnsEmptyWhenNoneMatch() throws Exception {
        QueryResponse response = mock(QueryResponse.class);
        when(response.getResults()).thenReturn(new SolrDocumentList());
        when(solrClient.query(eq("documents"), any())).thenReturn(response);

        assertTrue(service.findDocumentIdsByRi("doi:10.5285/none").isEmpty());
    }

    @Test
    void queriesTheResourceIdentifierField() throws Exception {
        QueryResponse response = mock(QueryResponse.class);
        when(response.getResults()).thenReturn(new SolrDocumentList());
        when(solrClient.query(eq("documents"), any())).thenReturn(response);

        service.findDocumentIdsByRi("doi:10.5285/abc");

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture());
        assertTrue(
            captor.getValue().get("q").startsWith("resourceIdentifier:"),
            "Uniqueness lookup must target the un-analyzed resourceIdentifier field"
        );
    }

    private SolrDocument docWithIdentifier(String id) {
        SolrDocument doc = new SolrDocument();
        doc.setField("identifier", id);
        return doc;
    }
}
