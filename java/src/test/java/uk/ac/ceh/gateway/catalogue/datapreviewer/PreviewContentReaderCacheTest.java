package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
        CsvSampler csvSampler() {
            return mock(CsvSampler.class);
        }

        @Bean
        DocumentConverter documentConverter() {
            return mock(DocumentConverter.class);
        }

        @Bean
        PreviewContentReader previewContentReader(
            PreviewFileResolver resolver, CsvSampler csvSampler, DocumentConverter documentConverter
        ) {
            DataPreviewProperties properties = new DataPreviewProperties(
                true, null, null, 10, 50, 262144, 20971520, 200000, Duration.ofSeconds(20),
                Duration.ofSeconds(3), Duration.ofSeconds(10));
            return new PreviewContentReader(resolver, csvSampler, documentConverter, properties);
        }
    }

    @Autowired
    private PreviewContentReader reader;
    @Autowired
    private PreviewFileResolver resolver;
    @Autowired
    private CsvSampler csvSampler;
    @Autowired
    private DocumentConverter documentConverter;
    @Autowired
    private CacheManager cacheManager;

    private final CsvSample sample = new CsvSample(List.of("name"), List.of(List.of("x")), false, true);
    private final DocumentPreview preview = DocumentPreview.text("Units", false);

    @BeforeEach
    void setUp() throws IOException {
        reset(resolver, csvSampler, documentConverter);
        cacheManager.getCache(PreviewContentReader.DATA_PREVIEW_CONTENT_CACHE).clear();
        Path file = Files.createTempFile("preview-cache", ".csv");
        file.toFile().deleteOnExit();
        when(resolver.resolve(any(), anyString(), anyString())).thenReturn(Optional.of(file));
        when(csvSampler.sample(any(), anyInt(), anyInt())).thenReturn(sample);
        when(documentConverter.convert(any(), any())).thenReturn(preview);
    }

    @Test
    void csvAndDocumentForSamePathAndHashDoNotCollide() {
        CsvSample csv = reader.csv(ID, PATH, SHA);
        DocumentPreview document = reader.document(ID, PATH, SHA);

        assertThat(csv, is(sample));
        assertThat(document, is(preview));
    }

    @Test
    void secondCsvReadIsServedFromCache() {
        reader.csv(ID, PATH, SHA);
        reader.csv(ID, PATH, SHA);

        verify(csvSampler, times(1)).sample(any(), anyInt(), anyInt());
    }

    @Test
    void nullSha256IsNotCached() {
        reader.csv(ID, PATH, null);
        reader.csv(ID, PATH, null);
        reader.document(ID, PATH, null);
        reader.document(ID, PATH, null);

        verify(csvSampler, times(2)).sample(any(), anyInt(), anyInt());
        verify(documentConverter, times(2)).convert(any(), any());
    }
}
