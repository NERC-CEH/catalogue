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
import java.util.Set;

/**
 * Decides, for the record page, whether to render the Data Preview container. It reads only the
 * record (served from the datastore read cache) and never calls Hubbub or touches a file share, so
 * rendering the page stays cheap. The browser fetches the preview itself afterwards.
 * <p>
 * Only a record declaring data the preview can show (delimited text, plain text, Excel or NetCDF) is
 * offered: its
 * supporting documents are previewed beside the data, never on their own. A declared format is only
 * a hint; the browser removes the section when the listing holds no data file it can show.
 */
@Slf4j
@Component
public class DataPreviewOffer {
    // Upper-cased words in a declared format name that mean the data may be previewable, as in
    // "Comma-separated values (CSV)", "Comma separated values", "Tab delimited text", "Plain text",
    // "Microsoft Excel" or "NetCDF".
    private static final List<String> TABULAR_WORDS =
        List.of("CSV", "TSV", "SEPARATED", "DELIMITED", "TEXT", "EXCEL", "XLSX", "NETCDF");
    // Whole format names, matched exactly, too short to look for inside others.
    private static final Set<String> TABULAR_NAMES = Set.of("DAT", "TXT", "ASCII");
    // Names containing a tabular word that are not tables.
    private static final List<String> NOT_TABULAR = List.of("RICH TEXT");

    private final DataPreviewProperties properties;
    private final DocumentRepository documentRepository;
    private final OpenDataAccess openDataAccess;

    public DataPreviewOffer(
        DataPreviewProperties properties,
        DocumentRepository documentRepository,
        OpenDataAccess openDataAccess
    ) {
        this.properties = properties;
        this.documentRepository = documentRepository;
        this.openDataAccess = openDataAccess;
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
            return hasTabularFormat(gemini);
        } catch (Exception e) {
            log.debug("Data preview not offered for {}: record could not be read", recordId, e);
            return false;
        }
    }

    private boolean hasTabularFormat(GeminiDocument gemini) {
        List<DistributionInfo> formats = gemini.getDistributionFormats();
        return formats != null && formats.stream()
            .filter(Objects::nonNull)
            .map(DistributionInfo::getName)
            .filter(Objects::nonNull)
            .map(name -> name.strip().toUpperCase(Locale.ROOT))
            .anyMatch(DataPreviewOffer::isTabular);
    }

    static boolean isTabular(String upperCaseName) {
        if (NOT_TABULAR.stream().anyMatch(upperCaseName::contains)) {
            return false;
        }
        return TABULAR_NAMES.contains(upperCaseName.replaceFirst("^\\.", ""))
            || TABULAR_WORDS.stream().anyMatch(upperCaseName::contains);
    }
}
