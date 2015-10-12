package uk.ac.ceh.gateway.catalogue.config;

import com.google.common.eventbus.EventBus;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.IndexingFileEventListener;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

/**
 *
 * @author cjohn
 */
@Configuration
public class EventWiring {
    @Autowired EventBus bus;
    
    @Autowired @Qualifier("solr-index") DocumentIndexingService solrIndex;
    @Autowired @Qualifier("jena-index") DocumentIndexingService linkIndex;
    @Autowired @Qualifier("datacite-index") DocumentIndexingService dataciteIndex;
    @Autowired DocumentListingService listing;
    
    @PostConstruct
    public void addEventListeners() {
        bus.register(new IndexingFileEventListener(solrIndex, listing));
        bus.register(new IndexingFileEventListener(linkIndex, listing));
        bus.register(new IndexingFileEventListener(dataciteIndex, listing));
    }
}
