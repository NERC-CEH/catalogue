package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What the last export run did to each source graph, and whether a graph that is
 * being held back has stopped making progress.
 *
 * <p>A source graph is published only when the run behind it is complete, so
 * being held back is a normal step rather than a fault: a cold cache fills ORCID
 * over five runs at {@code orcid.requestsPerRun}, and each of those runs
 * correctly publishes nothing. Reporting every one of them as a problem would
 * make the real problem invisible.
 *
 * <p>But the same branch is reached when a graph will <em>never</em> converge —
 * a referenced set that has outgrown its budget, or one entity returning a 4xx
 * that is not a 404, which is classified transient and so blocks the graph for
 * good. That was logged at {@code INFO}, and {@code logging.level.root=warn} in
 * production means it was not logged at all. A graph could stay unpublished
 * indefinitely with nothing to say so.
 *
 * <p>What separates the two is whether the number of outstanding entities is
 * going down. A first fill shrinks it every run; a stuck graph does not. So this
 * remembers the last run per graph and escalates only when it fails to improve,
 * which is the point at which someone needs to look.
 *
 * <h2>Why this is read as well as written</h2>
 *
 * <p>Escalating to {@code WARN} tells an operator who is watching the log. It
 * does not tell one who is looking at the catalogue, and "is ORCID filled yet?"
 * is a question asked far more often than a log is read. So the same record the
 * escalation is decided from is published on the maintenance page, which is why
 * a graph that publishes is now recorded as published rather than simply
 * forgotten: a healthy graph is the one an operator most wants to see listed.
 *
 * <p>The counts are per-process and start empty after a restart, which costs one
 * run's escalation and no correctness — a genuinely stuck graph is still stuck
 * on the run after that. The page says as much rather than presenting an
 * un-run graph as an empty one.
 */
@Slf4j
@Profile("exports")
@Service
@ToString
public class SourceGraphProgress {

    /** What became of one graph on the run that last touched it. */
    public enum State {

        /** The run was complete and the graph was written to the endpoint. */
        PUBLISHED("Published", false),

        /** Held back, but fewer entities are outstanding than on the run before. */
        FILLING("Filling", false),

        /** Held back with no improvement, so it will not publish without a change. */
        NOT_CONVERGING("Not converging", true),

        /** Neither the authority nor the cache had anything to say. */
        NOTHING_RETRIEVED("Nothing retrieved", true),

        /**
         * The catalogue cites no entity this authority describes, so the run had
         * nothing to ask for. Distinguished from a graph no run has reached,
         * which looks identical on the page and is not the same thing at all:
         * one is working correctly and the other may not be.
         */
        NOTHING_REFERENCED("Nothing referenced", false);

        private final String label;
        private final boolean warning;

        State(String label, boolean warning) {
            this.label = label;
            this.warning = warning;
        }

        /** How the maintenance page names this state. */
        public String label() {
            return label;
        }

        /** Whether reaching this state is something an operator has to act on. */
        public boolean warning() {
            return warning;
        }
    }

    /**
     * @param entities    how many the run set out to describe
     * @param outstanding how many of them it did not end up with
     * @param state       what became of the graph as a result
     */
    public record Run(int entities, int outstanding, State state) {

        /** Derived rather than stored: the retriever counts what it missed, not what it got. */
        public int described() {
            return entities - outstanding;
        }
    }

    private final Map<String, Run> runs = new ConcurrentHashMap<>();

    /**
     * Records that a graph was not published because its run was incomplete.
     *
     * @param graph             the named graph held back
     * @param entities          how many the run set out to describe
     * @param deferred          how many it never reached
     * @param transientFailures how many it reached but could not obtain
     */
    public void withheld(String graph, int entities, int deferred, int transientFailures) {
        val remaining = deferred + transientFailures;
        val previous = previousHoldBack(graph);
        val stalled = previous != null && remaining >= previous;
        runs.put(graph, new Run(entities, remaining, stalled ? State.NOT_CONVERGING : State.FILLING));

        if (stalled) {
            log.warn(
                "Not publishing {}: {} of {} entities are still outstanding ({} to fetch, {} could "
                    + "not be served) and that has not improved since the last run, which was {}. "
                    + "A graph in this state stays unpublished until it converges -- check the "
                    + "per-run budget against the size of the referenced set, and the log above for "
                    + "an authority refusing one entity persistently",
                graph, remaining, entities, deferred, transientFailures, previous);
            return;
        }
        log.info(
            "Not publishing {} yet: of {} entities, {} are still to be fetched and {} could not be "
                + "served, so replacing the graph would publish less than it already holds{}",
            graph, entities, deferred, transientFailures,
            previous == null ? "" : " (down from " + previous + " outstanding)");
    }

    /**
     * The outstanding count to judge this run against, or null if there is none
     * to judge it by.
     *
     * <p>Only a previous <em>hold-back</em> counts, so every other state resets
     * the comparison. Publishing resets it because the graph demonstrably
     * converged; a total failure resets it because that is already reported on
     * its own, and treating it as a baseline would make the recovery after it
     * look like a stall. Whitelisting the two hold-back states rather than
     * excluding the rest means a state added later cannot accidentally become a
     * baseline it was never meant to be.
     */
    private Integer previousHoldBack(String graph) {
        val previous = runs.get(graph);
        if (previous == null
            || (previous.state() != State.FILLING && previous.state() != State.NOT_CONVERGING)) {
            return null;
        }
        return previous.outstanding();
    }

    /**
     * Records that the run had nothing to ask this authority for, because the
     * catalogue's own graph cites none of its entities.
     *
     * <p>Not a hold-back and not a failure, so it takes no part in the
     * escalation: there is nothing outstanding to converge.
     */
    public void nothingReferenced(String graph) {
        runs.put(graph, new Run(0, 0, State.NOTHING_REFERENCED));
    }

    /**
     * Records that nothing at all came back for a graph, from the authority or
     * the cache.
     *
     * <p>Deliberately not fed into the escalation above. The caller already warns
     * unconditionally on this branch, so there is nothing an escalation could
     * add, and comparing the next run against a total failure would make the
     * first genuine hold-back after one look like a stall.
     *
     * @param graph    the named graph left alone
     * @param entities how many the run set out to describe, none of which it did
     */
    public void nothingRetrieved(String graph, int entities) {
        runs.put(graph, new Run(entities, entities, State.NOTHING_RETRIEVED));
    }

    /**
     * Records that a graph was published, so the next hold-back starts afresh.
     *
     * @param graph    the named graph written to the endpoint
     * @param entities how many it describes
     */
    public void published(String graph, int entities) {
        runs.put(graph, new Run(entities, 0, State.PUBLISHED));
    }

    /**
     * @return what the last run that touched it did to each graph, keyed by
     *         graph. A copy, so a page rendering it does not see the map change
     *         under it while an export is running. Graphs no run has reached are
     *         absent rather than zeroed — a graph nobody has tried yet and one
     *         that came back with nothing are different states, and the second
     *         is the one worth acting on.
     */
    public Map<String, Run> lastRun() {
        return Map.copyOf(runs);
    }
}
