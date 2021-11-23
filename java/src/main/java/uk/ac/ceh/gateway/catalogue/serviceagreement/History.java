package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ConvertUsing({
        @Template(called = "html/service-agreement-history.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
})
public class History {
    private String historyOf;
    private List<Revision> revisions;

    public  History(String id, List<DataRevision<CatalogueUser>> history, String baseUri) {
        historyOf = id;
        revisions = new ArrayList<>();
        int version = history.size();
        for (DataRevision revision : history) {
            revisions.add(new History.Revision(historyOf, String.valueOf(version), revision.getRevisionID(), baseUri));
            version--;
        }
    }


    @Data
    public static class Revision {
        private String version;
        private String href;

        public Revision(String historyOf, String version, String revisionId, String baseUri) {
            this.version = version;
            this.href = baseUri + "/service-agreement/" + historyOf + "/version/" + revisionId;
        }
    }
}
