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
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProgress.State;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;

/**
 * What the last run did to each graph, and whether one being held back gets noticed.
 *
 * <p>The distinction under test is the one that matters in production: a first
 * fill legitimately withholds a graph for several runs and must stay quiet,
 * while a graph that has stopped converging must not — that case was logged at
 * {@code INFO}, which {@code logging.level.root=warn} does not emit, so a
 * permanently unpublished graph said nothing at all.
 *
 * <p>The same distinction is now also what the maintenance page reports, so the
 * recorded state is asserted alongside the log level rather than instead of it.
 */
@DisplayName("Recording what a run did to each source graph")
class SourceGraphProgressTest {

    private static final String GRAPH = "https://orcid.org/";

    private SourceGraphProgress progress;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        progress = new SourceGraphProgress();
        appender = new ListAppender<>();
        appender.start();
        ((Logger) LoggerFactory.getLogger(SourceGraphProgress.class)).addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        ((Logger) LoggerFactory.getLogger(SourceGraphProgress.class)).detachAppender(appender);
        appender.stop();
    }

    private List<Level> levels() {
        return appender.list.stream().map(ILoggingEvent::getLevel).toList();
    }

    private String lastMessage() {
        val event = appender.list.get(appender.list.size() - 1);
        return event.getFormattedMessage();
    }

    private SourceGraphProgress.Run run() {
        return progress.lastRun().get(GRAPH);
    }

    @Nested
    @DisplayName("while a first fill is still converging")
    class Converging {

        @Test
        @DisplayName("the first run says so at INFO, because withholding is the correct behaviour")
        void firstRunIsInfo() {
            progress.withheld(GRAPH, 2125, 1625, 0);

            assertThat(levels(), contains(Level.INFO));
            assertThat(lastMessage(), containsString("1625 are still to be fetched"));
        }

        @Test
        @DisplayName("a run that has fetched more of them stays at INFO and says it is coming down")
        void progressStaysInfo() {
            progress.withheld(GRAPH, 2125, 1625, 0);
            progress.withheld(GRAPH, 2125, 1125, 0);
            progress.withheld(GRAPH, 2125, 625, 0);

            assertThat("five runs to fill ORCID must not raise five warnings",
                levels(), contains(Level.INFO, Level.INFO, Level.INFO));
            assertThat(lastMessage(), containsString("down from 1125 outstanding"));
        }

        @Test
        @DisplayName("it is reported as filling, with how far it has got")
        void reportsFilling() {
            progress.withheld(GRAPH, 2125, 278, 0);

            assertThat(run().state(), is(State.FILLING));
            assertThat(run().entities(), is(2125));
            assertThat(run().outstanding(), is(278));
            assertThat("described is what is left once the outstanding are taken off",
                run().described(), is(1847));
        }

        @Test
        @DisplayName("entities the authority could not serve count as outstanding, not as described")
        void transientFailuresAreOutstanding() {
            progress.withheld(GRAPH, 2125, 100, 39);

            assertThat(run().outstanding(), is(139));
            assertThat(run().described(), is(1986));
        }
    }

    @Nested
    @DisplayName("once it has stopped converging")
    class Stuck {

        @Test
        @DisplayName("a run that is no better than the last warns, since nothing will fix itself")
        void noProgressWarns() {
            progress.withheld(GRAPH, 2125, 40, 0);
            progress.withheld(GRAPH, 2125, 40, 0);

            assertThat(levels(), contains(Level.INFO, Level.WARN));
            assertThat(lastMessage(), containsString("has not improved since the last run"));
            assertThat(run().state(), is(State.NOT_CONVERGING));
        }

        @Test
        @DisplayName("so does a run that is worse, which is a referenced set outgrowing its budget")
        void regressionWarns() {
            progress.withheld(GRAPH, 2125, 40, 0);
            progress.withheld(GRAPH, 2200, 115, 0);

            assertThat(levels(), contains(Level.INFO, Level.WARN));
            assertThat(run().state(), is(State.NOT_CONVERGING));
        }

        @Test
        @DisplayName("one entity the authority will never serve is enough to warn about")
        void oneTransientFailureForeverWarns() {
            // A 401 or a 403 is classified transient, so it blocks the graph for
            // good while a 404 does not. This is the shape of that.
            progress.withheld(GRAPH, 2125, 0, 1);
            progress.withheld(GRAPH, 2125, 0, 1);

            assertThat(levels(), contains(Level.INFO, Level.WARN));
            assertThat(lastMessage(), containsString("1 could not be served"));
            assertThat(run().state(), is(State.NOT_CONVERGING));
        }

        @Test
        @DisplayName("and it stays reported that way for as long as it stays stuck")
        void staysNotConverging() {
            progress.withheld(GRAPH, 2125, 40, 0);
            progress.withheld(GRAPH, 2125, 40, 0);
            progress.withheld(GRAPH, 2125, 40, 0);

            assertThat(run().state(), is(State.NOT_CONVERGING));
        }
    }

    @Nested
    @DisplayName("when the authority gave nothing at all")
    class NothingRetrieved {

        @Test
        @DisplayName("every entity is outstanding, because none of them was described")
        void allOutstanding() {
            progress.nothingRetrieved(GRAPH, 2125);

            assertThat(run().state(), is(State.NOTHING_RETRIEVED));
            assertThat(run().outstanding(), is(2125));
            assertThat(run().described(), is(0));
        }

        @Test
        @DisplayName("it does not make the next hold-back look like a stall")
        void doesNotSeedTheEscalation() {
            // The caller already warns unconditionally on this branch, so there is
            // nothing for the escalation to add - and comparing against it would
            // make the first genuine hold-back after a total failure warn as
            // though it had stopped converging, which it has not.
            progress.nothingRetrieved(GRAPH, 2125);
            progress.withheld(GRAPH, 2125, 2125, 0);

            assertThat(levels(), contains(Level.INFO));
        }
    }

    @Nested
    @DisplayName("when the graph publishes")
    class Published {

        @Test
        @DisplayName("a graph that publishes starts afresh, so a later hold-back is not read as a stall")
        void publishingResets() {
            progress.withheld(GRAPH, 2125, 40, 0);
            progress.published(GRAPH, 2125);
            progress.withheld(GRAPH, 2125, 40, 0);

            assertThat("the run in between succeeded, so this is a new first hold-back",
                levels(), contains(Level.INFO, Level.INFO));
        }

        @Test
        @DisplayName("nothing is outstanding, so the page shows it complete")
        void nothingOutstanding() {
            progress.published(GRAPH, 561);

            assertThat(run().state(), is(State.PUBLISHED));
            assertThat(run().entities(), is(561));
            assertThat(run().outstanding(), is(0));
            assertThat(run().described(), is(561));
        }

        @Test
        @DisplayName("the row is replaced rather than removed, so a healthy graph is still listed")
        void staysListed() {
            progress.withheld(GRAPH, 2125, 40, 0);
            progress.published(GRAPH, 2125);

            assertThat("a graph that has just succeeded is the one an operator most wants to see",
                progress.lastRun(), hasKey(GRAPH));
        }
    }

    @Nested
    @DisplayName("when nothing cites the authority")
    class NothingReferenced {

        @Test
        @DisplayName("it is reported as such, not as a graph nobody has reached")
        void isDistinctFromNotHavingRun() {
            progress.nothingReferenced(GRAPH);

            assertThat(run().state(), is(State.NOTHING_REFERENCED));
            assertThat("a graph working correctly must not be flagged",
                run().state().warning(), is(false));
        }

        @Test
        @DisplayName("it does not make the next hold-back look like a stall")
        void doesNotSeedTheEscalation() {
            // Nothing was outstanding, so there is no baseline to judge the run
            // after it against - the referenced set having grown from nothing is
            // a first fill, not a graph that has stopped converging.
            progress.nothingReferenced(GRAPH);
            progress.withheld(GRAPH, 40, 40, 0);

            assertThat(levels(), contains(Level.INFO));
        }
    }

    @Test
    @DisplayName("graphs are counted separately, so one stalling does not implicate another")
    void graphsAreIndependent() {
        progress.withheld(GRAPH, 2125, 40, 0);
        progress.withheld("https://ror.org/", 561, 40, 0);

        assertThat(levels(), contains(Level.INFO, Level.INFO));
    }

    @Test
    @DisplayName("the warning names the graph, so an operator knows which authority to look at")
    void warningNamesTheGraph() {
        progress.withheld(GRAPH, 2125, 40, 0);
        progress.withheld(GRAPH, 2125, 40, 0);

        assertThat(lastMessage(), containsString(GRAPH));
        assertThat(appender.list.get(1).getLevel(), is(Level.WARN));
    }

    @Test
    @DisplayName("nothing is reported before the first export, since the counts are per-process")
    void emptyBeforeAnyRun() {
        assertThat(progress.lastRun(), is(anEmptyMap()));
    }

    @Test
    @DisplayName("what is handed out cannot be written back into")
    void lastRunIsACopy() {
        progress.withheld(GRAPH, 2125, 40, 0);
        val taken = progress.lastRun();
        progress.published(GRAPH, 2125);

        assertThat("a caller iterating the map while an export runs must not see it change",
            taken.get(GRAPH).state(), is(State.FILLING));
        assertThat(taken, not(is(progress.lastRun())));
    }
}
