package uk.ac.ceh.gateway.catalogue.config;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataSubmittedEvent;
import uk.ac.ceh.gateway.catalogue.model.User;

/**
 *
 * @author cjohn
 */
public class EventWiring {
    @Autowired EventBus bus;
    
    @Subscribe
    public void indexNewFile(DataSubmittedEvent<DataRepository<User>> event) {
        DataRepository<User> repo = event.getDataRepository();
        
        event.getFilenames()
             .stream()
             .filter(f -> f.endsWith("json"))
             .forEach(f -> indexFile(f, repo));
    }
    
    public void indexFile(String filename, DataRepository<User> repo) {
        try {
            repo.getData(filename);
        } catch (DataRepositoryException ex) {
            bus.post(ex);
        }
    }
}
