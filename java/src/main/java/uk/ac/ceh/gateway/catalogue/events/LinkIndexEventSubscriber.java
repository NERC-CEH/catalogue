package uk.ac.ceh.gateway.catalogue.events;

import com.google.common.eventbus.Subscribe;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ceh.components.datastore.DataDeletedEvent;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;

@Component
@Subscriber
public class LinkIndexEventSubscriber {
    private final DocumentLinkService service;

    @Autowired
    public LinkIndexEventSubscriber(DocumentLinkService service) {
      this.service = service;
    }
        
    @Subscribe
    public void indexLinks(DataSubmittedEvent<?> event) throws DocumentLinkingException {       
      service.linkDocuments(event.getFilenames()
          .stream()
          .map(f -> f.substring(0, f.lastIndexOf(".")))
          .collect(Collectors.toSet()));
    }
    
    @Subscribe
    public void unindexLinks(DataDeletedEvent<?> event) throws DocumentLinkingException {
        service.rebuildLinks();
    }
}
