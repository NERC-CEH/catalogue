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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
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

    /**
     * The switch of dri-one #356 defaults to on, and has to: no deployment sets it, so if the default
     * were off — or if the property name were ever mistyped — every scheduled task in the table in
     * {@code docs/scheduled-tasks.md} would quietly stop, with a healthy-looking pod to show for it.
     * This asserts the default from the other side of the same coin as
     * {@link #turningSchedulingOffRegistersNothing()}: no property set, everything registered.
     */
    @Test
    @DisplayName("Scheduling is on unless something explicitly turns it off")
    void schedulingIsOnByDefault() {
        contextRunner.run(context -> {
            assertThat(context)
                .as("no @Scheduled method is registered without this processor")
                .hasSingleBean(ScheduledAnnotationBeanPostProcessor.class);
            assertThat(context.getBean(TaskScheduler.class))
                .as("Boot's scheduler is @ConditionalOnBean on that processor")
                .isInstanceOf(ThreadPoolTaskScheduler.class);
            assertThat(((ThreadPoolTaskScheduler) context.getBean(TaskScheduler.class))
                .getScheduledThreadPoolExecutor().getCorePoolSize())
                .as("the shipped pool size, on the bean rather than on a builder")
                .isEqualTo(4);
        });
    }

    /**
     * Off means nothing is registered <em>and</em> nothing submitted by hand runs either. The second
     * half is the one that matters for {@code SolrScheduledReindexService}, which schedules its
     * startup retry chain programmatically rather than through {@code @Scheduled} and so is untouched
     * by dropping the annotation processor.
     */
    @Test
    @DisplayName("Turning scheduling off registers nothing and discards what is submitted")
    void turningSchedulingOffRegistersNothing() {
        contextRunner
            .withPropertyValues(SchedulingConfig.ENABLED_PROPERTY + "=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);

                val taskScheduler = context.getBean(TaskScheduler.class);
                assertThat(taskScheduler)
                    .as("something must satisfy the beans that inject a TaskScheduler")
                    .isInstanceOf(SchedulingConfig.DiscardingTaskScheduler.class);

                val ran = new AtomicBoolean(false);
                val future = taskScheduler.schedule(() -> ran.set(true), Instant.now());
                // Due immediately, so a scheduler that intended to run it would have by now.
                Thread.sleep(200);
                assertThat(ran).isFalse();
                assertThat(future.isCancelled()).isTrue();
            });
    }

    /**
     * Every overload, not just the one {@code SolrScheduledReindexService} happens to call today: a
     * half-implemented stand-in that quietly runs the fixed-delay variants would reintroduce #356 for
     * whichever caller reached for it next.
     */
    @Test
    @DisplayName("Every way of submitting a task is discarded, not just the one in use")
    void everySchedulingOverloadIsDiscarded() throws InterruptedException {
        val scheduler = new SchedulingConfig.DiscardingTaskScheduler();
        val ran = new AtomicBoolean(false);
        val task = (Runnable) () -> ran.set(true);
        val now = Instant.now();
        val tick = Duration.ofMillis(1);

        val futures = List.of(
            scheduler.schedule(task, new PeriodicTrigger(tick)),
            scheduler.schedule(task, now),
            scheduler.scheduleAtFixedRate(task, now, tick),
            scheduler.scheduleAtFixedRate(task, tick),
            scheduler.scheduleWithFixedDelay(task, now, tick),
            scheduler.scheduleWithFixedDelay(task, tick)
        );

        Thread.sleep(200);
        assertThat(ran).isFalse();
        assertThat(futures).allSatisfy(future -> assertThat(future.isCancelled()).isTrue());
    }

    private ThreadPoolTaskScheduler buildScheduler(ThreadPoolTaskSchedulerBuilder builder) {
        val taskScheduler = builder.build();
        // What the bean's afterPropertiesSet does; the builder only configures.
        taskScheduler.initialize();
        return taskScheduler;
    }
}
