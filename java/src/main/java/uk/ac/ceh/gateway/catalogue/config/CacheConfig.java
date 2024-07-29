package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

@Profile("cache")
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

        cacheManager.createCache("capabilities", new MutableConfiguration<String, WmsCapabilities>()
            .setTypes(String.class, WmsCapabilities.class)
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30))));
        cacheManager.createCache("crowd-user", new MutableConfiguration<String, CatalogueUser>()
            .setTypes(String.class, CatalogueUser.class)
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30))));
        cacheManager.createCache("crowd-user-groups", new MutableConfiguration<>()
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30))));
        cacheManager.createCache("metadata-listings", new MutableConfiguration<>()
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 3))));

        return new JCacheCacheManager(cacheManager);
    }
}
