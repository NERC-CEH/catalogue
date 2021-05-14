package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.ToString;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The following class will process a list of OnlineResources to identify:
 * - The Supporting documentation of the document
 * - Links to resources in the order manager
 * - Links to download resources
 * - If this document is currently orderable/downloadable
 *
 * If an order resource is present inside the online resource list but does not
 * link to the order manager, then we will deem this to not be orderable (e.g.
 * Embargoed).
 *
 * The logic in this class makes use of the fact that OnlineResources have safe
 * variables (That is strings are never null)
 */
@Slf4j
@ToString
@Service
public class DownloadOrderDetailsService {
    private final Pattern supportingDocUrlPattern;
    private final List<Pattern> orderManagerUrlPatterns;

    private static final String SUPPORTING_DOC_URL_PATTERN = "https:\\/\\/data-package\\.ceh\\.ac\\.uk\\/sd\\/.*";
    private static final List<String> ORDER_MANAGER_URL_PATTERNS = Arrays.asList(
        "http(s?):\\/\\/catalogue\\.ceh\\.ac\\.uk\\/download\\?fileIdentifier=.*",
        "https:\\/\\/order-eidc\\.ceh\\.ac\\.uk\\/resources\\/.{8}\\/order\\?*.*"
    );

    public DownloadOrderDetailsService() {
        this.supportingDocUrlPattern = Pattern.compile(SUPPORTING_DOC_URL_PATTERN);
        this.orderManagerUrlPatterns = ORDER_MANAGER_URL_PATTERNS.stream()
            .map(Pattern::compile)
            .collect(Collectors.toList());
        log.info("Creating {}", this);
    }

    public DownloadOrder from(List<OnlineResource> onlineResources) {
        return new DownloadOrder(onlineResources);
    }

    @Value
    public class DownloadOrder {
        String supportingDocumentsUrl;
        List<OnlineResource> orderResources;

        // Decide if we should show an unavailable message on the UI. This value
        // will be false if the dataset is embargoed or unavailable
        boolean isOrderable;

        public DownloadOrder(List<OnlineResource> onlineResources) {
            supportingDocumentsUrl = extractSupportingDocumentUrl(onlineResources);
            orderResources = Lists.newArrayList(Iterables.concat(
                extractDownloadUrl(onlineResources),
                extractOrderUrl(onlineResources)
            ));
            isOrderable = !orderResources.isEmpty();

            if (!isOrderable) {
                // No DOWNLOADs or order manager ORDERs were found. Does a
                // message exist as a dummy order?
                onlineResources
                    .stream()
                    .filter(r -> r.getFunction().equals("offlineAccess"))
                    .forEach(orderResources::add);
            }
        }

        private String extractSupportingDocumentUrl(List<OnlineResource> onlineResources) {
            return onlineResources
                .stream()
                .filter(r -> r.getFunction().equals("information"))
                .filter(r -> supportingDocUrlPattern.matcher(r.getUrl()).matches())
                .map(OnlineResource::getUrl)
                .findFirst().orElse(null);
        }

        private List<OnlineResource> extractDownloadUrl(List<OnlineResource> onlineResources) {
            return onlineResources
                .stream()
                .filter(r -> r.getFunction().equals("download"))
                .collect(Collectors.toList());
        }

        private List<OnlineResource> extractOrderUrl(List<OnlineResource> onlineResources) {
            return onlineResources
                .stream()
                .filter(r -> r.getFunction().equals("order"))
                .filter(r -> orderManagerUrlPatterns.stream()
                    .anyMatch(p -> p.matcher(r.getUrl()).matches())
                )
                .collect(Collectors.toList());
        }
    }
}
