package uk.ac.ceh.gateway.catalogue.events;

import com.google.common.eventbus.Subscribe;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ceh.components.datastore.DataDeletedEvent;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

@Component
@Subscriber
@Slf4j
public class SolrIndexFileEventSubscriber {
    private final SolrIndexingService<MetadataDocument> service;
    private final DocumentListingService listingService;

    @Autowired
    public SolrIndexFileEventSubscriber(SolrIndexingService<MetadataDocument> service,  DocumentListingService listingService) {
        this.service = service;
        this.listingService = listingService;
    }
    
    @Subscribe
    public void indexDocument(DataSubmittedEvent<?> event) throws DocumentIndexingException, DataRepositoryException {
        List<String> filenames = listingService.filterFilenames(event.getFilenames());
        String revisionID = event.getDataRepository().getLatestRevision().getRevisionID();
        log.debug("About to index files: {} for revision: {}", filenames, revisionID);
        service.indexDocuments(filenames, revisionID);
    }
    
    @Subscribe
    public void unindexDocument(DataDeletedEvent<?> event) throws DocumentIndexingException {
        List<String> filenames = listingService.filterFilenames(event.getFilenames());
        log.debug("About to unindex files: {}", filenames);
        service.unindexDocuments(filenames);
    }
}