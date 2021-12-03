package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Data
@ConvertUsing({
        @Template(called = "html/service_agreement/service-agreement-history.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
})
@Slf4j
public class History {
    private String historyOf;
    private List<Revision> revisions;

    public History(String baseUri, String id, List<DataRevision<CatalogueUser>> dataRevisions) {
        historyOf = id;
        this.revisions = new ArrayList<>();
        int version = dataRevisions.size() - 1; // don't include current version
        log.debug("start - version: {}", version);

        for (DataRevision<CatalogueUser> dataRevision : dataRevisionsWithoutCurrent(dataRevisions)) {
            val revisionId = dataRevision.getRevisionID();
            log.debug("version: {}, {}", version, revisionId);
            this.revisions.add(new History.Revision(String.valueOf(version), baseUri, id, revisionId));
            version--;
        }
    }

    private List<DataRevision<CatalogueUser>> dataRevisionsWithoutCurrent(List<DataRevision<CatalogueUser>> dataRevisions) {
        List<DataRevision<CatalogueUser>> withoutCurrent = new LinkedList<>(dataRevisions);
        withoutCurrent.remove(0); //remove current version
        return withoutCurrent;
    }

    @Data
    public static class Revision {
        private String version;
        private String href;

        public Revision(String version, String baseUri, String id, String revisionId) {
            this.version = version;
            this.href = baseUri + "/service-agreement/" + id + "/version/" + revisionId;
        }
    }
}
