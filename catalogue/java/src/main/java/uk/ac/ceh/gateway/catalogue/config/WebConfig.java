package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUserHandlerMethodArgumentResolver;
import uk.ac.ceh.gateway.catalogue.converters.Object2TemplatedMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2WmsCapabilitiesMessageConverter;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.converters.TransparentProxyMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.UkeofXml2EFDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Network;
import uk.ac.ceh.gateway.catalogue.ef.Programme;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.imp.ModelApplication;
import uk.ac.ceh.gateway.catalogue.lake.LakeDocument;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.model.ErrorResponse;
import uk.ac.ceh.gateway.catalogue.model.MaintenanceResponse;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource;
import uk.ac.ceh.gateway.catalogue.model.SparqlResponse;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DownloadOrderDetailsService;
import uk.ac.ceh.gateway.catalogue.services.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;

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
    
    @Value("${template.location}") File templates;
    @Autowired ObjectMapper mapper;
    @Autowired CodeLookupService codesLookup;
    @Autowired JenaLookupService jenaLookupService;
    @Autowired DownloadOrderDetailsService downloadOrderDetailsService;
    @Autowired PermissionService permissionService;
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(mapper);
        
        // EF Message Converters  
        converters.add(new Object2TemplatedMessageConverter(Activity.class,  configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(Facility.class,  configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(Network.class,   configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(Programme.class, configureFreeMarker().getConfiguration()));
        converters.add(new UkeofXml2EFDocumentMessageConverter());
        
        // IMP Message Converters
        converters.add(new Object2TemplatedMessageConverter(Model.class,            configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(ModelApplication.class, configureFreeMarker().getConfiguration()));
        
        // Lake Message Converters
        converters.add(new Object2TemplatedMessageConverter(LakeDocument.class, configureFreeMarker().getConfiguration()));
        
        // Gemini Message Converters
        converters.add(new Object2TemplatedMessageConverter(GeminiDocument.class,       configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(SearchResults.class,        configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(Citation.class,             configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(StateResource.class,        configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(PermissionResource.class,   configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(MaintenanceResponse.class,  configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(SparqlResponse.class,       configureFreeMarker().getConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(ErrorResponse.class,        configureFreeMarker().getConfiguration()));
        converters.add(new TransparentProxyMessageConverter(httpClient()));
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
            Map<String, Object> shared = new HashMap<>();
            shared.put("jena", jenaLookupService);
            shared.put("codes", codesLookup);
            shared.put("downloadOrderDetails", downloadOrderDetailsService);
            shared.put("permission", permissionService);
            
            freemarker.template.Configuration config = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_22);
            config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            config.setSharedVaribles(shared);
            config.setDefaultEncoding("UTF-8");
            config.setTemplateLoader(new FileTemplateLoader(templates));
            
            FreeMarkerConfigurer freemarkerConfig = new FreeMarkerConfigurer();
            freemarkerConfig.setConfiguration(config);
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
    
    @Bean
    public CloseableHttpClient httpClient() {
        PoolingHttpClientConnectionManager connPool = new PoolingHttpClientConnectionManager();
        connPool.setMaxTotal(100);
        connPool.setDefaultMaxPerRoute(20);
        
        return HttpClients.custom()
                          .setConnectionManager(connPool)
                          .build();
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
