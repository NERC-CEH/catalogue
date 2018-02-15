package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import org.apache.solr.client.solrj.response.UpdateResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

public class SolrIndexingServiceTest {
    
    @Mock BundledReaderService<GeminiDocument> reader;
    @Mock DocumentListingService listingService;
    @Mock DataRepository<?> repo;
    @Mock IndexGenerator<GeminiDocument, SolrIndex> indexGenerator;
    @Mock SolrServer solrServer;
    @Mock JenaLookupService lookupService;
    @Mock DocumentIdentifierService identifierService;
    
    private SolrIndexingService service;
    
    @Before
    public void createSolrIndexGenerator() {
        MockitoAnnotations.initMocks(this);
        service = spy(new SolrIndexingService(
            reader,
            listingService,
            repo,
            indexGenerator,
            solrServer,
            lookupService,
            identifierService
        ));
    }
    
    @Test
    public void checkThatReBuildingIndexRemovesEntireIndexFromSolr() throws DocumentIndexingException, SolrServerException, IOException {
        //Given
        DataRevision revision = mock(DataRevision.class);
        when(repo.getLatestRevision()).thenReturn(revision);
        
        //When
        service.rebuildIndex();
        
        //Then
        verify(solrServer).deleteByQuery("*:*");
        verify(solrServer).commit();
    }
    
    @Test
    public void checkThatReIndexingIndexesAllFiles() throws DocumentIndexingException, SolrServerException, IOException, UnknownContentTypeException {
        //Given
        String revId = "Latest";
        List<String> documents = Arrays.asList("doc1", "doc2");
        
        when(listingService.filterFilenames(any(List.class))).thenReturn(documents);
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn(revId);
        when(repo.getLatestRevision()).thenReturn(revision);
        
        doNothing().when(service).indexDocuments(any(List.class), eq(revId));
        
        //When
        service.rebuildIndex();
        
        //Then
        verify(service).indexDocuments(documents, revId);
    }
    
    @Test
    public void checkThatIndexesAllSpecifiedFiles() throws DocumentIndexingException, SolrServerException, IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String revId = "Latest";
        List<String> documents = Arrays.asList("doc1", "doc2");
        
        when(listingService.filterFilenames(any(List.class))).thenReturn(documents);
        
        GeminiDocument document1 = mock(GeminiDocument.class);
        GeminiDocument document2 = mock(GeminiDocument.class);
        when(reader.readBundle("doc1")).thenReturn(document1);
        when(reader.readBundle("doc2")).thenReturn(document2);
        
        SolrIndex document1Index = mock(SolrIndex.class);
        SolrIndex document2Index = mock(SolrIndex.class);
        when(indexGenerator.generateIndex(document1)).thenReturn(document1Index);
        when(indexGenerator.generateIndex(document2)).thenReturn(document2Index);
        
        //When
        service.indexDocuments(documents, revId);
        
        //Then
        verify(solrServer).addBean(document1Index);
        verify(solrServer).addBean(document2Index);
        verify(solrServer).commit();
    }
    
    @Test
    public void checkThatContinuesToIndexIfOneDocumentFails() throws DocumentIndexingException, SolrServerException, IOException, UnknownContentTypeException {
        //Given
        String revId = "Latest";
        List<String> documents = Arrays.asList("doc1", "doc2");
        when(solrServer.addBean(any(Object.class))).thenThrow(new SolrServerException("Please carry on"))
                                                   .thenReturn(new UpdateResponse());
        
        //When
        try {
            service.indexDocuments(documents, revId);
        }
        catch(DocumentIndexingException ex) {}
        
        //Then
        verify(solrServer).commit();
    }
    
    @Test(expected=DocumentIndexingException.class)
    public void checkThatExceptionIsThrownIfDocumentFailsToIndex() throws DocumentIndexingException, SolrServerException, IOException, UnknownContentTypeException {
        //Given
        String revId = "Latest";
        List<String> documents = Arrays.asList("doc1", "doc2");

        when(solrServer.addBean(any())).thenThrow(new SolrServerException("Please carry on"))
                                                   .thenReturn(null);
        
        //When
        service.indexDocuments(documents, revId);
        
        //Then
        fail("Expected to fail with a DocumentIndexingException");
    }
    
    @Test
    public void checkThatCanRemoveIndexForSpecificDocuments() throws DocumentIndexingException, SolrServerException, IOException, UnknownContentTypeException {
        //Given
        List<String> documents = Arrays.asList("doc1", "doc2", "doc3");

        
        //When
        service.unindexDocuments(documents);
        
        //Then
        verify(solrServer).deleteById(documents);
        verify(solrServer).commit();
    }
    
    @Test
    public void checkThatEmptySolrIndexResultsInEmptyService() throws SolrServerException, DocumentIndexingException {
        //Given
        QueryResponse queryResponse = mock(QueryResponse.class, RETURNS_DEEP_STUBS);
        when(queryResponse.getResults().isEmpty()).thenReturn(true);
        when(solrServer.query(any(SolrQuery.class))).thenReturn(queryResponse);
        
        //When
        boolean isEmpty = service.isIndexEmpty();
        
        //Then
        assertTrue("Expected the service to return empty", isEmpty);
    }
    
    @Test
    public void checkThatPopulatedSolrIndexResultsInPopulatedService() throws SolrServerException, DocumentIndexingException {
        //Given
        QueryResponse queryResponse = mock(QueryResponse.class, RETURNS_DEEP_STUBS);
        when(queryResponse.getResults().isEmpty()).thenReturn(false);
        when(solrServer.query(any(SolrQuery.class))).thenReturn(queryResponse);
        
        //When
        boolean isEmpty = service.isIndexEmpty();
        
        //Then
        assertFalse("Expected the service to not be empty", isEmpty);
    }
    
    @Test
    public void checkThatWeQueryForAllDocumentsWhenCheckingIfSolrIndexIsEmpty() throws SolrServerException, DocumentIndexingException  {
        //Given
        QueryResponse queryResponse = mock(QueryResponse.class, RETURNS_DEEP_STUBS);
        when(solrServer.query(any(SolrQuery.class))).thenReturn(queryResponse);
        
        //When
        service.isIndexEmpty();
        
        //Then
        ArgumentCaptor<SolrQuery> solrQuery = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer).query(solrQuery.capture());
        assertEquals("Expecte a wildcard query for the solr search", "*:*", solrQuery.getValue().getQuery());
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
