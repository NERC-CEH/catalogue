package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Notices when a graph that is being held back has stopped making progress.
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
 * remembers the last count per graph and escalates only when it fails to
 * improve, which is the point at which someone needs to look.
 *
 * <p>The counts are per-process and start empty after a restart, which costs one
 * run's escalation and no correctness — a genuinely stuck graph is still stuck
 * on the run after that.
 */
@Slf4j
@Profile("exports")
@Service
@ToString
public class WithheldGraphLog {

    /** Outstanding entities per graph, as of the last run that held it back. */
    private final Map<String, Integer> outstanding = new ConcurrentHashMap<>();

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
        val previous = outstanding.put(graph, remaining);

        if (previous != null && remaining >= previous) {
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

    /** Records that a graph was published, so the next hold-back starts afresh. */
    public void published(String graph) {
        outstanding.remove(graph);
    }
}
