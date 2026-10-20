package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Reads a listed file off the share and turns it into what the preview shows. Makes no access or
 * listing check: callers go through {@link DataPreviewService}, which does both first.
 *
 * <p>Results are cached on the Hubbub checksum, so a replaced file is read afresh. Exceptions are
 * never cached, so a {@link PreviewBusyException} or a share fault is retried on the next request.
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

    @Cacheable(cacheNames = DATA_PREVIEW_CONTENT_CACHE, key = "#recordId + '|' + #path + '|' + #sha256")
    public CsvSample csv(String recordId, String path, String sha256) {
        Optional<Path> file = resolver.resolve(properties.dataLocation(), recordId, path);
        if (file.isEmpty()) {
            return CsvSample.notPreviewable();
        }
        try (InputStream in = Files.newInputStream(file.get())) {
            return csvSampler.sample(in, properties.maxCsvBytes(), properties.previewRows());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Cacheable(cacheNames = DATA_PREVIEW_CONTENT_CACHE, key = "#recordId + '|' + #path + '|' + #sha256")
    public DocumentPreview document(String recordId, String path, String sha256) {
        return resolver.resolve(properties.supportingDocumentsLocation(), recordId, path)
            .map(documentConverter::convert)
            .orElseGet(DocumentPreview::notPreviewable);
    }
}
