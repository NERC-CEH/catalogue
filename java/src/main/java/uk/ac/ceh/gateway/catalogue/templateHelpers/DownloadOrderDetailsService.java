package uk.ac.ceh.gateway.catalogue.templateHelpers;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.config.DownloadUrlProperties;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The following class will process a list of OnlineResources to identify:
 * - The Supporting documentation of the document
 * - Links to resources in the order manager
 * - Links to download resources
 * - If this document is currently orderable/downloadable
 * <p>
 * If an order resource is present inside the online resource list but does not
 * link to the order manager, then we will deem this to not be orderable (e.g.
 * Embargoed).
 * <p>
 * The logic in this class makes use of the fact that OnlineResources have safe
 * variables (That is strings are never null)
 */
@Slf4j
@ToString
@Service
public class DownloadOrderDetailsService {
    private final Pattern supportingDocUrlPattern;
    private final List<Pattern> orderManagerUrlPatterns;
    boolean containsEidcDistribution;

    public DownloadOrderDetailsService(
        @NotNull DownloadUrlProperties downloadUrlProperties
        ) {
        this.supportingDocUrlPattern = Pattern.compile(downloadUrlProperties.getRegexSupportingDocs());
        this.orderManagerUrlPatterns = List.of(
            Pattern.compile(downloadUrlProperties.getRegexOrderManDownload()),
            Pattern.compile(downloadUrlProperties.getRegexOrder())
        );
        log.info("Creating");
    }

    public DownloadOrder from(List<OnlineResource> onlineResources) {
        return new DownloadOrder(onlineResources);
    }

    @lombok.Value
    public class DownloadOrder {
        String supportingDocumentsUrl;
        List<OnlineResource> dataAccessResources;

        // Decide if we should show an unavailable message on the UI. This value
        // will be false if the dataset is embargoed or unavailable
        boolean isDataAccessible, isDataAddressable;
        boolean isEidcDistribution;

        public DownloadOrder(List<OnlineResource> onlineResources) {

            var fileAccessUrls = extractFileAccessUrl(onlineResources);
            var downloadUrls = extractDownloadUrl(onlineResources);
            var orderUrls = extractOrderUrl(onlineResources);
            var offlineAccessUrls = extractOfflineAccessUrl(onlineResources);

            supportingDocumentsUrl = extractSupportingDocumentUrl(onlineResources);
            dataAccessResources = Lists.newArrayList(Iterables.concat(
                fileAccessUrls,
                downloadUrls,
                orderUrls,
                offlineAccessUrls
            ));

            isDataAccessible =
                !downloadUrls.isEmpty() ||
                !orderUrls.isEmpty() ||
                !fileAccessUrls.isEmpty();

            isDataAddressable = !fileAccessUrls.isEmpty();

            // Compute whether any resource is an EIDC distribution
            isEidcDistribution = onlineResources.stream()
                .anyMatch(OnlineResource::isEidcDistribution);
        }

        private String extractSupportingDocumentUrl(List<OnlineResource> onlineResources) {
            return onlineResources
                .stream()
                .filter(r -> r.getFunction().equals("information"))
                .map(OnlineResource::getUrl)
                .filter(url -> supportingDocUrlPattern.matcher(url).matches())
                .findFirst().orElse(null);
        }

        private List<OnlineResource> extractDownloadUrl(List<OnlineResource> onlineResources) {
            return onlineResources
                .stream()
                .filter(r -> r.getFunction().equals("download"))
                .collect(Collectors.toList());
        }

        private List<OnlineResource> extractFileAccessUrl(List<OnlineResource> onlineResources) {
            return onlineResources
                .stream()
                .filter(r -> r.getFunction().equals("fileAccess"))
                .collect(Collectors.toList());
        }

        private List<OnlineResource> extractOfflineAccessUrl(List<OnlineResource> onlineResources) {
            return onlineResources
                .stream()
                .filter(r -> r.getFunction().equals("offlineAccess"))
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
