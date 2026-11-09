package uk.ac.ceh.gateway.catalogue.services;

import org.apache.solr.client.solrj.RemoteSolrException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.ac.ceh.gateway.catalogue.model.ResourceIdentifierLookupException;

import java.io.IOException;
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

    /**
     * A failed lookup says nothing about whether the identifier exists. Reporting it as "no match"
     * made the resolver redirect to a URL that 404s and let the uniqueness check pass a duplicate
     * unchecked (dri-one #522).
     */
    @Test
    void resolveReportsSolrFailureRatherThanNoMatch() throws Exception {
        when(solrClient.query(eq("documents"), any())).thenThrow(new SolrServerException("Solr down"));

        ResourceIdentifierLookupException ex = assertThrows(
            ResourceIdentifierLookupException.class,
            () -> service.resolveToUuid("eidc:error-case")
        );
        assertInstanceOf(SolrServerException.class, ex.getCause());
    }

    @Test
    void findOwnersReportsSolrFailureRatherThanNoOwners() throws Exception {
        when(solrClient.query(eq("documents"), any())).thenThrow(new IOException("connection refused"));

        assertThrows(
            ResourceIdentifierLookupException.class,
            () -> service.findDocumentIdsByRi("doi:10.5285/abc")
        );
    }

    @Test
    void findOwnersReportsSolrErrorResponseRatherThanNoOwners() throws Exception {
        when(solrClient.query(eq("documents"), any())).thenThrow(new RemoteSolrException("http://solr", 503, "unavailable", null));

        assertThrows(
            ResourceIdentifierLookupException.class,
            () -> service.findDocumentIdsByRi("doi:10.5285/abc")
        );
    }

    @Test
    void failureMessageDoesNotLeakSolrDetails() throws Exception {
        when(solrClient.query(eq("documents"), any())).thenThrow(new SolrServerException("http://solr:8983/solr/documents secret"));

        ResourceIdentifierLookupException ex = assertThrows(
            ResourceIdentifierLookupException.class,
            () -> service.findDocumentIdsByRi("doi:10.5285/abc")
        );
        assertFalse(ex.getMessage().contains("solr:8983"), "The message reaches the client; the cause is only logged");
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

    @Test
    void excludesLinkDocumentsFromOwnership() throws Exception {
        QueryResponse response = mock(QueryResponse.class);
        when(response.getResults()).thenReturn(new SolrDocumentList());
        when(solrClient.query(eq("documents"), any())).thenReturn(response);

        service.findDocumentIdsByRi("doi:10.5285/abc");

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(eq("documents"), captor.capture());
        assertTrue(
            captor.getValue().get("q").contains("-documentType:LINK_DOCUMENT"),
            "Link documents mirror an original's resourceIdentifier, so they must be "
                + "excluded from ownership to avoid false duplicate-id conflicts"
        );
    }

    private SolrDocument docWithIdentifier(String id) {
        SolrDocument doc = new SolrDocument();
        doc.setField("identifier", id);
        return doc;
    }
}
