package uk.ac.ceh.gateway.catalogue.config;

import com.google.common.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.IndexingFileEventListener;
import uk.ac.ceh.gateway.catalogue.indexing.network.NetworkFileEventListener;
import uk.ac.ceh.gateway.catalogue.indexing.network.NetworkIndexingService;


@SuppressWarnings("UnstableApiUsage")
@Configuration
public class EventWiring {
    @SuppressWarnings("UnstableApiUsage")
    private final EventBus bus;
    private final DocumentIndexingService solrIndex;
    private final DocumentIndexingService linkIndex;
    private final DocumentIndexingService dataciteIndex;
    private final DocumentIndexingService validationIndex;
    private final DocumentIndexingService mapserverIndex;
    private final NetworkIndexingService networkIndex;
    private final DocumentListingService listing;

    public EventWiring(
        EventBus bus,
        @Qualifier("solr-index") DocumentIndexingService solrIndex,
        @Qualifier("jena-index") DocumentIndexingService linkIndex,
        @Qualifier("datacite-index") DocumentIndexingService dataciteIndex,
        @Qualifier("validation-index") DocumentIndexingService validationIndex,
        @Qualifier("mapserver-index") DocumentIndexingService mapserverIndex,
        @Qualifier("network-index") NetworkIndexingService networkIndex,
        DocumentListingService listing
    ) {
        this.bus = bus;
        this.solrIndex = solrIndex;
        this.linkIndex = linkIndex;
        this.dataciteIndex = dataciteIndex;
        this.validationIndex = validationIndex;
        this.mapserverIndex = mapserverIndex;
        this.networkIndex = networkIndex;
        this.listing = listing;
    }

    @SuppressWarnings("UnstableApiUsage")
    @PostConstruct
    public void addEventListeners() {
        bus.register(new IndexingFileEventListener(solrIndex, listing));
        bus.register(new IndexingFileEventListener(linkIndex, listing));
        bus.register(new IndexingFileEventListener(dataciteIndex, listing));
        bus.register(new IndexingFileEventListener(validationIndex, listing));
        bus.register(new IndexingFileEventListener(mapserverIndex, listing));
        bus.register(new NetworkFileEventListener(networkIndex, listing));
    }
}
