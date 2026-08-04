package uk.ac.ceh.gateway.catalogue.metrics;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static uk.ac.ceh.gateway.catalogue.metrics.JDBCMetricsService.DOWNLOAD_TOTALS_CACHE;
import static uk.ac.ceh.gateway.catalogue.metrics.JDBCMetricsService.VIEW_TOTALS_CACHE;

/**
 * View and download counts are read from inside {@code _metrics.ftlh} while a record page renders, so
 * every uncached read puts a SQLite query on the render critical path. In production that database sits
 * on a CIFS mount and a single read cost seconds, which pinned 135 of 139 request threads inside
 * {@code NativeDB.step}. These tests cover the cache that keeps those reads off the render path.
 *
 * <p>{@link uk.ac.ceh.gateway.catalogue.config.CacheConfig} is {@code @Profile("!test")} and ordinary
 * {@code @SpringBootTest} runs set {@code spring.cache.type=none}, so caching is inert in the rest of
 * the suite. This class therefore stands up its own small caching context, using
 * {@link CaffeineCacheManager} to match what production actually runs.</p>
 */
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(MetricsCountCachingTest.Config.class)
class MetricsCountCachingTest {

    private static final String TEST_DOCUMENT = "123e4567-e89b-12d3-a456-426614174000";

    @Autowired private MetricsService service;
    @Autowired private EmbeddedDatabase db;
    @Autowired private CacheManager cacheManager;

    /**
     * The context is shared across methods, so both the rows and the cache entries have to be reset
     * here — a leftover cached total would make a later test pass or fail for the wrong reason.
     */
    @SneakyThrows
    @BeforeEach
    void reset() {
        try (val statement = db.getConnection().createStatement()) {
            statement.executeUpdate("DELETE FROM views");
            statement.executeUpdate("DELETE FROM downloads");
        }
        cacheManager.getCache(VIEW_TOTALS_CACHE).clear();
        cacheManager.getCache(DOWNLOAD_TOTALS_CACHE).clear();
    }

    /**
     * Inserting a second row behind the cache's back and still seeing the first total is what proves the
     * database was not consulted again — no mocking of the JDBC layer required.
     */
    @SneakyThrows
    @Test
    void repeatedViewCountsAreServedFromCacheRatherThanTheDatabase() {
        //given a document with one recorded view
        insert("views", TEST_DOCUMENT, 1);
        assertThat(service.totalViews(TEST_DOCUMENT), equalTo(1));

        //when more views land in the database directly
        insert("views", TEST_DOCUMENT, 40);

        //then the cached total is served, unchanged
        assertThat(service.totalViews(TEST_DOCUMENT), equalTo(1));
    }

    @SneakyThrows
    @Test
    void repeatedDownloadCountsAreServedFromCacheRatherThanTheDatabase() {
        //given
        insert("downloads", TEST_DOCUMENT, 2);
        assertThat(service.totalDownloads(TEST_DOCUMENT), equalTo(2));

        //when
        insert("downloads", TEST_DOCUMENT, 40);

        //then
        assertThat(service.totalDownloads(TEST_DOCUMENT), equalTo(2));
    }

    /**
     * {@code totalViews} and {@code totalDownloads} take the same single {@code String} argument, so a
     * shared cache under the default key generator would give them the same key and one would serve the
     * other's number. They must be cached separately.
     */
    @SneakyThrows
    @Test
    void viewAndDownloadTotalsDoNotShareCacheEntries() {
        //given the same document has different view and download counts
        insert("views", TEST_DOCUMENT, 7);
        insert("downloads", TEST_DOCUMENT, 3);

        //when both are read, in either order
        val views = service.totalViews(TEST_DOCUMENT);
        val downloads = service.totalDownloads(TEST_DOCUMENT);

        //then neither has been served the other's total
        assertThat(views, equalTo(7));
        assertThat(downloads, equalTo(3));
    }

    /**
     * An unavailable count is {@code null} by design, so the record page renders without a counter.
     * Caching that null would turn a momentary CIFS or lock failure into a counter that stays missing
     * for the whole TTL, so the absence must not be stored.
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void unavailableTotalsAreNotCached() {
        //given the database cannot be reached
        db.shutdown();

        //when the counts come back absent
        assertThat(service.totalViews(TEST_DOCUMENT), is(nullValue()));
        assertThat(service.totalDownloads(TEST_DOCUMENT), is(nullValue()));

        //then nothing was cached, so the next request can recover
        assertThat(cacheManager.getCache(VIEW_TOTALS_CACHE).get(TEST_DOCUMENT), is(nullValue()));
        assertThat(cacheManager.getCache(DOWNLOAD_TOTALS_CACHE).get(TEST_DOCUMENT), is(nullValue()));
    }

    private void insert(String table, String document, int amount) throws Exception {
        val sql = ("INSERT INTO %s (start_timestamp, end_timestamp, amount, document, doc_title, record_type) "
            + "VALUES (0, 0, %d, '%s', 'title', 'dataset')").formatted(table, amount, document);
        try (val statement = db.getConnection().createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    @Configuration
    @EnableCaching
    static class Config {
        @Bean
        EmbeddedDatabase db() {
            return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .build();
        }

        @Bean
        DocumentRepository documentRepository() {
            return mock(DocumentRepository.class);
        }

        @Bean
        MetricsService metricsService(EmbeddedDatabase db, DocumentRepository documentRepository) {
            return new JDBCMetricsService(db, documentRepository);
        }

        @Bean
        CacheManager cacheManager() {
            val cacheManager = new CaffeineCacheManager();
            val spec = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).maximumSize(100);
            cacheManager.registerCustomCache(VIEW_TOTALS_CACHE, spec.build());
            cacheManager.registerCustomCache(DOWNLOAD_TOTALS_CACHE, spec.build());
            return cacheManager;
        }
    }
}
