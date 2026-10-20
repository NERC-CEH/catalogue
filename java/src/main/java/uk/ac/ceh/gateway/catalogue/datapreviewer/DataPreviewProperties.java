package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings for the data preview on the dataset record page. The feature is off unless
 * {@code enabled} is true; the two locations are the volume mounts holding the data files and the
 * supporting documents, each laid out as {@code <location>/<recordId>/<hubbub path>}.
 *
 * <p>The Hubbub timeouts apply only to the preview's own listing calls, which anonymous page views
 * drive; the upload page keeps its own client, as some of its operations legitimately run long.
 */
@ConfigurationProperties("catalogue.data-preview")
public record DataPreviewProperties(
    @DefaultValue("false") boolean enabled,
    Path dataLocation,
    Path supportingDocumentsLocation,
    @DefaultValue("10") int maxCsvFiles,
    @DefaultValue("50") int previewRows,
    @DefaultValue("262144") int maxCsvBytes,
    @DefaultValue("20971520") long maxDocumentBytes,
    @DefaultValue("200000") int maxDocumentChars,
    @DefaultValue("20s") Duration conversionTimeout,
    @DefaultValue("3s") Duration hubbubConnectTimeout,
    @DefaultValue("10s") Duration hubbubReadTimeout
) {}
