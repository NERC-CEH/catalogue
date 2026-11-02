package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.nio.file.Path;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Reads a listed file off the share and turns it into what the preview shows. Makes no access or
 * listing check: callers go through {@link DataPreviewService}, which does both first.
 *
 * <p>Results are cached on the Hubbub checksum, so a replaced file is read afresh; a file Hubbub
 * reports no checksum for is never cached. Data and document keys carry their kind, as one file can
 * be listed as both and the two results share a cache. Exceptions are never cached, so a
 * {@link PreviewBusyException}, a listed file missing from the share or a read fault is retried on
 * the next request.
 */
@Component
public class PreviewContentReader {

    public static final String DATA_PREVIEW_CONTENT_CACHE = "data-preview-content";

    private final PreviewFileResolver resolver;
    private final DocumentConverter documentConverter;
    private final DataPreviewProperties properties;

    public PreviewContentReader(
        PreviewFileResolver resolver,
        DocumentConverter documentConverter,
        DataPreviewProperties properties
    ) {
        this.resolver = resolver;
        this.documentConverter = documentConverter;
        this.properties = properties;
    }

    @Cacheable(cacheNames = DATA_PREVIEW_CONTENT_CACHE, key = "'data|' + #recordId + '|' + #path + '|' + #sha256",
        condition = "#sha256 != null")
    public DocumentPreview data(String recordId, String path, String sha256) {
        return documentConverter.convertData(resolve(properties.dataLocation(), recordId, path), path);
    }

    @Cacheable(cacheNames = DATA_PREVIEW_CONTENT_CACHE, key = "'doc|' + #recordId + '|' + #path + '|' + #sha256",
        condition = "#sha256 != null")
    public DocumentPreview document(String recordId, String path, String sha256) {
        return documentConverter.convert(resolve(properties.supportingDocumentsLocation(), recordId, path), path);
    }

    /**
     * A listed file that cannot be found on the share may be there again shortly, as after a share
     * fault, so this throws rather than giving a result that would be cached.
     */
    private Path resolve(Path location, String recordId, String path) {
        return resolver.resolve(location, recordId, path).orElseThrow(() -> new IllegalStateException(
            "Listed file " + path + " of " + recordId + " cannot be read from the share"));
    }
}
