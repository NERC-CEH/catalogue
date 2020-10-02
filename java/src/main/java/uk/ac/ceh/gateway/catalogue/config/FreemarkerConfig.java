package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.TemplateExceptionHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import uk.ac.ceh.gateway.catalogue.converters.Object2TemplatedMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.TransparentProxyMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.UkeofXml2EFDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.WmsFeatureInfo2XmlMessageConverter;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Network;
import uk.ac.ceh.gateway.catalogue.ef.Programme;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpModel;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.CaseStudy;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.imp.ModelApplication;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.osdp.*;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.services.*;
import uk.ac.ceh.gateway.catalogue.templateHelpers.GeminiExtractor;
import uk.ac.ceh.gateway.catalogue.upload.UploadDocument;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Configuration
public class FreemarkerConfig {
    @Value("${template.location}") private File templates;

    @Autowired private CatalogueService catalogueService;
    @Autowired private CodeLookupService codeLookupService;
    @Autowired private DownloadOrderDetailsService downloadOrderDetailsService;
    @Autowired private GeminiExtractor geminiExtractor;
    @Autowired private ObjectMapper jacksonMapper;
    @Autowired private JiraService jiraService;
    @Autowired private JenaLookupService jenaLookupService;
    @Autowired private MapServerDetailsService mapServerDetailsService;
    @Autowired private MetadataQualityService metadataQualityService;
    @Autowired private PermissionService permissionService;

    @Bean
    @SneakyThrows
    public freemarker.template.Configuration freemarkerConfiguration() {
        Map<String, Object> shared = new HashMap<>();
        shared.put("jena", Objects.requireNonNull(jenaLookupService));
        shared.put("codes", Objects.requireNonNull(codeLookupService));
        shared.put("downloadOrderDetails", downloadOrderDetailsService);
        shared.put("permission", Objects.requireNonNull(permissionService));
        shared.put("jira", jiraService);
        shared.put("mapServerDetails", mapServerDetailsService);
        shared.put("geminiHelper", Objects.requireNonNull(geminiExtractor));
        shared.put("catalogues", Objects.requireNonNull(catalogueService));
        shared.put("metadataQuality", Objects.requireNonNull(metadataQualityService));
        log.info("Freemarker shared variables:");
        shared.forEach((key, value) -> log.info("{}: {}", key, value));

        freemarker.template.Configuration config = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_22);
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setSharedVaribles(shared);
        config.setDefaultEncoding("UTF-8");
        config.setTemplateLoader(new FileTemplateLoader(templates));
        return config;
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
        freemarkerConfig.setConfiguration(freemarkerConfiguration());
        return freemarkerConfig;
    }

    @Bean
    @Qualifier("writing")
    public List<HttpMessageConverter<?>> messageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(jacksonMapper);

        // EF Message Converters
        converters.add(new Object2TemplatedMessageConverter<>(Activity.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Facility.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Network.class,  freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Programme.class, freemarkerConfiguration()));
        converters.add(new UkeofXml2EFDocumentMessageConverter());

        // IMP Message Converters
        converters.add(new Object2TemplatedMessageConverter<>(Model.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(ModelApplication.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(CaseStudy.class, freemarkerConfiguration()));

        // CEH model catalogue
        converters.add(new Object2TemplatedMessageConverter<>(CehModel.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(CehModelApplication.class, freemarkerConfiguration()));

        //OSDP
        converters.add(new Object2TemplatedMessageConverter<>(Agent.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(uk.ac.ceh.gateway.catalogue.osdp.Dataset.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(uk.ac.ceh.gateway.catalogue.osdp.Model.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(MonitoringActivity.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(MonitoringFacility.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(MonitoringProgramme.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Publication.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Sample.class, freemarkerConfiguration()));

        //ERAMMP
        converters.add(new Object2TemplatedMessageConverter<>(ErammpModel.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(ErammpDatacube.class, freemarkerConfiguration()));

        //Sample Archive
        converters.add(new Object2TemplatedMessageConverter<>(SampleArchive.class, freemarkerConfiguration()));

        // Gemini Message Converters
        converters.add(new Object2TemplatedMessageConverter<>(GeminiDocument.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(LinkDocument.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(SearchResults.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Citation.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(StateResource.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(PermissionResource.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(CatalogueResource.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(MaintenanceResponse.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(SparqlResponse.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(ValidationResponse.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(ErrorResponse.class, freemarkerConfiguration()));
        converters.add(new TransparentProxyMessageConverter(httpClient()));
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        converters.add(new WmsFeatureInfo2XmlMessageConverter());
        converters.add(mappingJackson2HttpMessageConverter);

        converters.add(new Object2TemplatedMessageConverter<>(UploadDocument.class, freemarkerConfiguration()));

        converters.add(new Object2TemplatedMessageConverter<>(DataType.class, freemarkerConfiguration()));

        return converters;
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
}
