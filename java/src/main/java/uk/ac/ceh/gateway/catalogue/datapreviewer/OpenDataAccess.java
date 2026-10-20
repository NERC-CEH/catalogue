package uk.ac.ceh.gateway.catalogue.datapreviewer;

import com.google.common.collect.Multimap;
import org.springframework.stereotype.Component;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;

/**
 * The single rule for whether a record's data files may be shown to anyone.
 * A record is open when it is publicly viewable, marked as available and its
 * access limitation grants open access.
 */
@Component
public class OpenDataAccess {

    public boolean isOpen(GeminiDocument document) {
        if (document == null) {
            return false;
        }
        MetadataInfo metadata = document.getMetadata();
        if (metadata == null) {
            return false;
        }
        Multimap<Permission, String> permissions = metadata.getPermissions();
        if (permissions == null) {
            return false;
        }

        boolean hasPublicView = permissions.containsEntry(Permission.VIEW, "public");
        boolean isAvailable = "Available".equalsIgnoreCase(document.getAvailability());
        boolean isFreelyAvailable = document.getAccessLimitation() != null
            && document.getAccessLimitation().grantsOpenAccess();

        return hasPublicView && isAvailable && isFreelyAvailable;
    }
}
