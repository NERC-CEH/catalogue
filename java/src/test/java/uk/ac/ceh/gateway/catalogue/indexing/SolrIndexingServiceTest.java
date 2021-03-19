package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.SneakyThrows;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.services.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class SolrIndexingServiceTest {
    
    @Mock BundledReaderService<GeminiDocument> reader;
    @Mock DocumentListingService listingService;
    @Mock DataRepository<?> repo;
    @Mock IndexGenerator<GeminiDocument, SolrIndex> indexGenerator;
    @Mock SolrClient solrClient;
    @Mock JenaLookupService lookupService;
    @Mock DocumentIdentifierService identifierService;
    
    private SolrIndexingService service;
    
    @BeforeEach
    public void createSolrIndexGenerator() {
        MockitoAnnotations.initMocks(this);
        service = spy(new SolrIndexingService(
            reader,
            listingService,
            repo,
            indexGenerator,
            solrClient,
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
        verify(solrClient).deleteByQuery("*:*");
        verify(solrClient).commit();
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
        verify(solrClient).addBean(document1Index);
        verify(solrClient).addBean(document2Index);
        verify(solrClient).commit();
    }
    
    @Test
    public void checkThatContinuesToIndexIfOneDocumentFails() throws DocumentIndexingException, SolrServerException, IOException, UnknownContentTypeException {
        //Given
        String revId = "Latest";
        List<String> documents = Arrays.asList("doc1", "doc2");
        when(solrClient.addBean(any(Object.class))).thenThrow(new SolrServerException("Please carry on"))
                                                   .thenReturn(new UpdateResponse());
        
        //When
        try {
            service.indexDocuments(documents, revId);
        }
        catch(DocumentIndexingException ex) {}
        
        //Then
        verify(solrClient).commit();
    }
    
    @Test
    public void checkThatExceptionIsThrownIfDocumentFailsToIndex() throws DocumentIndexingException, SolrServerException, IOException, UnknownContentTypeException {
        Assertions.assertThrows(DocumentIndexingException.class, () -> {
            //Given
            String revId = "Latest";
            List<String> documents = Arrays.asList("doc1", "doc2");

            when(solrClient.addBean(any())).thenThrow(new SolrServerException("Please carry on"))
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
        verify(solrClient).deleteById(documents);
        verify(solrClient).commit();
    }
    
    @Test
    @SneakyThrows
    public void checkThatEmptySolrIndexResultsInEmptyService() {
        //Given
        QueryResponse queryResponse = mock(QueryResponse.class, RETURNS_DEEP_STUBS);
        when(queryResponse.getResults().isEmpty()).thenReturn(true);
        when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        
        //When
        boolean isEmpty = service.isIndexEmpty();
        
        //Then
        assertTrue("Expected the service to return empty", isEmpty);
    }
    
    @Test
    @SneakyThrows
    public void checkThatPopulatedSolrIndexResultsInPopulatedService() {
        //Given
        QueryResponse queryResponse = mock(QueryResponse.class, RETURNS_DEEP_STUBS);
        when(queryResponse.getResults().isEmpty()).thenReturn(false);
        when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        
        //When
        boolean isEmpty = service.isIndexEmpty();
        
        //Then
        assertFalse("Expected the service to not be empty", isEmpty);
    }
    
    @Test
    @SneakyThrows
    public void checkThatWeQueryForAllDocumentsWhenCheckingIfSolrIndexIsEmpty() {
        //Given
        QueryResponse queryResponse = mock(QueryResponse.class, RETURNS_DEEP_STUBS);
        when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        
        //When
        service.isIndexEmpty();
        
        //Then
        ArgumentCaptor<SolrQuery> solrQuery = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrClient).query(solrQuery.capture());
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
