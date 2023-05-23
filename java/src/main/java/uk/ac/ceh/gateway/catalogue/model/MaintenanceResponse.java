package uk.ac.ceh.gateway.catalogue.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@ConvertUsing({
    @Template(called="html/maintenance.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Data
public class MaintenanceResponse {
    private List<String> messages = new ArrayList<>();
    private boolean isLinked;
    private boolean isIndexed;
    private boolean isValidated;
    private boolean isHasMapFiles;
    private int indexedMapFilesCount;
    private DataRevision<CatalogueUser> latestRevision;
    private Date lastOptimized;

    public MaintenanceResponse addMessage(String message) {
        messages.add(message);
        return this;
    }
}
