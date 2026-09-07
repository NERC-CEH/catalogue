package uk.ac.ceh.gateway.catalogue.exports;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Whether a graph that is being held back gets noticed.
 *
 * <p>The distinction under test is the one that matters in production: a first
 * fill legitimately withholds a graph for several runs and must stay quiet,
 * while a graph that has stopped converging must not — that case was logged at
 * {@code INFO}, which {@code logging.level.root=warn} does not emit, so a
 * permanently unpublished graph said nothing at all.
 */
@DisplayName("Noticing a graph that is being held back")
class WithheldGraphLogTest {

    private static final String GRAPH = "https://orcid.org/";

    private WithheldGraphLog log;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        log = new WithheldGraphLog();
        appender = new ListAppender<>();
        appender.start();
        ((Logger) LoggerFactory.getLogger(WithheldGraphLog.class)).addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        ((Logger) LoggerFactory.getLogger(WithheldGraphLog.class)).detachAppender(appender);
        appender.stop();
    }

    private List<Level> levels() {
        return appender.list.stream().map(ILoggingEvent::getLevel).toList();
    }

    private String lastMessage() {
        val event = appender.list.get(appender.list.size() - 1);
        return event.getFormattedMessage();
    }

    @Nested
    @DisplayName("while a first fill is still converging")
    class Converging {

        @Test
        @DisplayName("the first run says so at INFO, because withholding is the correct behaviour")
        void firstRunIsInfo() {
            log.withheld(GRAPH, 2125, 1625, 0);

            assertThat(levels(), contains(Level.INFO));
            assertThat(lastMessage(), containsString("1625 are still to be fetched"));
        }

        @Test
        @DisplayName("a run that has fetched more of them stays at INFO and says it is coming down")
        void progressStaysInfo() {
            log.withheld(GRAPH, 2125, 1625, 0);
            log.withheld(GRAPH, 2125, 1125, 0);
            log.withheld(GRAPH, 2125, 625, 0);

            assertThat("five runs to fill ORCID must not raise five warnings",
                levels(), contains(Level.INFO, Level.INFO, Level.INFO));
            assertThat(lastMessage(), containsString("down from 1125 outstanding"));
        }
    }

    @Nested
    @DisplayName("once it has stopped converging")
    class Stuck {

        @Test
        @DisplayName("a run that is no better than the last warns, since nothing will fix itself")
        void noProgressWarns() {
            log.withheld(GRAPH, 2125, 40, 0);
            log.withheld(GRAPH, 2125, 40, 0);

            assertThat(levels(), contains(Level.INFO, Level.WARN));
            assertThat(lastMessage(), containsString("has not improved since the last run"));
        }

        @Test
        @DisplayName("so does a run that is worse, which is a referenced set outgrowing its budget")
        void regressionWarns() {
            log.withheld(GRAPH, 2125, 40, 0);
            log.withheld(GRAPH, 2200, 115, 0);

            assertThat(levels(), contains(Level.INFO, Level.WARN));
        }

        @Test
        @DisplayName("one entity the authority will never serve is enough to warn about")
        void oneTransientFailureForeverWarns() {
            // A 401 or a 403 is classified transient, so it blocks the graph for
            // good while a 404 does not. This is the shape of that.
            log.withheld(GRAPH, 2125, 0, 1);
            log.withheld(GRAPH, 2125, 0, 1);

            assertThat(levels(), contains(Level.INFO, Level.WARN));
            assertThat(lastMessage(), containsString("1 could not be served"));
        }
    }

    @Test
    @DisplayName("a graph that publishes starts afresh, so a later hold-back is not read as a stall")
    void publishingResets() {
        log.withheld(GRAPH, 2125, 40, 0);
        log.published(GRAPH);
        log.withheld(GRAPH, 2125, 40, 0);

        assertThat("the run in between succeeded, so this is a new first hold-back",
            levels(), contains(Level.INFO, Level.INFO));
    }

    @Test
    @DisplayName("graphs are counted separately, so one stalling does not implicate another")
    void graphsAreIndependent() {
        log.withheld(GRAPH, 2125, 40, 0);
        log.withheld("https://ror.org/", 561, 40, 0);

        assertThat(levels(), contains(Level.INFO, Level.INFO));
    }

    @Test
    @DisplayName("the warning names the graph, so an operator knows which authority to look at")
    void warningNamesTheGraph() {
        log.withheld(GRAPH, 2125, 40, 0);
        log.withheld(GRAPH, 2125, 40, 0);

        assertThat(lastMessage(), containsString(GRAPH));
        assertThat(appender.list.get(1).getLevel(), is(Level.WARN));
    }
}
