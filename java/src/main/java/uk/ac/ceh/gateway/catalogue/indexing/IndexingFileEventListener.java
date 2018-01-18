package uk.ac.ceh.gateway.catalogue.indexing;

import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.components.datastore.DataDeletedEvent;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class IndexingFileEventListener {
    private final DocumentIndexingService service;
    private final DocumentListingService listingService;
    
    @Subscribe
    public void indexDocument(DataSubmittedEvent<?> event) throws DocumentIndexingException, DataRepositoryException {
        List<String> filenames = listingService.filterFilenamesEitherExtension(event.getFilenames());
        String revisionID = event.getDataRepository().getLatestRevision().getRevisionID();
        log.debug("About to index files: {} for revision: {}", filenames, revisionID);
        service.indexDocuments(filenames, revisionID);
    }
    
    @Subscribe
    public void unindexDocument(DataDeletedEvent<?> event) throws DocumentIndexingException {
        List<String> filenames = listingService.filterFilenamesEitherExtension(event.getFilenames());
        log.debug("About to unindex files: {}", filenames);
        service.unindexDocuments(filenames);
    }
}