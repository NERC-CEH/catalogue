package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@DisplayName("SolrScheduledReindexService")
@ExtendWith(MockitoExtension.class)
class SolrScheduledReindexServiceTest {
    @Mock private DocumentIndexingService documentIndexingService;
    @Mock private TaskScheduler taskScheduler;

    private SolrScheduledReindexService service;

    @BeforeEach
    void setup() {
        service = new SolrScheduledReindexService(documentIndexingService, taskScheduler);
    }

    @Test
    @DisplayName("scheduled reindex attempts indexing")
    @SneakyThrows
    void reindex() {
        service.reindex();

        verify(documentIndexingService).attemptIndexing();
    }

    @Test
    @DisplayName("scheduled reindex does not fire before Solr has had a chance to start")
    @SneakyThrows
    void scheduledReindexHasAnInitialDelay() {
        val scheduled = SolrScheduledReindexService.class
            .getDeclaredMethod("reindex")
            .getAnnotation(Scheduled.class);

        assertThat(scheduled.initialDelay()).isPositive();
    }

    @Test
    @DisplayName("checks the index shortly after startup")
    void schedulesFirstCheckAfterStartup() {
        service.verifyIndexOnStartup();

        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    @DisplayName("stops checking once Solr answers")
    void stopsRetryingOnceIndexIsVerified() {
        given(documentIndexingService.attemptIndexing()).willReturn(true);

        service.verifyIndexOnStartup();
        runScheduledTasks();

        verify(documentIndexingService).attemptIndexing();
        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    @DisplayName("retries with a longer delay each time Solr is unreachable")
    void retriesWithBackoffWhileSolrIsUnreachable() {
        given(documentIndexingService.attemptIndexing()).willReturn(false, false, true);

        service.verifyIndexOnStartup();
        runScheduledTasks();

        verify(documentIndexingService, times(3)).attemptIndexing();
        assertThat(scheduledDelays()).hasSize(3);
        assertThat(scheduledDelays().get(1)).isAfter(scheduledDelays().get(0));
        assertThat(scheduledDelays().get(2)).isAfter(scheduledDelays().get(1));
    }

    @Test
    @DisplayName("gives up after a bounded number of startup attempts")
    void givesUpAfterMaxAttempts() {
        given(documentIndexingService.attemptIndexing()).willReturn(false);

        service.verifyIndexOnStartup();
        runScheduledTasks();

        verify(documentIndexingService, times(SolrScheduledReindexService.MAX_STARTUP_ATTEMPTS))
            .attemptIndexing();
        verify(taskScheduler, times(SolrScheduledReindexService.MAX_STARTUP_ATTEMPTS))
            .schedule(any(Runnable.class), any(Instant.class));
        verifyNoMoreInteractions(taskScheduler);
    }

    @Test
    @DisplayName("never waits longer than the maximum retry delay")
    void backoffIsCapped() {
        assertThat(service.retryDelay(SolrScheduledReindexService.MAX_STARTUP_ATTEMPTS))
            .isEqualTo(SolrScheduledReindexService.MAX_RETRY_DELAY);
    }

    /**
     * The service reschedules itself, so drain every task the scheduler has been handed,
     * including the ones queued while draining.
     */
    private void runScheduledTasks() {
        int executed = 0;
        while (executed < scheduledTasks().size()) {
            scheduledTasks().get(executed++).run();
        }
    }

    private java.util.List<Runnable> scheduledTasks() {
        val task = ArgumentCaptor.forClass(Runnable.class);
        verify(taskScheduler, org.mockito.Mockito.atLeast(0)).schedule(task.capture(), any(Instant.class));
        return task.getAllValues();
    }

    private java.util.List<Instant> scheduledDelays() {
        val start = ArgumentCaptor.forClass(Instant.class);
        verify(taskScheduler, org.mockito.Mockito.atLeast(0)).schedule(any(Runnable.class), start.capture());
        return start.getAllValues();
    }
}
