package uk.ac.ceh.gateway.catalogue.indexing;

import com.google.common.eventbus.Subscribe;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.components.datastore.DataDeletedEvent;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

@Slf4j
public class IndexingFileEventListener {
    private final DocumentIndexingService service;
    private final DocumentListingService listingService;

    public IndexingFileEventListener(DocumentIndexingService service,  DocumentListingService listingService) {
        this.service = service;
        this.listingService = listingService;
    }
    
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