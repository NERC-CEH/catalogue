package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.task.ThreadPoolTaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Decides whether scheduled work runs at all, and customises the auto-configured
 * {@code taskScheduler} beyond what {@code spring.task.scheduling.*} can express.
 * <p>
 * Everything else about the scheduler is set in {@code application.properties} — pool size, and
 * whether a running task is awaited at shutdown — and the reasoning for those values is recorded
 * there and in {@code docs/scheduled-tasks.md}.
 */
@Slf4j
@Configuration
public class SchedulingConfig {

    /**
     * Whether {@code @Scheduled} methods are registered and programmatically scheduled work runs.
     * Absent means enabled: see {@link SchedulingEnabled}.
     */
    public static final String ENABLED_PROPERTY = "catalogue.scheduling.enabled";

    /**
     * The name Boot's auto-configuration gives the scheduler, reused by {@link SchedulingDisabled} so
     * that injection by name behaves the same in both modes.
     */
    public static final String TASK_SCHEDULER_BEAN = "taskScheduler";

    /**
     * Registers {@code @Scheduled} methods, which in turn is what makes Boot auto-configure a
     * {@code taskScheduler} at all — {@code TaskSchedulingConfigurations.TaskSchedulerConfiguration}
     * is {@code @ConditionalOnBean} on the annotation processor this enables.
     * <p>
     * {@code matchIfMissing} is deliberate and load-bearing. The property exists for the test suite,
     * and every deployment leaves it unset; if the default were "off", a typo in the property name —
     * or a stray copy of a test properties file — would silently stop the Fuseki export, the
     * vocabulary refreshes, the metrics sync and the empty-index repair, with nothing in the logs to
     * say so. {@code SchedulingConfigTest} asserts the default both ways round.
     */
    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(name = ENABLED_PROPERTY, havingValue = "true", matchIfMissing = true)
    static class SchedulingEnabled {
    }

    /**
     * Stands in for the scheduler when scheduling is off, so that beans which take a
     * {@link TaskScheduler} still construct.
     * <p>
     * A bean is needed rather than nothing at all because Boot's {@code taskScheduler} disappears
     * along with the annotation processor, and {@code SolrScheduledReindexService} — the one
     * component that schedules <em>programmatically</em> rather than through {@code @Scheduled} —
     * injects one. Discarding its submissions is the point rather than a side effect: switching off
     * {@code @Scheduled} registration alone would leave that retry chain running, which is half of
     * what dri-one #356 is about.
     */
    @Configuration
    @ConditionalOnProperty(name = ENABLED_PROPERTY, havingValue = "false")
    static class SchedulingDisabled {

        @Bean(TASK_SCHEDULER_BEAN)
        TaskScheduler discardingTaskScheduler() {
            log.warn(
                "{}=false: no @Scheduled method will run and submitted tasks are discarded. This is "
                    + "for the test suite -- if you are seeing this in a deployment, background work "
                    + "(exports, vocabulary refreshes, index repair, metrics sync) is switched off.",
                ENABLED_PROPERTY
            );
            return new DiscardingTaskScheduler();
        }
    }

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
     * <p>
     * Left unconditional so that it is applied to any scheduler built from Boot's builder, including
     * one built by a test; it is inert when there is no pool to customise.
     */
    @Bean
    ThreadPoolTaskSchedulerCustomizer dropPendingScheduledTasksOnShutdown() {
        log.info("Scheduled tasks still queued at shutdown will be dropped, not awaited");
        return taskScheduler -> taskScheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    /**
     * Accepts scheduling requests and runs none of them, returning an already-cancelled future so a
     * caller that keeps the handle sees a task that will not complete rather than one that never
     * fires. Nothing in this application inspects the returned future, but a cancelled one is the
     * honest answer.
     */
    static class DiscardingTaskScheduler implements TaskScheduler {

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
            return discard(task);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
            return discard(task);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
            return discard(task);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
            return discard(task);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
            return discard(task);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
            return discard(task);
        }

        private ScheduledFuture<?> discard(Runnable task) {
            log.debug("Scheduling is disabled; discarding {}", task);
            return new CancelledScheduledFuture();
        }
    }

    /**
     * A {@link ScheduledFuture} that is cancelled from the outset. {@link CompletableFuture} supplies
     * the {@code Future} half; only the two {@code ScheduledFuture} methods need writing.
     */
    private static class CancelledScheduledFuture extends CompletableFuture<Object> implements ScheduledFuture<Object> {

        CancelledScheduledFuture() {
            cancel(false);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(getDelay(TimeUnit.NANOSECONDS), other.getDelay(TimeUnit.NANOSECONDS));
        }
    }
}
