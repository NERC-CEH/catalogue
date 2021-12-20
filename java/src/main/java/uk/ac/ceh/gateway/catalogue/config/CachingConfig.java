package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.stereotype.Component;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CachingConfig implements JCacheManagerCustomizer {

    @Override
    public void customize(javax.cache.CacheManager cacheManager) {
        log.info("Customizing caches");
        cacheManager.createCache("capabilities", new MutableConfiguration<>()
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 5))));
        cacheManager.createCache("crowd-user", new MutableConfiguration<>()
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30))));
        cacheManager.createCache("crowd-user-groups", new MutableConfiguration<>()
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30))));
        cacheManager.createCache("metadata-listings", new MutableConfiguration<>()
            .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 3))));
    }
}
