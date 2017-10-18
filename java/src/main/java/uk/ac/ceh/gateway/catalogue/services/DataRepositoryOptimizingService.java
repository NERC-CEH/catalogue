package uk.ac.ceh.gateway.catalogue.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Calendar;
import java.util.Date;

@Slf4j
@Service
@AllArgsConstructor
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

    public Date getLastOptimized() {
        return new Date(this.lastOptimized.getTime());
    }

    public DataRevision<CatalogueUser> getLatestRevision() throws DataRepositoryException {
        return this.repo.getLatestRevision();
    }
}
