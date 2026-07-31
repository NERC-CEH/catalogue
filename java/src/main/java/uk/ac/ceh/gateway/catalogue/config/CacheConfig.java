package uk.ac.ceh.gateway.catalogue.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;

import java.time.Duration;

import static uk.ac.ceh.gateway.catalogue.services.MetadataListingService.METADATA_LISTINGS_CACHE;
import static uk.ac.ceh.gateway.catalogue.userdetails.CrowdGroupStore.CROWD_GROUP_CACHE;
import static uk.ac.ceh.gateway.catalogue.userdetails.CrowdUserStore.CROWD_USER_CACHE;
import static uk.ac.ceh.gateway.catalogue.wms.GetCapabilitiesObtainerService.CAPABILITIES_CACHE;

// Enabled in every real environment; disabled under the "test" profile (which all @SpringBootTest
// classes activate) so that spring.cache.type=none (application-test.properties) can supply Boot's
// NoOpCacheManager for ordinary tests, undisturbed by this class's own explicit @Bean CacheManager.
@Profile("!test")
@Slf4j
@Configuration
public class CacheConfig implements CachingConfigurer {

    @Bean
    @Override
    public CacheManager cacheManager() {
        log.info("Customizing caches");
        val cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache(CAPABILITIES_CACHE, expireAfterAccess(Duration.ofMinutes(30)).build());
        cacheManager.registerCustomCache(CROWD_USER_CACHE, expireAfterAccess(Duration.ofMinutes(30)).build());
        cacheManager.registerCustomCache(CROWD_GROUP_CACHE, expireAfterAccess(Duration.ofMinutes(30)).build());
        cacheManager.registerCustomCache(METADATA_LISTINGS_CACHE, expireAfterAccess(Duration.ofMinutes(3)).build());

        // Datastore read caches. Unlike the caches above these hold record bytes (.raw can be large)
        // so they MUST be size-bounded to avoid OOM. Latest/historical blob content is keyed so a
        // bounded entry count is the right limit; eviction-on-write keeps "latest" fresh.
        //
        // TTLs here are backstops, NOT the invalidation mechanism: GitRepoWrapper.save/delete evict
        // the affected LATEST entries and all REVISION_ID entries on every write, so cached content
        // can only go stale if a write bypasses that path. In production this app is the sole writer
        // of a single-replica deployment, so that cannot happen; the TTLs are therefore generous
        // (guarding only against an unforeseen missed eviction) rather than short. Short TTLs were a
        // heavy tax against the SMB-mounted SAN: the 10s revision-id TTL re-resolved Git HEAD (a
        // multi-round-trip commit+tree walk) on the next read every 10s during read-only periods, and
        // the 60min LATEST TTL re-fetched each hot record's blobs hourly. If this ever becomes a
        // multi-replica deployment, or an external process writes the datastore, these TTLs bound
        // cross-writer staleness and must be shortened again.
        cacheManager.registerCustomCache(CachedDataRepository.REVISION_ID_CACHE,
            expireAfterWrite(16, Duration.ofHours(1)).build());
        // 6000 entries comfortably covers the whole published corpus (< 5000 records) plus growth
        // headroom, so the read working set stays fully warm and the long tail never re-hits the SAN.
        cacheManager.registerCustomCache(CachedDataRepository.LATEST_CACHE,
            expireAfterWrite(6000, Duration.ofHours(6)).build());
        // Historical (revision:name) content is immutable, so this TTL is purely a memory bound and
        // never a correctness concern; kept modest as historical reads are off the hot render path.
        cacheManager.registerCustomCache(CachedDataRepository.HISTORICAL_CACHE,
            expireAfterWrite(1000, Duration.ofMinutes(30)).build());
        cacheManager.registerCustomCache(CachedDataRepository.DOC_REVISION_CACHE,
            expireAfterWrite(6000, Duration.ofHours(6)).build());

        return cacheManager;
    }

    private Caffeine<Object, Object> expireAfterAccess(Duration ttl) {
        return Caffeine.newBuilder().expireAfterAccess(ttl).recordStats();
    }

    private Caffeine<Object, Object> expireAfterWrite(long maxEntries, Duration ttl) {
        return Caffeine.newBuilder().expireAfterWrite(ttl).maximumSize(maxEntries).recordStats();
    }
}
