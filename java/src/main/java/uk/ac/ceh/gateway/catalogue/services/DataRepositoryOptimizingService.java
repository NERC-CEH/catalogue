package uk.ac.ceh.gateway.catalogue.services;

import java.util.Calendar;
import java.util.Date;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Slf4j
@Data
public class DataRepositoryOptimizingService {
    private final DataRepository<CatalogueUser> repo;
    private Date lastOptimized;
    
    @Scheduled(cron="0 0 0 * * ?")
    public void performOptimization() throws DataRepositoryException {
        if(repo instanceof GitDataRepository) {
            log.info("DataRepository Optimization Start");
            ((GitDataRepository)repo).optimize();
            this.lastOptimized = Calendar.getInstance().getTime();
        }
        else {
            log.info("Ignoring request to optimize: Not using a git repository");
        }
    }
}
