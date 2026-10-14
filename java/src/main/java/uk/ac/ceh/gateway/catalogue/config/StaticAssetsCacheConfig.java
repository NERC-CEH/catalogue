package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * Lets browsers keep webpack's output for a year without asking again. Every file under these paths
 * has a hash of its content in its name, so a deploy that changes one gives it a new URL, which the
 * page then links ({@code StaticAssets}, NERC-CEH/dri-one#364). The rest of /static (images, which
 * keep their names) is still served by Spring Boot's own handler, revalidated by Last-Modified.
 */
@Configuration
public class StaticAssetsCacheConfig implements WebMvcConfigurer {
    static final CacheControl IMMUTABLE = CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable();
    static final String[] HASHED = {"scripts", "css", "assets"};

    private final String staticLocation;

    public StaticAssetsCacheConfig(@Value("${spring.web.resources.static-locations}") String staticLocation) {
        this.staticLocation = staticLocation.endsWith("/") ? staticLocation : staticLocation + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        for (String directory : HASHED) {
            registry.addResourceHandler("/static/" + directory + "/**")
                .addResourceLocations(staticLocation + directory + "/")
                .setCacheControl(IMMUTABLE);
        }
    }
}
