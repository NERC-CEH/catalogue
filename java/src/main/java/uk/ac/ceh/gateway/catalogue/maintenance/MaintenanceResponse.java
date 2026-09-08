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

    public MaintenanceResponse addMessage(String message) {
        messages.add(message);
        return this;
    }
}
