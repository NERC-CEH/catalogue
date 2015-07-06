package uk.ac.ceh.gateway.catalogue.indexing;

import uk.ac.ceh.gateway.catalogue.indexing.IndexingFileEventListener;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

public class IndexingFileEventSubscriberTest {
    @Mock private SolrIndexingService<MetadataDocument> service;
    @Mock private DataSubmittedEvent<?> event;
    @Mock private DocumentListingService listingService;
    private IndexingFileEventListener eventSubscriber; 
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        eventSubscriber = new IndexingFileEventListener(service, listingService);
    }

    @Test
    public void indexFilesThatHaveDuplicateNames() throws DocumentIndexingException, DataRepositoryException {
        //Given
        String revisionId = "revision";
        List<String> originalFilenames = Arrays.asList("asd.raw", "asd.meta", "trd.meta");
        List<String> processedFilenames = Arrays.asList("asd", "trd");
        given(event.getFilenames()).willReturn(originalFilenames);
        given(listingService.filterFilenames(originalFilenames)).willReturn(processedFilenames);
        DataRepository repo = mock(DataRepository.class);
        DataRevision latestRevision = mock(DataRevision.class);
        given(latestRevision.getRevisionID()).willReturn(revisionId);
        given(repo.getLatestRevision()).willReturn(latestRevision);
        given(event.getDataRepository()).willReturn(repo);
        
        //When
        eventSubscriber.indexDocument(event);
        
        //Then
        verify(service).indexDocuments(processedFilenames, revisionId);
    }
    
}
