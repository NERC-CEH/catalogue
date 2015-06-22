package uk.ac.ceh.gateway.catalogue.events;

import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ceh.components.datastore.DataDeletedEvent;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

@Component
@Subscriber
public class LinkIndexEventSubscriber {
    private final DocumentLinkService service;
    private final DocumentListingService listingService;

    @Autowired
    public LinkIndexEventSubscriber(DocumentLinkService service, DocumentListingService listingService) {
      this.service = service;
      this.listingService = listingService;
    }
        
    @Subscribe
    public void indexLinks(DataSubmittedEvent<?> event) throws DocumentLinkingException {       
        service.linkDocuments(listingService.filterFilenames(event.getFilenames()));
    }
    
    @Subscribe
    public void unindexLinks(DataDeletedEvent<?> event) throws DocumentLinkingException {
        service.rebuildLinks();
    }
}