package uk.ac.ceh.gateway.catalogue.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;

public class CachingConfigTest {
    private CachingConfig config;
    
    @BeforeEach
    public void cachingConfigTest() {
        config = new CachingConfig();
    }
    
    @Test
    public void ensureThatACacheManagerIsCreatedForCapabilitiesService() throws NoSuchMethodException {
        //Given
        String cacheName = GetCapabilitiesObtainerService.class
                            .getMethod("getWmsCapabilities", OnlineResource.class)
                            .getAnnotation(Cacheable.class)
                            .value()[0];
        
        CacheManager manager = config.cacheManager();
        
        //When
        Cache cache = manager.getCache(cacheName);
        
        //Then
        assertNotNull(cache);
    }
}
