package uk.ac.ceh.gateway.catalogue.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.config.CacheConfig;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Regression test for the SimpleKey/String key-type mismatch that broke Solr indexing.
 *
 * <p>An argument-less {@code @Cacheable} method without an explicit key makes Spring generate a
 * {@code SimpleKey} instead of a stable literal key. The production cache manager (Caffeine, via
 * {@link CacheConfig}) is untyped and won't throw on this — it will simply cache under a different,
 * less specific key, silently degrading caching rather than failing loudly. So this test's job is
 * to prove {@code getLatestRevisionId()}'s explicit {@code key = "'HEAD'"} SpEL still produces a
 * stable, reusable key by asserting a genuine cache hit on the second call. The behavioural
 * {@link DatastoreReadCacheTest} doesn't cover this because its {@code ConcurrentMapCacheManager} is
 * a separate, ad-hoc fixture — this test uses the real production cache manager to be faithful to
 * how {@code CacheConfig} actually wires the {@code datastore-revision-id} cache.</p>
 */
@SpringJUnitConfig(RevisionIdCacheKeyTypeTest.Config.class)
public class RevisionIdCacheKeyTypeTest {

    @EnableCaching
    @Configuration
    static class Config {
        @Bean
        CacheManager cacheManager() {
            // The real production wiring: CaffeineCacheManager as configured by CacheConfig.
            return new CacheConfig().cacheManager();
        }

        @Bean
        @SuppressWarnings("unchecked")
        DataRepository<CatalogueUser> dataRepository() {
            return mock(DataRepository.class, RETURNS_DEEP_STUBS);
        }

        @Bean
        CachedDataRepository cachedDataRepository(DataRepository<CatalogueUser> repo) {
            return new CachedDataRepository(repo);
        }
    }

    @Autowired DataRepository<CatalogueUser> repo;
    @Autowired CachedDataRepository cachedRepo;
    @Autowired CacheManager cacheManager;

    @BeforeEach
    public void reset() {
        clearInvocations(repo);
        var cache = cacheManager.getCache(CachedDataRepository.REVISION_ID_CACHE);
        if (cache != null) cache.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void latestRevisionIdCachesWithoutKeyTypeError() throws Exception {
        DataRevision<CatalogueUser> revision = mock(DataRevision.class);
        given(revision.getRevisionID()).willReturn("rev-1");
        given(repo.getLatestRevision()).willReturn(revision);

        // First call must not throw "Invalid key type ... SimpleKey"; second must hit the cache.
        assertThat(cachedRepo.getLatestRevisionId()).isEqualTo("rev-1");
        assertThat(cachedRepo.getLatestRevisionId()).isEqualTo("rev-1");

        verify(repo, times(1)).getLatestRevision();
    }
}
