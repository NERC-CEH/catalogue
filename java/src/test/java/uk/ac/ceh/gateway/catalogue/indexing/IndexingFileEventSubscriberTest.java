package uk.ac.ceh.gateway.catalogue.indexing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class IndexingFileEventSubscriberTest {
    @Mock
    private SolrIndexingService service;

    @Mock
    private DataSubmittedEvent<DataRepository<CatalogueUser>> event;

    @Mock
    private DocumentListingService listingService;

    @Mock
    private DataRepository<CatalogueUser> repo;

    @Mock
    private DataRevision<CatalogueUser> latestRevision;

    @InjectMocks
    private IndexingFileEventListener eventSubscriber;

    @Test
    public void indexFilesThatHaveDuplicateNames() throws DocumentIndexingException, DataRepositoryException {
        //Given
        String revisionId = "revision";
        List<String> originalFilenames = Arrays.asList("asd.raw", "asd.meta", "trd.meta");
        List<String> processedFilenames = Arrays.asList("asd", "trd");
        given(event.getFilenames()).willReturn(originalFilenames);
        given(listingService.filterFilenamesEitherExtension(originalFilenames)).willReturn(processedFilenames);
        given(latestRevision.getRevisionID()).willReturn(revisionId);
        given(repo.getLatestRevision()).willReturn(latestRevision);
        given(event.getDataRepository()).willReturn(repo);

        //When
        eventSubscriber.indexDocument(event);

        //Then
        verify(service).indexDocuments(processedFilenames, revisionId);
    }

}
