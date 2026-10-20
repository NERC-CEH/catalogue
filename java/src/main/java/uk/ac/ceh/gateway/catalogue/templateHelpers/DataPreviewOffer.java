package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ceh.gateway.catalogue.datapreviewer.DataPreviewProperties;
import uk.ac.ceh.gateway.catalogue.datapreviewer.OpenDataAccess;
import uk.ac.ceh.gateway.catalogue.gemini.DistributionInfo;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Decides, for the record page, whether to render the Data Preview container. It reads only the
 * record (served from the datastore read cache) and never calls Hubbub or touches a file share, so
 * rendering the page stays cheap. The browser fetches the preview itself afterwards.
 */
@Slf4j
@Component
public class DataPreviewOffer {
    private final DataPreviewProperties properties;
    private final DocumentRepository documentRepository;
    private final OpenDataAccess openDataAccess;
    private final DownloadOrderDetailsService downloadOrderDetails;

    public DataPreviewOffer(
        DataPreviewProperties properties,
        DocumentRepository documentRepository,
        OpenDataAccess openDataAccess,
        DownloadOrderDetailsService downloadOrderDetails
    ) {
        this.properties = properties;
        this.documentRepository = documentRepository;
        this.openDataAccess = openDataAccess;
        this.downloadOrderDetails = downloadOrderDetails;
    }

    /** Never throws: a record page must not fail because of the preview. */
    public boolean offered(String recordId) {
        if (!properties.enabled()) {
            return false;
        }
        try {
            MetadataDocument document = documentRepository.read(recordId);
            if (!(document instanceof GeminiDocument gemini) || !openDataAccess.isOpen(gemini)) {
                return false;
            }
            return hasCsvFormat(gemini) || hasSupportingDocuments(gemini);
        } catch (Exception e) {
            log.debug("Data preview not offered for {}: record could not be read", recordId, e);
            return false;
        }
    }

    private boolean hasCsvFormat(GeminiDocument gemini) {
        List<DistributionInfo> formats = gemini.getDistributionFormats();
        return formats != null && formats.stream()
            .filter(Objects::nonNull)
            .map(DistributionInfo::getName)
            .anyMatch(name -> name != null && name.toUpperCase(Locale.ROOT).contains("CSV"));
    }

    private boolean hasSupportingDocuments(GeminiDocument gemini) {
        String url = downloadOrderDetails.from(gemini.getOnlineResources()).getSupportingDocumentsUrl();
        return url != null && !url.isBlank();
    }
}
