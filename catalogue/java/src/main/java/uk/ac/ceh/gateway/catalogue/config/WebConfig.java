package uk.ac.ceh.gateway.catalogue.config;

import java.util.Arrays;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUserHandlerMethodArgumentResolver;
import uk.ac.ceh.gateway.catalogue.config.ServiceConfig.MessageConvertersHolder;
import uk.ac.ceh.gateway.catalogue.converters.Xml2WmsCapabilitiesMessageConverter;

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
    public static final String UKEOF_XML_SHORT              = "ukeof";
    public static final String UKEOF_XML_VALUE              = "application/ukeof+xml";
    public static final String EF_INSPIRE_XML_SHORT         = "efinspire";
    public static final String EF_INSPIRE_XML_VALUE         = "application/vnd.ukeof.inspire+xml";
    
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
    
    @Bean
    public RestTemplate restTemplate() throws XPathExpressionException {
        RestTemplate toReturn = new RestTemplate();
        toReturn.setMessageConverters(Arrays.asList(
            new Xml2WmsCapabilitiesMessageConverter()
        ));
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
            .defaultContentType(MediaType.TEXT_HTML)
            .favorParameter(true)
            .mediaType("html", MediaType.TEXT_HTML)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType(GEMINI_SHORT, MediaType.parseMediaType(GEMINI_JSON_VALUE))
            .mediaType(GEMINI_XML_SHORT, MediaType.parseMediaType(GEMINI_XML_VALUE))
            .mediaType(UKEOF_XML_SHORT, MediaType.parseMediaType(UKEOF_XML_VALUE))
            .mediaType(EF_INSPIRE_XML_SHORT, MediaType.parseMediaType(EF_INSPIRE_XML_VALUE))
            .mediaType(RDF_XML_SHORT, MediaType.parseMediaType(RDF_XML_VALUE))
            .mediaType(BIBTEX_SHORT, MediaType.parseMediaType(BIBTEX_VALUE))
            .mediaType(RESEARCH_INFO_SYSTEMS_SHORT, MediaType.parseMediaType(RESEARCH_INFO_SYSTEMS_VALUE));
    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new ActiveUserHandlerMethodArgumentResolver());
    }
}
