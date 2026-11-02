package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Proves the content cache's keys and condition through a real caching proxy, which the
 * {@code test} profile's no-op cache manager cannot.
 */
@SpringJUnitConfig(PreviewContentReaderCacheTest.Config.class)
class PreviewContentReaderCacheTest {

    private static final String ID = "rec-1";
    private static final String PATH = "dictionary.csv";
    private static final String SHA = "abc123";

    @Configuration
    @EnableCaching
    static class Config {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(PreviewContentReader.DATA_PREVIEW_CONTENT_CACHE);
        }

        @Bean
        PreviewFileResolver resolver() {
            return mock(PreviewFileResolver.class);
        }

        @Bean
        DocumentConverter documentConverter() {
            return mock(DocumentConverter.class);
        }

        @Bean
        PreviewContentReader previewContentReader(
            PreviewFileResolver resolver, DocumentConverter documentConverter
        ) {
            DataPreviewProperties properties = new DataPreviewProperties(
                true, null, null, 10, 50, 262144, 20971520, 200000, Duration.ofSeconds(20),
                Duration.ofSeconds(3), Duration.ofSeconds(10));
            return new PreviewContentReader(resolver, documentConverter, properties);
        }
    }

    @Autowired
    private PreviewContentReader reader;
    @Autowired
    private PreviewFileResolver resolver;
    @Autowired
    private DocumentConverter documentConverter;
    @Autowired
    private CacheManager cacheManager;

    private final DocumentPreview table = DocumentPreview.table(new CsvSample(List.of("name"), List.of(List.of("x")), false, true));
    private final DocumentPreview preview = DocumentPreview.text("Units", false);
    private Path file;

    @BeforeEach
    void setUp() throws IOException {
        reset(resolver, documentConverter);
        cacheManager.getCache(PreviewContentReader.DATA_PREVIEW_CONTENT_CACHE).clear();
        file = Files.createTempFile("preview-cache", ".csv");
        file.toFile().deleteOnExit();
        when(resolver.resolve(any(), anyString(), anyString())).thenReturn(Optional.of(file));
        when(documentConverter.convertData(any(), any())).thenReturn(table);
        when(documentConverter.convert(any(), any())).thenReturn(preview);
    }

    @Test
    void dataAndDocumentForSamePathAndHashDoNotCollide() {
        DocumentPreview data = reader.data(ID, PATH, SHA);
        DocumentPreview document = reader.document(ID, PATH, SHA);

        assertThat(data, is(table));
        assertThat(document, is(preview));
    }

    @Test
    void secondDataReadIsServedFromCache() {
        reader.data(ID, PATH, SHA);
        reader.data(ID, PATH, SHA);

        verify(documentConverter, times(1)).convertData(any(), any());
    }

    @Test
    void unresolvableFileIsNotCached() {
        when(resolver.resolve(any(), anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> reader.data(ID, PATH, SHA));
        assertThrows(IllegalStateException.class, () -> reader.document(ID, PATH, SHA));
        reset(resolver);
        when(resolver.resolve(any(), anyString(), anyString())).thenReturn(Optional.of(file));

        assertThat(reader.data(ID, PATH, SHA), is(table));
        assertThat(reader.document(ID, PATH, SHA), is(preview));
    }

    @Test
    void converterFailureIsNotCached() {
        when(documentConverter.convert(any(), any()))
            .thenThrow(new UncheckedIOException(new IOException("Host is down")))
            .thenReturn(preview);

        assertThrows(UncheckedIOException.class, () -> reader.document(ID, PATH, SHA));

        assertThat(reader.document(ID, PATH, SHA), is(preview));
        verify(documentConverter, times(2)).convert(any(), any());
    }

    @Test
    void nullSha256IsNotCached() {
        reader.data(ID, PATH, null);
        reader.data(ID, PATH, null);
        reader.document(ID, PATH, null);
        reader.document(ID, PATH, null);

        verify(documentConverter, times(2)).convertData(any(), any());
        verify(documentConverter, times(2)).convert(any(), any());
    }
}
