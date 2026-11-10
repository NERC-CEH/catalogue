package uk.ac.ceh.gateway.catalogue.datapreviewer;

import com.google.common.collect.Multimap;
import org.springframework.stereotype.Component;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.templateHelpers.DownloadOrderDetailsService;

/**
 * The single rule for whether a record's data files may be shown to anyone.
 * A record is open when it is publicly viewable, marked as available, its
 * access limitation grants open access, and it has been released: it has a
 * DOI under our DataCite prefix and a link that gives access to the data
 * ({@code download}, {@code order} or {@code fileAccess}), the same tests the
 * record page uses for "has a DOI" and "data can be accessed".
 * <p>
 * The release conditions matter because the files reach Hubbub's datastore
 * before the DOI is minted and the links are added; without them the files
 * would be listable and previewable ahead of their release.
 */
@Component
public class OpenDataAccess {
    private final DataciteService dataciteService;
    private final DownloadOrderDetailsService downloadOrderDetailsService;

    public OpenDataAccess(
        DataciteService dataciteService,
        DownloadOrderDetailsService downloadOrderDetailsService
    ) {
        this.dataciteService = dataciteService;
        this.downloadOrderDetailsService = downloadOrderDetailsService;
    }

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

        return hasPublicView && isAvailable && isFreelyAvailable && isReleased(document);
    }

    private boolean isReleased(GeminiDocument document) {
        return dataciteService.getDoi(document).isPresent()
            && downloadOrderDetailsService.from(document.getOnlineResources()).isDataAccessible();
    }
}
