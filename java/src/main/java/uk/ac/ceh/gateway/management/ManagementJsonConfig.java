package uk.ac.ceh.gateway.management;

import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Gives the actuator on the management port Spring's stock JSON types back.
 * <p>
 * The actuator runs in a child context with its own {@code @EnableWebMvc}, and that context applies every
 * {@link WebMvcConfigurer} it can see, the main context's {@code WebConfig} included. {@code WebConfig}
 * narrows the JSON converter to the record media types (dri-one #409), which left nothing able to write the
 * actuator's own {@code application/vnd.spring-boot.actuator.v3+json}: with no {@code Accept} header, as the
 * kubelet probes send, every health check was a 500 (dri-one #527).
 * <p>
 * Registered only in the child context, through
 * {@code META-INF/spring/org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration.imports},
 * so the application port keeps the narrowing. It lives outside {@code uk.ac.ceh.gateway.catalogue} so that
 * component scanning does not also load it into the main context. It must run after {@code WebConfig}, which
 * is ordered one ahead of the lowest precedence for that reason.
 */
@ManagementContextConfiguration(value = ManagementContextType.CHILD, proxyBeanMethods = false)
@Order(Ordered.LOWEST_PRECEDENCE)
public class ManagementJsonConfig implements WebMvcConfigurer {

    /** What {@link JacksonJsonHttpMessageConverter} supports out of the box. */
    static final List<MediaType> STOCK_JSON_MEDIA_TYPES = List.of(
        MediaType.APPLICATION_JSON,
        new MediaType("application", "*+json")
    );

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.configureMessageConverters(converter -> {
            if (converter.getClass() == JacksonJsonHttpMessageConverter.class) {
                ((JacksonJsonHttpMessageConverter) converter).setSupportedMediaTypes(STOCK_JSON_MEDIA_TYPES);
            }
        });
    }
}
