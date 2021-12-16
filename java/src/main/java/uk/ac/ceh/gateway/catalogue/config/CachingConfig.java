package uk.ac.ceh.gateway.catalogue.config;

import lombok.val;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.components.userstore.crowd.CrowdEhCacheSupport;

@Configuration
public class CachingConfig {
    @Bean
    public CacheManager cacheManager() {
        val config = CrowdEhCacheSupport.createCrowdCacheConfiguration()
            .cache(capabilities())
            .cache(metadataListings());
        val cacheManager = net.sf.ehcache.CacheManager.newInstance(config);
        return new EhCacheCacheManager(cacheManager);
    }

    private CacheConfiguration capabilities() {
        return new CacheConfiguration("capabilities", 20)
            .timeToIdleSeconds(60L)
            .timeToLiveSeconds(120L);
    }

    private CacheConfiguration metadataListings() {
        return new CacheConfiguration("metadata-listings", 20)
            .timeToIdleSeconds(60L)
            .timeToLiveSeconds(120L);
    }
}
