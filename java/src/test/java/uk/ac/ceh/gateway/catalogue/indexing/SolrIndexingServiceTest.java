package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.permission.CrowdPermissionServiceTest;
import uk.ac.ceh.gateway.catalogue.services.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("SolrIndexing")
@ExtendWith(MockitoExtension.class)
class SolrIndexingServiceTest {

    @Mock private BundledReaderService<GeminiDocument> reader;
    @Mock private DocumentListingService listingService;
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private IndexGenerator<GeminiDocument, SolrIndex> indexGenerator;
    @Mock private SolrClient solrClient;
    @Mock private JenaLookupService lookupService;
    @Mock private DocumentIdentifierService identifierService;
    private static final String COLLECTION = "documents";

    @InjectMocks
    private SolrIndexingService<MetadataDocument> service;

    @Test
    @DisplayName("re-indexing indexes all files")
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void checkThatReIndexingIndexesAllFiles() {
        //Given
        val revId = "Latest";
        given(repo.getLatestRevision()).willReturn(new CrowdPermissionServiceTest.DummyRevision(revId));

        val documents = Arrays.asList("doc1", "doc2");
        given(listingService.filterFilenames(any(List.class))).willReturn(documents);

        val doc = new GeminiDocument();
        given(reader.readBundle(anyString())).willReturn(doc);

        val solr = new SolrIndex();
        given(indexGenerator.generateIndex(doc)).willReturn(solr);

        //When
        service.rebuildIndex();

        //Then
        verify(solrClient).deleteByQuery(COLLECTION, "*:*");
        verify(solrClient, times(2)).addBean(COLLECTION, solr);
        verify(solrClient).commit(COLLECTION);
    }

    @Test
    @SneakyThrows
    void checkThatIndexesAllSpecifiedFiles() {
        //Given
        String revId = "Latest";
        List<String> documents = Arrays.asList("doc1", "doc2");

        GeminiDocument document1 = new GeminiDocument();
        GeminiDocument document2 = new GeminiDocument();
        when(reader.readBundle("doc1")).thenReturn(document1);
        when(reader.readBundle("doc2")).thenReturn(document2);

        SolrIndex document1Index = new SolrIndex();
        SolrIndex document2Index = new SolrIndex();
        when(indexGenerator.generateIndex(any(GeminiDocument.class)))
            .thenReturn(document1Index, document2Index);

        //When
        service.indexDocuments(documents, revId);

        //Then
        verify(solrClient, times(2)).addBean(eq(COLLECTION), any(SolrIndex.class));
        verify(solrClient).commit(COLLECTION);
    }

    @Test
    @SneakyThrows
    public void checkThatContinuesToIndexIfOneDocumentFails() {
        //Given
        String revId = "Latest";
        List<String> documents = Arrays.asList("doc1", "doc2");
        when(solrClient.addBean(any(Object.class))).thenThrow(new SolrServerException("Please carry on"))
                                                   .thenReturn(new UpdateResponse());

        //When
        try {
            service.indexDocuments(documents, revId);
        }
        catch(DocumentIndexingException ignored) {}

        //Then
        verify(solrClient).commit(COLLECTION);
    }

    @Test
    @SneakyThrows
    public void checkThatExceptionIsThrownIfDocumentFailsToIndex() {
        Assertions.assertThrows(DocumentIndexingException.class, () -> {
            //Given
            String revId = "Latest";
            List<String> documents = Arrays.asList("doc1", "doc2");

            when(solrClient.addBean(eq(COLLECTION), any(SolrIndex.class))).thenThrow(new SolrServerException("Please carry on"))
                    .thenReturn(null);

            //When
            service.indexDocuments(documents, revId);

            //Then
            fail("Expected to fail with a DocumentIndexingException");
        });
    }

    @Test
    public void checkThatCanRemoveIndexForSpecificDocuments() throws DocumentIndexingException, SolrServerException, IOException, UnknownContentTypeException {
        //Given
        List<String> documents = Arrays.asList("doc1", "doc2", "doc3");


        //When
        service.unindexDocuments(documents);

        //Then
        verify(solrClient).deleteById(COLLECTION, documents);
        verify(solrClient).commit(COLLECTION);
    }

    @Test
    @SneakyThrows
    public void checkThatEmptySolrIndexResultsInEmptyService() {
        //Given
        QueryResponse queryResponse = mock(QueryResponse.class, RETURNS_DEEP_STUBS);
        when(queryResponse.getResults().isEmpty()).thenReturn(true);
        when(solrClient.query(eq(COLLECTION), any(SolrQuery.class))).thenReturn(queryResponse);

        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertTrue(isEmpty);
    }

    @Test
    @SneakyThrows
    public void checkThatPopulatedSolrIndexResultsInPopulatedService() {
        //Given
        QueryResponse queryResponse = mock(QueryResponse.class, RETURNS_DEEP_STUBS);
        when(queryResponse.getResults().isEmpty()).thenReturn(false);
        when(solrClient.query(eq(COLLECTION), any(SolrQuery.class))).thenReturn(queryResponse);

        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertFalse(isEmpty);
    }

    @Test
    @SneakyThrows
    public void checkThatWeQueryForAllDocumentsWhenCheckingIfSolrIndexIsEmpty() {
        //Given
        QueryResponse queryResponse = mock(QueryResponse.class, RETURNS_DEEP_STUBS);
        when(solrClient.query(eq(COLLECTION),any(SolrQuery.class))).thenReturn(queryResponse);

        //When
        service.isIndexEmpty();

        //Then
        ArgumentCaptor<SolrQuery> solrQuery = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrClient).query(eq(COLLECTION), solrQuery.capture());
        assertEquals("*:*", solrQuery.getValue().getQuery());
    }

    @Test
    public void linkDocumentsGetReindexed() throws Exception {
        //given
        String master = "master";
        String revision = "latest";
        List<String> documents = Arrays.asList(master, "another");

        given(identifierService.generateUri(any(String.class))).willReturn("http://master", "http://another");

        //when
        service.indexDocuments(documents, revision);

        //then
        verify(lookupService).linked("http://master");
        verify(identifierService).generateUri(master);
    }
}
