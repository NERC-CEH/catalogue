package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import uk.ac.ceh.gateway.catalogue.converters.Metadata2HtmlMessageConverter;

@Configuration
@EnableWebMvc
@EnableScheduling
@ComponentScan(basePackages = "uk.ac.ceh.gateway.catalogue")
public class WebConfig extends WebMvcConfigurerAdapter {
    @Autowired ObjectMapper mapper;
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(mapper);
        
        converters.add(new Metadata2HtmlMessageConverter(configureFreeMarker().getConfiguration()));
        converters.add(mappingJackson2HttpMessageConverter);
    }
    
    @Bean
    public FreeMarkerConfigurer configureFreeMarker() {
        FreeMarkerConfigurer freemarkerConfig = new FreeMarkerConfigurer();
        freemarkerConfig.setTemplateLoaderPath("WEB-INF/templates");
        return freemarkerConfig;
    }
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorPathExtension(false)
            .defaultContentType(Metadata2HtmlMessageConverter.HTML_MEDIATYPE)
            .favorParameter(true)
            .mediaType("html", Metadata2HtmlMessageConverter.HTML_MEDIATYPE)
            .mediaType("json", MediaType.APPLICATION_JSON);
    }
}