package uk.ac.ceh.gateway.catalogue.config;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author cjohn
 */
@Configuration
public class CachingConfig implements CachingConfigurer {
    @Bean(destroyMethod="shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        CacheConfiguration capabilitiesCache = new CacheConfiguration();
        capabilitiesCache.setName("capabilities");
        capabilitiesCache.setTimeToLiveSeconds(500);
        capabilitiesCache.setMemoryStoreEvictionPolicy("LRU");
        capabilitiesCache.setMaxEntriesLocalHeap(1000);
        
        CacheConfiguration wafCache = new CacheConfiguration();
        wafCache.setName("metadata-listings");
        wafCache.setMaxEntriesLocalHeap(10);

        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(capabilitiesCache);
        config.addCache(wafCache);

        return net.sf.ehcache.CacheManager.newInstance(config);
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }
}
