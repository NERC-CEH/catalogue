package uk.ac.ceh.gateway.catalogue.config;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUserHandlerMethodArgumentResolver;
import uk.ac.ceh.gateway.catalogue.config.ServiceConfig.MessageConvertersHolder;
import uk.ac.ceh.gateway.catalogue.converters.Gml2WmsFeatureInfoMessageConverter;
import uk.ac.ceh.gateway.catalogue.util.ForgivingParameterContentNegotiationStrategy;
import uk.ac.ceh.gateway.catalogue.util.MapServerGetFeatureInfoErrorHandler;
import uk.ac.ceh.gateway.catalogue.util.WmsFormatContentNegotiationStrategy;

@Configuration
@EnableWebMvc
@EnableScheduling
@EnableCaching
@ComponentScan(basePackages = "uk.ac.ceh.gateway.catalogue")
public class WebConfig extends WebMvcConfigurerAdapter {
    public static final String BIBTEX_SHORT                 = "bib";
    public static final String BIBTEX_VALUE                 = "application/x-bibtex";
    public static final String GEMINI_XML_SHORT             = "gemini";
    public static final String GEMINI_XML_VALUE             = "application/x-gemini+xml";
    public static final String RDF_XML_SHORT                = "rdfxml";
    public static final String RDF_XML_VALUE                = "application/rdf+xml";
    public static final String RESEARCH_INFO_SYSTEMS_SHORT  = "ris";
    public static final String RESEARCH_INFO_SYSTEMS_VALUE  = "application/x-research-info-systems";
    public static final String DATACITE_XML_VALUE           = "application/x-datacite+xml";
    public static final String GEMINI_SHORT                 = "gemini";
    public static final String GEMINI_JSON_VALUE            = "application/gemini+json";
    public static final String MODEL_SHORT                  = "model";
    public static final String MODEL_JSON_VALUE             = "application/model+json";
    public static final String CEH_MODEL_SHORT              = "ceh-model";
    public static final String CEH_MODEL_JSON_VALUE         = "application/vnd.ceh.model+json";
    public static final String LINKED_SHORT                 = "link";
    public static final String LINKED_JSON_VALUE            = "application/link+json";
    public static final String UKEOF_XML_SHORT              = "ukeof";
    public static final String UKEOF_XML_VALUE              = "application/ukeof+xml";
    public static final String EF_INSPIRE_XML_SHORT         = "efinspire";
    public static final String EF_INSPIRE_XML_VALUE         = "application/vnd.ukeof.inspire+xml";
    public static final String MAPSERVER_GML_VALUE          = "application/vnd.ogc.gml";
    
    @Autowired MessageConvertersHolder messageConvertersHolder;
    @Autowired freemarker.template.Configuration freemarkerConfiguration;
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addAll(messageConvertersHolder.getConverters());
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
        FreeMarkerConfigurer freemarkerConfig = new FreeMarkerConfigurer();
        freemarkerConfig.setConfiguration(freemarkerConfiguration);
        return freemarkerConfig;
    }

    @Bean(name="getfeatureinfo-rest")
    public RestTemplate getFeatureInfoRestTemplate() throws XPathExpressionException {
        RestTemplate toReturn = new RestTemplate();
        toReturn.setMessageConverters(Arrays.asList(
                new Gml2WmsFeatureInfoMessageConverter()
        ));
        toReturn.setErrorHandler(new MapServerGetFeatureInfoErrorHandler());
        return toReturn;
    }
    
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver=new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorPathExtension(false)
            .ignoreAcceptHeader(true) // Define accept header handling manually
            .defaultContentTypeStrategy(new ContentNegotiationManager(
                    new ForgivingParameterContentNegotiationStrategy(ImmutableMap.<String, MediaType>builder()
                        .put("html", MediaType.TEXT_HTML)
                        .put("json", MediaType.APPLICATION_JSON)
                        .put(GEMINI_XML_SHORT, MediaType.parseMediaType(GEMINI_XML_VALUE))
                        .put(UKEOF_XML_SHORT, MediaType.parseMediaType(UKEOF_XML_VALUE))
                        .put(EF_INSPIRE_XML_SHORT, MediaType.parseMediaType(EF_INSPIRE_XML_VALUE))
                        .put(RDF_XML_SHORT, MediaType.parseMediaType(RDF_XML_VALUE))
                        .put(BIBTEX_SHORT, MediaType.parseMediaType(BIBTEX_VALUE))
                        .put(RESEARCH_INFO_SYSTEMS_SHORT, MediaType.parseMediaType(RESEARCH_INFO_SYSTEMS_VALUE))
                        .build()
                    ),
                    new WmsFormatContentNegotiationStrategy("INFO_FORMAT"), // GetFeatureInfo
                    new HeaderContentNegotiationStrategy(),
                    new FixedContentNegotiationStrategy(MediaType.TEXT_HTML)
            ));
    }
    
    
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new ActiveUserHandlerMethodArgumentResolver());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
    }
}
