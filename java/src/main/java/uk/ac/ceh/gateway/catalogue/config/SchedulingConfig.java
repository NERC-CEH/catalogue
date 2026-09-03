package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.task.ThreadPoolTaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customises the auto-configured {@code taskScheduler} beyond what
 * {@code spring.task.scheduling.*} can express.
 * <p>
 * Everything else about the scheduler is set in {@code application.properties} — pool size, and
 * whether a running task is awaited at shutdown — and the reasoning for those values is recorded
 * there and in {@code docs/scheduled-tasks.md}. This class exists for the one shutdown policy Boot
 * does not expose as a property.
 */
@Slf4j
@Configuration
public class SchedulingConfig {

    /**
     * Drops still-pending one-shot tasks when the scheduler shuts down.
     * <p>
     * {@code ScheduledThreadPoolExecutor} defaults to <em>keeping</em> delayed one-shot tasks after
     * {@code shutdown()} — only periodic tasks are cancelled — and it does not terminate until they
     * have run. That matters here because {@code spring.task.scheduling.shutdown.await-termination}
     * is true: the executor's shutdown blocks on termination, so one pending task with a delay
     * longer than the await period spends the whole budget waiting for something nobody is waiting
     * for. Measured on this JDK: with the default policy, {@code awaitTermination} against a pool
     * holding a single task due in 10s returns false after the full timeout; with this policy it
     * returns true immediately.
     * <p>
     * The tasks in question are the ones scheduled directly rather than by {@code @Scheduled}, which
     * today means {@code SolrScheduledReindexService}'s startup retry chain — up to four minutes
     * apart, and not registered with Spring's {@code ScheduledTaskRegistrar}, so nothing else
     * cancels them. Keeping them would also let a retry <em>start</em> a rebuild during shutdown,
     * which is the partial-index failure of dri-one #355 with extra steps.
     * <p>
     * A task already running is unaffected: this drops the queue, and awaiting termination is what
     * gives the running one time to finish.
     */
    @Bean
    ThreadPoolTaskSchedulerCustomizer dropPendingScheduledTasksOnShutdown() {
        log.info("Scheduled tasks still queued at shutdown will be dropped, not awaited");
        return taskScheduler -> taskScheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }
}
