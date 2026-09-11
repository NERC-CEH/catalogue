package uk.ac.ceh.gateway.catalogue.maintenance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@ConvertUsing({
    @Template(called="html/maintenance.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Data
public class MaintenanceResponse {
    private List<String> messages = new ArrayList<>();
    private boolean isLinked;
    private boolean isIndexed;
    private boolean isHasMapFiles;
    private int indexedMapFilesCount;
    private DataRevision<CatalogueUser> latestRevision;
    private Date lastOptimized;
    private boolean exportsAvailable;
    private Date lastExported;
    private List<GraphProgress> sourceGraphProgress = List.of();
    /** Null unless the "vector-search" profile is active, which keeps the panel off the page entirely. */
    private EmbeddingProgress embeddingProgress;

    /**
     * How far one authority's source graph has got towards being published.
     *
     * <p>Flattened into display terms here rather than handed to the template as
     * the enum it comes from, because the alternative is the template deciding
     * which states are worth flagging — and a state added later would then
     * silently render as neither. {@code state} and {@code warning} come off the
     * enum together, so a new one cannot be forgotten.
     *
     * @param graph       the named graph, which is also the authority's namespace
     * @param title       the authority's human-readable name
     * @param state       what the last run to touch it did
     * @param warning     whether that state is one an operator has to act on
     * @param run         false if no run has reached this graph since the
     *                    application started, in which case the counts mean
     *                    nothing and must not be shown as though they did
     * @param described   how many entities the graph describes
     * @param entities    how many the run set out to describe
     * @param outstanding how many it did not end up with
     */
    public record GraphProgress(
        String graph,
        String title,
        String state,
        boolean warning,
        boolean run,
        int described,
        int entities,
        int outstanding
    ) {
        /** Whether there are counts worth rendering, as opposed to a state alone. */
        public boolean hasCounts() {
            return run && entities > 0;
        }
    }

    /**
     * How much of the Solr index carries a vector, so an admin can tell at a glance whether
     * semantic search has anything to search.
     *
     * <p>{@code state} and {@code warning} are decided in {@link #from} rather than in the
     * template, for the same reason as {@link GraphProgress}: a template choosing which states
     * deserve flagging renders a later addition as neither.
     *
     * @param embedded  records carrying a vector
     * @param total     records in the index
     * @param pending   records queued for the next flush
     * @param abandoned records given up on after max-attempts, which will never gain a vector
     *                  without being saved again or the index rebuilt
     */
    public record EmbeddingProgress(
        long embedded,
        long total,
        int pending,
        int abandoned,
        String state,
        boolean warning
    ) {
        public static EmbeddingProgress from(long embedded, long total, int pending, int abandoned) {
            return new EmbeddingProgress(embedded, total, pending, abandoned,
                state(embedded, total, pending, abandoned), abandoned > 0 || noneAtAll(embedded, total, pending));
        }

        private static String state(long embedded, long total, int pending, int abandoned) {
            if (abandoned > 0) {
                // The one state needing action: these have exhausted max-attempts and will not be
                // retried, so they stay keyword-only until someone intervenes.
                return abandoned + (abandoned == 1 ? " record abandoned" : " records abandoned");
            }
            // An empty index is already reported by the Indexing section, and would otherwise show
            // here as the much more alarming "No embeddings".
            if (total == 0) return "No records indexed";
            if (noneAtAll(embedded, total, pending)) return "No embeddings";
            // A backfill runs a batch per flush, so partial coverage is normal for a while after a
            // reindex rather than a fault.
            return embedded < total ? "Filling" : "Ready";
        }

        private static boolean noneAtAll(long embedded, long total, int pending) {
            return embedded == 0 && pending == 0 && total > 0;
        }

        /** Whole-percent coverage, floored, for display beside the counts. */
        public int percentage() {
            return total == 0 ? 0 : (int) (embedded * 100 / total);
        }
    }

    public MaintenanceResponse addMessage(String message) {
        messages.add(message);
        return this;
    }
}
