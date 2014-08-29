package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.cache.FileTemplateLoader;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUserHandlerMethodArgumentResolver;
import uk.ac.ceh.gateway.catalogue.converters.Object2TemplatedMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2WmsCapabilitiesMessageConverter;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.DocumentSearchResults;
import uk.ac.ceh.gateway.catalogue.ukeof.UKEOFDocument;

@Configuration
@EnableWebMvc
@EnableScheduling
@ComponentScan(basePackages = "uk.ac.ceh.gateway.catalogue")
public class WebConfig extends WebMvcConfigurerAdapter {
    @Value("${template.location}") File templates;
    @Autowired ObjectMapper mapper;
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(mapper);
        
        converters.add(new Object2TemplatedMessageConverter(GeminiDocument.class, configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(UKEOFDocument.class, configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(DocumentSearchResults.class, configureFreeMarker().getConfiguration()));
        converters.add(new ResourceHttpMessageConverter());
        converters.add(mappingJackson2HttpMessageConverter);
    }
    
    @Bean
    public FreeMarkerViewResolver configureFreeMarkerViewResolver() {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setCache(true);
        resolver.setContentType("text/html;charset=UTF-8");
        return resolver;
    }
    
    @Bean
    public FreeMarkerConfigurer configureFreeMarker() {
        try {
            FreeMarkerConfigurer freemarkerConfig = new FreeMarkerConfigurer();

            //freemarkerConfig.setTemplateLoaderPath(templates);
            freemarkerConfig.setPreTemplateLoaders(new FileTemplateLoader(templates));
            return freemarkerConfig;
        }
        catch(Exception e) {
            return null;
        }
    }
    
    @Bean
    public RestTemplate restTemplate() throws XPathExpressionException {
        RestTemplate toReturn = new RestTemplate();
        toReturn.setMessageConverters(Arrays.asList(
            new Xml2WmsCapabilitiesMessageConverter()
        ));
        return toReturn;
    }
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorPathExtension(false)
            .defaultContentType(MediaType.TEXT_HTML)
            .favorParameter(true)
            .mediaType("html", MediaType.TEXT_HTML)
            .mediaType("json", MediaType.APPLICATION_JSON);
    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new ActiveUserHandlerMethodArgumentResolver());
    }
}