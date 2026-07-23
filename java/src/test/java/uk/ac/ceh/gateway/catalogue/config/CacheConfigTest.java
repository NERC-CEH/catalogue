package uk.ac.ceh.gateway.catalogue.config;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository.HISTORICAL_CACHE;
import static uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository.LATEST_CACHE;
import static uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository.REVISION_ID_CACHE;
import static uk.ac.ceh.gateway.catalogue.services.MetadataListingService.METADATA_LISTINGS_CACHE;
import static uk.ac.ceh.gateway.catalogue.userdetails.CrowdGroupStore.CROWD_GROUP_CACHE;
import static uk.ac.ceh.gateway.catalogue.userdetails.CrowdUserStore.CROWD_USER_CACHE;
import static uk.ac.ceh.gateway.catalogue.wms.GetCapabilitiesObtainerService.CAPABILITIES_CACHE;

class CacheConfigTest {

    @Test
    void everyCacheRecordsStatsForActuatorMetrics() {
        //given
        CacheManager cacheManager = new CacheConfig().cacheManager();
        String[] cacheNames = {
            CAPABILITIES_CACHE, CROWD_USER_CACHE, CROWD_GROUP_CACHE, METADATA_LISTINGS_CACHE,
            REVISION_ID_CACHE, LATEST_CACHE, HISTORICAL_CACHE
        };

        //when/then
        for (String cacheName : cacheNames) {
            CaffeineCache springCache = (CaffeineCache) cacheManager.getCache(cacheName);
            Cache<Object, Object> nativeCache = springCache.getNativeCache();
            assertThat("stats recording enabled for " + cacheName, nativeCache.policy().isRecordingStats(), is(true));
        }
    }

}
