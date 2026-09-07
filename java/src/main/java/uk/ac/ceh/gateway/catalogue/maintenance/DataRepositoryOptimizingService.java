package uk.ac.ceh.gateway.catalogue.maintenance;

import lombok.ToString;
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
@ToString
public class DataRepositoryOptimizingService {
    private final DataRepository<CatalogueUser> repo;
    private Date lastOptimized;

    public DataRepositoryOptimizingService(DataRepository<CatalogueUser> repo) {
        this.repo = repo;
        log.info("Creating");
    }

    /**
     * Runs a git {@code gc} on the datastore. gc repacks and <em>prunes loose objects</em>; when it
     * runs against the shared SMB-mounted datastore while requests are reading loose objects, a
     * reader can open an object as gc removes it and fail with {@code FileNotFoundException ...
     * (Resource busy)} (EBUSY), surfacing to users as errors.
     *
     * <p>The schedule is therefore externalised via {@code data.repository.optimize.cron} (default
     * midnight). Set it to {@code -} (Spring's disabled-cron marker) in environments where the
     * datastore is a shared mount, and run gc as a separate maintenance job off the live path. The
     * manual trigger on {@code MaintenanceController} is unaffected either way.</p>
     */
    @Scheduled(cron = "${data.repository.optimize.cron:0 0 0 * * ?}")
    public void performOptimization() throws DataRepositoryException {
        if(repo instanceof GitDataRepository) {
            GitDataRepository<CatalogueUser> gitRepo =  (GitDataRepository<CatalogueUser>) repo;
            log.info("DataRepository Optimization Start");
            gitRepo.optimize();
            this.lastOptimized = Calendar.getInstance().getTime();
        }
        else {
            log.info("Ignoring request to optimize: Not using a git repository");
        }
    }

    public Date getLastOptimized() {
        if (this.lastOptimized != null) {
            return new Date(this.lastOptimized.getTime());
        } else {
            return null;
        }
    }

    public DataRevision<CatalogueUser> getLatestRevision() throws DataRepositoryException {
        return this.repo.getLatestRevision();
    }
}
