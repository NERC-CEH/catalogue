package uk.ac.ceh.gateway.catalogue.indexing.network;

import com.google.common.eventbus.Subscribe;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.repository.FacilityDeletedEvent;

import java.util.List;

@Slf4j
@ToString
public class NetworkFileEventListener {
    private final NetworkIndexingService service;
    private final DocumentListingService listingService;

    public NetworkFileEventListener(NetworkIndexingService service, DocumentListingService listingService) {
        this.service = service;
        this.listingService = listingService;
        log.info("Creating {}", this);
    }

    @Subscribe
    public void indexDocument(DataSubmittedEvent<?> event) throws DocumentIndexingException, DataRepositoryException {
        List<String> filenames = listingService.filterFilenamesEitherExtension(event.getFilenames());
        String revisionID = event.getDataRepository().getLatestRevision().getRevisionID();
        log.debug("About to index files: {} for revision: {}", filenames, revisionID);
        service.indexDocuments(filenames);
    }

    @Subscribe
    public void unindexDocument(FacilityDeletedEvent event) throws DocumentIndexingException {
        log.debug("About to unindex: {}", event.getFacilityId());
        service.unindexDocuments(event.getFacilityId(), event.getBelongToFilenames());
    }
}
