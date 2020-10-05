package uk.ac.ceh.gateway.catalogue.config;

import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.IndexingFileEventListener;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

import javax.annotation.PostConstruct;

@Configuration
public class EventWiring {
    @SuppressWarnings("UnstableApiUsage")
    @Autowired EventBus bus;
    
    @Autowired @Qualifier("solr-index") DocumentIndexingService solrIndex;
    @Autowired @Qualifier("jena-index") DocumentIndexingService linkIndex;
    @Autowired @Qualifier("datacite-index") DocumentIndexingService dataciteIndex;
    @Autowired @Qualifier("validation-index") DocumentIndexingService validationIndex;
    @Autowired @Qualifier("mapserver-index") DocumentIndexingService mapserverIndex;
    @Autowired DocumentListingService listing;
    
    @SuppressWarnings("UnstableApiUsage")
    @PostConstruct
    public void addEventListeners() {
        bus.register(new IndexingFileEventListener(solrIndex, listing));
        bus.register(new IndexingFileEventListener(linkIndex, listing));
        bus.register(new IndexingFileEventListener(dataciteIndex, listing));
        bus.register(new IndexingFileEventListener(validationIndex, listing));
        bus.register(new IndexingFileEventListener(mapserverIndex, listing));
    }
}
