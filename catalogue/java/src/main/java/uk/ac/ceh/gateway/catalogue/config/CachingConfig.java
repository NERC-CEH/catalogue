package uk.ac.ceh.gateway.catalogue.config;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
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
public class CachingConfig extends CachingConfigurerSupport {
    @Bean(destroyMethod="shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        CacheConfiguration capabilitiesCache = new CacheConfiguration();
        capabilitiesCache.setName("capabilities");
        capabilitiesCache.setTimeToLiveSeconds(500);
        capabilitiesCache.setMemoryStoreEvictionPolicy("LRU");
        capabilitiesCache.setMaxEntriesLocalHeap(1000);
        
        CacheConfiguration listingsCache = new CacheConfiguration();
        listingsCache.setName("metadata-listings");
        listingsCache.setMaxEntriesLocalHeap(10);

        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(capabilitiesCache);
        config.addCache(listingsCache);

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
