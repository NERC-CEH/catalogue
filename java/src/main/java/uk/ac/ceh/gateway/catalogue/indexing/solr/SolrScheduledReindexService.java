package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Keeps the Solr index populated.
 * <p>
 * Solr runs in its own pod and is not necessarily up when this application starts: a release which
 * rolls the application before Solr leaves the index empty, and a single check made at startup will
 * simply fail against an unreachable Solr. The startup check is therefore retried with an increasing
 * delay until Solr answers, backed by a scheduled check which runs for the lifetime of the process.
 */
@Slf4j
@ToString
@Service
@Profile("!test")
public class SolrScheduledReindexService {
    static final Duration FIRST_RETRY_DELAY = Duration.ofSeconds(15);
    static final Duration MAX_RETRY_DELAY = Duration.ofMinutes(4);
    static final int MAX_STARTUP_ATTEMPTS = 8;

    private final DocumentIndexingService indexingService;
    private final TaskScheduler taskScheduler;

    public SolrScheduledReindexService(
            @Qualifier("solr-index") DocumentIndexingService indexingService,
            TaskScheduler taskScheduler
    ) {
        this.indexingService = indexingService;
        this.taskScheduler = taskScheduler;
        log.info("Creating");
    }

    @Scheduled(initialDelay = 1, fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    protected void reindex() {
        indexingService.attemptIndexing();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verifyIndexOnStartup() {
        scheduleStartupAttempt(1);
    }

    private void scheduleStartupAttempt(int attempt) {
        val delay = retryDelay(attempt);
        log.info("Checking the Solr index in {}s (attempt {} of {})", delay.toSeconds(), attempt, MAX_STARTUP_ATTEMPTS);
        taskScheduler.schedule(() -> startupAttempt(attempt), Instant.now().plus(delay));
    }

    private void startupAttempt(int attempt) {
        if (indexingService.attemptIndexing()) {
            log.info("Solr index verified on startup attempt {}", attempt);
            return;
        }
        if (attempt >= MAX_STARTUP_ATTEMPTS) {
            log.error(
                "Solr index could not be reached in {} startup attempts, so it may be empty. " +
                "The scheduled reindex will keep retrying every 30 minutes",
                MAX_STARTUP_ATTEMPTS
            );
            return;
        }
        scheduleStartupAttempt(attempt + 1);
    }

    /**
     * Doubles with every attempt, up to {@link #MAX_RETRY_DELAY}.
     */
    Duration retryDelay(int attempt) {
        val delay = FIRST_RETRY_DELAY.multipliedBy(1L << (attempt - 1));
        return delay.compareTo(MAX_RETRY_DELAY) > 0 ? MAX_RETRY_DELAY : delay;
    }
}
