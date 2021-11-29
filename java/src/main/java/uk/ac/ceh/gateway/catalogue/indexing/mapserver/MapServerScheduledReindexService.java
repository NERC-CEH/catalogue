package uk.ac.ceh.gateway.catalogue.indexing.mapserver;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;

import java.util.concurrent.TimeUnit;

@Slf4j
@ToString
@Service
public class MapServerScheduledReindexService {
    private final DocumentIndexingService indexingService;

    public MapServerScheduledReindexService(
            @Qualifier("mapserver-index") DocumentIndexingService indexingService
    ) {
        this.indexingService = indexingService;
        log.info("Creating");
    }

    @Scheduled(initialDelay = 3, fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    protected void reindex() {
        indexingService.attemptIndexing();
    }
}
