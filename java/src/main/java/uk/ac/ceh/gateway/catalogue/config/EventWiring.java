package uk.ac.ceh.gateway.catalogue.config;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

/**
 *
 * @author cjohn
 */
public class EventWiring {
    @Autowired EventBus bus;
    
    @Subscribe
    public void indexNewFile(DataSubmittedEvent<DataRepository<CatalogueUser>> event) {
        DataRepository<CatalogueUser> repo = event.getDataRepository();
        
        event.getFilenames()
             .stream()
             .filter(f -> f.endsWith("json"))
             .forEach(f -> indexFile(f, repo));
    }
    
    public void indexFile(String filename, DataRepository<CatalogueUser> repo) {
        try {
            repo.getData(filename);
        } catch (DataRepositoryException ex) {
            bus.post(ex);
        }
    }
}
