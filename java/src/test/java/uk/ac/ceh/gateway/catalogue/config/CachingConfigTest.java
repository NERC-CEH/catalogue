package uk.ac.ceh.gateway.catalogue.config;

import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;

public class CachingConfigTest {
    private CachingConfig config;
    
    @Before
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
        assertNotNull("Excepted a capabilities cache to be present", cache);
    }
}
