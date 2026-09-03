package uk.ac.ceh.gateway.catalogue.config;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingProperties;
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the scheduled-task threading and shutdown contract of dri-one #354.
 * <p>
 * {@link ConfigDataApplicationContextInitializer} loads the real {@code application.properties}, so
 * these assertions are against the values the application actually ships rather than values the test
 * supplies. The scheduler is then built through the same auto-configured builder production uses and
 * shut down by hand, because {@code ThreadPoolTaskScheduler#shutdown} is exactly what the bean's
 * destruction step calls.
 * <p>
 * What this cannot prove is the container half — that Kubernetes' termination grace period outlasts
 * the await, and that the JVM gets to run its shutdown hook at all. That needs a real
 * {@code kubectl delete pod} or {@code docker stop}; see {@code docs/scheduled-tasks.md}.
 */
@DisplayName("Scheduled task pool and shutdown")
class SchedulingConfigTest {

    /**
     * The Kubernetes default, and what both the prod and staging manifests leave unset. The await
     * period has to fit inside it with room for the rest of the shutdown, or the pod is SIGKILLed
     * part-way through the task anyway.
     */
    private static final Duration TERMINATION_GRACE_PERIOD = Duration.ofSeconds(30);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withInitializer(new ConfigDataApplicationContextInitializer())
        .withConfiguration(AutoConfigurations.of(TaskSchedulingAutoConfiguration.class))
        .withUserConfiguration(SchedulingConfig.class);

    @Test
    @DisplayName("Scheduled tasks have more than one thread between them")
    void poolSizeIsSetDeliberately() {
        contextRunner.run(context -> {
            val properties = context.getBean(TaskSchedulingProperties.class);
            assertThat(properties.getPool().getSize())
                .as("Boot's default of 1 puts the Fuseki export and the index-repair checks on the same thread")
                .isEqualTo(4);

            val taskScheduler = buildScheduler(context.getBean(ThreadPoolTaskSchedulerBuilder.class));
            try {
                assertThat(taskScheduler.getScheduledThreadPoolExecutor().getCorePoolSize())
                    .as("the property is inert if anything else defines a TaskScheduler bean")
                    .isEqualTo(4);
            } finally {
                taskScheduler.shutdown();
            }
        });
    }

    @Test
    @DisplayName("Await period fits inside the container's termination grace period")
    void awaitPeriodFitsTheGracePeriod() {
        contextRunner.run(context -> {
            val shutdown = context.getBean(TaskSchedulingProperties.class).getShutdown();
            assertThat(shutdown.isAwaitTermination()).isTrue();
            assertThat(shutdown.getAwaitTerminationPeriod())
                .isNotNull()
                .isPositive()
                // Not "less than the grace period": the JVM still has to exit afterwards.
                .isLessThan(TERMINATION_GRACE_PERIOD.minusSeconds(5));
        });
    }

    @Test
    @DisplayName("A task running at shutdown is allowed to finish, not interrupted")
    void runningTaskFinishes() {
        contextRunner.run(context -> {
            val taskScheduler = buildScheduler(context.getBean(ThreadPoolTaskSchedulerBuilder.class));
            val started = new CountDownLatch(1);
            val interrupted = new AtomicBoolean(false);
            val finished = new CountDownLatch(1);

            taskScheduler.schedule(() -> {
                started.countDown();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    interrupted.set(true);
                    Thread.currentThread().interrupt();
                }
                finished.countDown();
            }, Instant.now());
            assertThat(started.await(10, TimeUnit.SECONDS)).isTrue();

            taskScheduler.shutdown();

            assertThat(finished.await(1, TimeUnit.SECONDS))
                .as("shutdown should have waited for the task rather than returning while it ran")
                .isTrue();
            assertThat(interrupted.get())
                .as("await-termination unset means shutdownNow(), which interrupts mid-operation")
                .isFalse();
        });
    }

    @Test
    @DisplayName("A task still queued at shutdown is dropped rather than waited for")
    void queuedTaskIsDropped() {
        contextRunner.run(context -> {
            val taskScheduler = buildScheduler(context.getBean(ThreadPoolTaskSchedulerBuilder.class));
            // Nothing is running: this stands in for a SolrScheduledReindexService startup retry,
            // scheduled minutes out and not registered with Spring's ScheduledTaskRegistrar.
            taskScheduler.schedule(() -> { }, Instant.now().plus(Duration.ofHours(1)));

            val before = Instant.now();
            taskScheduler.shutdown();

            assertThat(Duration.between(before, Instant.now()))
                .as("without SchedulingConfig's customizer this burns the whole await period")
                .isLessThan(Duration.ofSeconds(5));
        });
    }

    private ThreadPoolTaskScheduler buildScheduler(ThreadPoolTaskSchedulerBuilder builder) {
        val taskScheduler = builder.build();
        // What the bean's afterPropertiesSet does; the builder only configures.
        taskScheduler.initialize();
        return taskScheduler;
    }
}
