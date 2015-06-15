package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;

/**
 * The following class will process a list of OnlineResources to identify: - The
 * Supporting documentation of the document - A link to a resource in the order
 * manager - If this document is currently orderable
 * 
 * If an order resource is present inside the online resource list but does not
 * link to the order manager, then we will deem this to not be orderable (e.g.
 * Embargoed).
 * 
 * The logic in this class makes use of the fact that OnlineResources have safe
 * variables (That is strings are never null)
 */
@Data
public class DownloadOrderDetailsService {
    private final Pattern eidchub, orderManager;
            
    public DownloadOrder from(List<OnlineResource> onlineResources) {
        return new DownloadOrder(onlineResources);
    }

    @Value
    public class DownloadOrder {
        private final String orderUrl, supportingDocumentsUrl, orderMessage;

        // Decide if we should show an unavailable message on the UI. This value 
        // will be false if the dataset is embargoed or unavailable
        private final boolean isOrderable;

        public DownloadOrder(List<OnlineResource> onlineResources) {
            //Read out the supportingDocumentationUrl (If present)
            supportingDocumentsUrl = onlineResources
                    .stream()
                    .filter(r -> r.getFunction().equals("information"))
                    .filter(r -> eidchub.matcher(r.getUrl()).matches())
                    .map(r -> r.getUrl())
                    .findFirst().orElse(null);

            Optional<OnlineResource> orderResource = onlineResources
                    .stream()
                    .filter(r -> r.getFunction().equals("order"))
                    .filter(r -> orderManager.matcher(r.getUrl()).matches())
                    .findFirst();
            isOrderable = orderResource.isPresent();

            if (!isOrderable) {
                orderResource = onlineResources
                        .stream()
                        .filter(r -> r.getFunction().equals("order"))
                        .findFirst();
            }

            if (orderResource.isPresent()) {
                orderMessage = orderResource.get().getDescription();
                orderUrl = orderResource.get().getUrl();
            } else {
                orderMessage = "";
                orderUrl = "";
            }
        }
    }
}
