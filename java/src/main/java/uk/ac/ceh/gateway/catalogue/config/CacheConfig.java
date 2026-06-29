package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;

import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

// Enabled in every real environment; only disabled under the "test" profile (which all
// @SpringBootTest classes activate) so the JVM-global JCache manager is not re-created across the
// many test contexts in a single suite run — the original reason this config was profile-gated.
@Profile("!test")
@SuppressWarnings("DuplicatedCode")
@Slf4j
@Configuration
public class CacheConfig implements CachingConfigurer {

    @Bean
    @Override
    public CacheManager cacheManager() {
        log.info("Customizing caches");
        val provider = Caching.getCachingProvider();
        val cacheManager = provider.getCacheManager();

        createIfAbsent(cacheManager, "capabilities", new MutableConfiguration<String, WmsCapabilities>()
            .setTypes(String.class, WmsCapabilities.class)
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30))));
        createIfAbsent(cacheManager, "crowd-user", new MutableConfiguration<String, CatalogueUser>()
            .setTypes(String.class, CatalogueUser.class)
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30))));
        createIfAbsent(cacheManager, "crowd-user-groups", new MutableConfiguration<>()
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30))));
        createIfAbsent(cacheManager, "metadata-listings", new MutableConfiguration<>()
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 3))));

        // Datastore read caches. Unlike the caches above these hold record bytes (.raw can be large)
        // so they MUST be size-bounded to avoid OOM. Latest/historical blob content is keyed so a
        // bounded heap entry count is the right limit; eviction-on-write keeps "latest" fresh.
        createIfAbsent(cacheManager, CachedDataRepository.REVISION_ID_CACHE,
            bytesBoundedCache(String.class, 16, 10, TimeUnit.SECONDS));
        createIfAbsent(cacheManager, CachedDataRepository.LATEST_CACHE,
            bytesBoundedCache(byte[].class, 5000, 60, TimeUnit.MINUTES));
        createIfAbsent(cacheManager, CachedDataRepository.HISTORICAL_CACHE,
            bytesBoundedCache(byte[].class, 1000, 30, TimeUnit.MINUTES));

        return new JCacheCacheManager(cacheManager);
    }

    /**
     * The JCache {@link javax.cache.CacheManager} is a JVM-global singleton, so when several
     * (non-{@code test}) application contexts are built in one JVM — as the integration tests that
     * verify the production wiring do — a second {@code createCache} for the same name throws
     * "cache already exists". Creating only when absent makes this config safe to load in multiple
     * contexts; in a real single-context runtime it behaves exactly as a plain create.
     */
    private <K, V> void createIfAbsent(
        javax.cache.CacheManager cacheManager,
        String name,
        javax.cache.configuration.Configuration<K, V> configuration
    ) {
        if (cacheManager.getCache(name) == null) {
            cacheManager.createCache(name, configuration);
        }
    }

    private <V> javax.cache.configuration.Configuration<String, V> bytesBoundedCache(
        Class<V> valueType, long maxEntries, long ttl, TimeUnit unit
    ) {
        return Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    String.class, valueType, ResourcePoolsBuilder.heap(maxEntries))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(
                    java.time.Duration.ofMillis(unit.toMillis(ttl))))
                .build()
        );
    }
}
