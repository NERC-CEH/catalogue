package uk.ac.ceh.gateway.catalogue.config;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPathExpressionException;

import com.google.common.collect.ImmutableMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
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
    public static final String RDF_TTL_SHORT                = "ttl";
    public static final String RDF_TTL_VALUE                = "text/turtle";
    public static final String RDF_SCHEMAORG_SHORT          = "schema.org";
    public static final String RDF_SCHEMAORG_VALUE          = "application/vnd.schemaorg.ld+json";
    public static final String RESEARCH_INFO_SYSTEMS_SHORT  = "ris";
    public static final String RESEARCH_INFO_SYSTEMS_VALUE  = "application/x-research-info-systems";
    public static final String DATACITE_XML_VALUE           = "application/x-datacite+xml";
    public static final String GEMINI_SHORT                 = "gemini";
    public static final String GEMINI_JSON_VALUE            = "application/gemini+json";
    public static final String MODEL_SHORT                  = "model";
    public static final String MODEL_JSON_VALUE             = "application/model+json";
    public static final String CEH_MODEL_SHORT              = "ceh-model";
    public static final String CEH_MODEL_JSON_VALUE         = "application/vnd.ceh.model+json";
    public static final String CEH_MODEL_APPLICATION_SHORT  = "ceh-model-application";
    public static final String CEH_MODEL_APPLICATION_JSON_VALUE = "application/vnd.ceh.model.application+json";
    public static final String LINKED_SHORT                 = "link";
    public static final String LINKED_JSON_VALUE            = "application/link+json";
    public static final String UKEOF_XML_SHORT              = "ukeof";
    public static final String UKEOF_XML_VALUE              = "application/ukeof+xml";
    public static final String EF_INSPIRE_XML_SHORT         = "efinspire";
    public static final String EF_INSPIRE_XML_VALUE         = "application/vnd.ukeof.inspire+xml";
    public static final String MAPSERVER_GML_VALUE          = "application/vnd.ogc.gml";
    public static final String OSDP_AGENT_JSON_VALUE        = "application/vnd.osdp.agent+json";
    public static final String OSDP_AGENT_SHORT             = "osdp-agent";
    public static final String OSDP_DATASET_JSON_VALUE      = "application/vnd.osdp.dataset+json";
    public static final String OSDP_DATASET_SHORT           = "osdp-dataset";
    public static final String OSDP_MODEL_JSON_VALUE        = "application/vnd.osdp.model+json";
    public static final String OSDP_MODEL_SHORT             = "osdp-model";
    public static final String OSDP_MONITORING_ACTIVITY_JSON_VALUE  = "application/vnd.osdp.monitoring-activity+json";
    public static final String OSDP_MONITORING_ACTIVITY_SHORT       = "osdp-monitoring-activity";
    public static final String OSDP_MONITORING_FACILITY_JSON_VALUE  = "application/vnd.osdp.monitoring-facility+json";
    public static final String OSDP_MONITORING_FACILITY_SHORT       = "osdp-monitoring-facility";
    public static final String OSDP_MONITORING_PROGRAMME_JSON_VALUE = "application/vnd.osdp.monitoring-programme+json";
    public static final String OSDP_MONITORING_PROGRAMME_SHORT      = "osdp-monitoring-programme";
    public static final String OSDP_PUBLICATION_JSON_VALUE  = "application/vnd.osdp.publication+json";
    public static final String OSDP_PUBLICATION_SHORT       = "osdp-publication";
    public static final String OSDP_SAMPLE_JSON_VALUE       = "application/vnd.osdp.sample+json";
    public static final String OSDP_SAMPLE_SHORT            = "osdp-sample";
    public static final String SAMPLE_ARCHIVE_SHORT         = "sample-archive";
    public static final String SAMPLE_ARCHIVE_JSON_VALUE         = "application/vnd.sample-archive+json";
    
    public static final String UPLOAD_DOCUMENT_JSON_VALUE = "application/vnd.upload-document+json";
    public static final String UPLOAD_DOCUMENT_SHORT = "Upload";

    public static final String DATA_TYPE_JSON_VALUE = "application/vnd.data-type+json";
    public static final String DATA_TYPE_SHORT = "data-type";
    
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
                        .put(RDF_TTL_SHORT, MediaType.parseMediaType(RDF_TTL_VALUE))
                        .put(RDF_SCHEMAORG_SHORT, MediaType.parseMediaType(RDF_SCHEMAORG_VALUE))
                        .put(BIBTEX_SHORT, MediaType.parseMediaType(BIBTEX_VALUE))
                        .put(RESEARCH_INFO_SYSTEMS_SHORT, MediaType.parseMediaType(RESEARCH_INFO_SYSTEMS_VALUE))
                        .put(CEH_MODEL_SHORT, MediaType.parseMediaType(CEH_MODEL_JSON_VALUE))
                        .put(CEH_MODEL_APPLICATION_SHORT, MediaType.parseMediaType(CEH_MODEL_APPLICATION_JSON_VALUE))
                        .put(OSDP_AGENT_SHORT, MediaType.parseMediaType(OSDP_AGENT_JSON_VALUE))
                        .put(OSDP_DATASET_SHORT, MediaType.parseMediaType(OSDP_DATASET_JSON_VALUE))
                        .put(OSDP_MODEL_SHORT, MediaType.parseMediaType(OSDP_MODEL_JSON_VALUE))
                        .put(OSDP_MONITORING_ACTIVITY_SHORT, MediaType.parseMediaType(OSDP_MONITORING_ACTIVITY_JSON_VALUE))
                        .put(OSDP_MONITORING_FACILITY_SHORT, MediaType.parseMediaType(OSDP_MONITORING_FACILITY_JSON_VALUE))
                        .put(OSDP_MONITORING_PROGRAMME_SHORT, MediaType.parseMediaType(OSDP_MONITORING_PROGRAMME_JSON_VALUE))
                        .put(OSDP_PUBLICATION_SHORT, MediaType.parseMediaType(OSDP_PUBLICATION_JSON_VALUE))
                        .put(OSDP_SAMPLE_SHORT, MediaType.parseMediaType(OSDP_SAMPLE_JSON_VALUE))
                        .put(SAMPLE_ARCHIVE_SHORT, MediaType.parseMediaType(SAMPLE_ARCHIVE_JSON_VALUE))
                        .put(UPLOAD_DOCUMENT_SHORT, MediaType.parseMediaType(UPLOAD_DOCUMENT_JSON_VALUE))
                        .put(DATA_TYPE_SHORT, MediaType.parseMediaType(DATA_TYPE_JSON_VALUE))
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
        registry
            .addResourceHandler("/static/**")
            .addResourceLocations("/static/")
            .setCacheControl(CacheControl.maxAge(2, TimeUnit.DAYS));
    }
}
