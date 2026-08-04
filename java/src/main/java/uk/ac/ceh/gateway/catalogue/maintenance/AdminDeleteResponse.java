package uk.ac.ceh.gateway.catalogue.maintenance;

import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.util.ArrayList;
import java.util.List;

/**
 * Backs the admin delete form. One object serves all three steps — the empty form, the preview and the
 * outcome — which is the pattern {@code SparqlResponse} and {@code MaintenanceResponse} already use.
 */
@ConvertUsing({
    @Template(called="html/maintenance/delete.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Data
public class AdminDeleteResponse {

    /** Every location, so the form can render the choices without hardcoding them. */
    private final List<AdminDeleteLocation> locations = List.of(AdminDeleteLocation.values());

    private List<String> messages = new ArrayList<>();
    private String error;

    private AdminDeleteLocation location;
    private String id;

    /** True once a preview has succeeded, which is what makes the confirm step available. */
    private boolean found;
    private boolean deleted;

    // Facts read from .meta. Deliberately not the assembled document: .meta is plain JSON and parses
    // even when documentType has no registered class, which is the case for the records this exists for.
    private String path;
    private String documentType;
    private boolean documentTypeRegistered;
    private String state;
    private String catalogue;
    private String permissions;
    private boolean rawPresent;
    private int rawSize;

    /**
     * CSRF is enabled for this route only, and templates here render through
     * {@code Object2TemplatedMessageConverter}, whose data model is just this object plus FreeMarker
     * shared variables — Spring MVC's {@code _csrf} request attribute is not available. So the token is
     * carried explicitly for the form's hidden field.
     */
    private String csrfParameterName;
    private String csrfToken;

    public AdminDeleteResponse addMessage(String message) {
        messages.add(message);
        return this;
    }

    /** A published record is the most consequential to remove, so the form asks for more before it does. */
    public boolean isPublished() {
        return "published".equalsIgnoreCase(state);
    }
}
