package uk.ac.ceh.gateway.catalogue.events;

import com.google.common.eventbus.Subscribe;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ceh.components.datastore.DataDeletedEvent;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

@Component
@Subscriber
@Slf4j
public class SolrIndexFileEventSubscriber {
    private final SolrIndexingService<MetadataDocument> service;

    @Autowired
    public SolrIndexFileEventSubscriber(SolrIndexingService<MetadataDocument> service) {
        this.service = service;
    }
    
    @Subscribe
    public void indexDocument(DataSubmittedEvent<?> event) throws DocumentIndexingException, DataRepositoryException {
        List<String> filenames = rawFilenamesOnly(event.getFilenames());
        String revisionID = event.getDataRepository().getLatestRevision().getRevisionID();
        log.debug("About to index files: {} for revision: {}", filenames, revisionID);
        service.indexDocuments(filenames, revisionID);
    }
    
    @Subscribe
    public void unindexDocument(DataDeletedEvent<?> event) throws DocumentIndexingException {
        List<String> filenames = rawFilenamesOnly(event.getFilenames());
        log.debug("About to unindex files: {}", filenames);
        service.unindexDocuments(filenames);
    }
    
    private List<String> rawFilenamesOnly(Collection<String> orig) {
        return orig
            .stream()
            .filter(f -> f.endsWith(".raw"))
            .map(f -> f.substring(0, f.lastIndexOf(".")))
            .collect(Collectors.toList());
    }
}