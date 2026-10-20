package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Reads a listed file off the share and turns it into what the preview shows. Makes no access or
 * listing check: callers go through {@link DataPreviewService}, which does both first.
 *
 * <p>Results are cached on the Hubbub checksum, so a replaced file is read afresh; a file Hubbub
 * reports no checksum for is never cached. CSV and document keys carry their kind, as one file can
 * be listed as both and the two results share a cache. Exceptions are never cached, so a
 * {@link PreviewBusyException}, a listed file missing from the share or a read fault is retried on
 * the next request.
 */
@Component
public class PreviewContentReader {

    public static final String DATA_PREVIEW_CONTENT_CACHE = "data-preview-content";

    private final PreviewFileResolver resolver;
    private final CsvSampler csvSampler;
    private final DocumentConverter documentConverter;
    private final DataPreviewProperties properties;

    public PreviewContentReader(
        PreviewFileResolver resolver,
        CsvSampler csvSampler,
        DocumentConverter documentConverter,
        DataPreviewProperties properties
    ) {
        this.resolver = resolver;
        this.csvSampler = csvSampler;
        this.documentConverter = documentConverter;
        this.properties = properties;
    }

    @Cacheable(cacheNames = DATA_PREVIEW_CONTENT_CACHE, key = "'csv|' + #recordId + '|' + #path + '|' + #sha256",
        condition = "#sha256 != null")
    public CsvSample csv(String recordId, String path, String sha256) {
        Path file = resolve(properties.dataLocation(), recordId, path);
        try (InputStream in = Files.newInputStream(file)) {
            return csvSampler.sample(in, properties.maxCsvBytes(), properties.previewRows());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
