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
 * <p>{@code datastore-revision-id} is declared String-keyed in {@link CacheConfig}, and EHCache
 * enforces that type. An argument-less {@code @Cacheable} method without an explicit key makes
 * Spring generate a {@code SimpleKey}, which EHCache rejects ("Invalid key type, expected:
 * java.lang.String but was: ...SimpleKey"). The behavioural {@link DatastoreReadCacheTest} could not
 * catch this because its {@code ConcurrentMapCacheManager} is untyped — so this test uses the real
 * production cache manager (typed EHCache) to faithfully reproduce the enforcement.</p>
 */
@SpringJUnitConfig(RevisionIdCacheKeyTypeTest.Config.class)
public class RevisionIdCacheKeyTypeTest {

    @EnableCaching
    @Configuration
    static class Config {
        @Bean
        CacheManager cacheManager() {
            // The real production wiring: JCacheCacheManager over String-typed EHCache.
            // createIfAbsent inside makes this safe against the JVM-global JCache manager.
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
