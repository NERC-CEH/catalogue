package uk.ac.ceh.gateway.catalogue.config;

import com.google.common.eventbus.EventBus;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.gateway.catalogue.indexing.IndexingFileEventListener;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

/**
 *
 * @author cjohn
 */
public class EventWiring {
    @Autowired EventBus bus;
    
    @Autowired SolrIndexingService solrIndex;
    @Autowired JenaIndexingService linkIndex;
    @Autowired DocumentListingService listing;
    
    @PostConstruct
    public void addEventListeners() {
        bus.register(new IndexingFileEventListener(solrIndex, listing));
        bus.register(new IndexingFileEventListener(linkIndex, listing));
    }
}
