package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUserHandlerMethodArgumentResolver;
import uk.ac.ceh.gateway.catalogue.converters.Object2TemplatedMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.TransparentProxyMessageConverter;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteResponse;
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
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.util.ForgivingParameterContentNegotiationStrategy;
import uk.ac.ceh.gateway.catalogue.util.WmsFormatContentNegotiationStrategy;

import java.util.Arrays;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.*;

@Slf4j
@Configuration
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final freemarker.template.Configuration freemarkerConfiguration;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new ActiveUserHandlerMethodArgumentResolver());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("adding static resource handler");
        registry
            .addResourceHandler("/static/**")
            .addResourceLocations("file:/opt/ceh-catalogue/static/");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("Adding message converters");
        // Before standard Spring message converters
        converters.add(0, new Object2TemplatedMessageConverter<>(DataciteResponse.class, freemarkerConfiguration));
        converters.add(0, new Object2TemplatedMessageConverter<>(GeminiDocument.class, freemarkerConfiguration));
        converters.add(0, new TransparentProxyMessageConverter(httpClient()));

        // After standard Spring message converters
        converters.add(new Object2TemplatedMessageConverter<>(Activity.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(CaseStudy.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(CehModel.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(CehModelApplication.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Citation.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(DataType.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ErammpModel.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ErammpDatacube.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ErrorResponse.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Facility.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(LinkDocument.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(MaintenanceResponse.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Model.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ModelApplication.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Network.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Programme.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(SearchResults.class, freemarkerConfiguration));

        if (log.isDebugEnabled()) {
            log.debug("After our message converters added");
            converters.forEach(convert -> log.debug(convert.toString()));
        }
    }

    @Bean
    public ObjectMapper objectMapper() {
        val mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new GuavaModule())
            .registerModule(new JaxbAnnotationModule());
        log.info("Creating ObjectMapper");
        return mapper;
    }

    @Bean
    public CloseableHttpClient httpClient() {
        log.info("Creating HttpClient");
        val connPool = new PoolingHttpClientConnectionManager();
        connPool.setMaxTotal(100);
        connPool.setDefaultMaxPerRoute(20);

        return HttpClients.custom()
            .setConnectionManager(connPool)
            .build();
    }


//    @Bean
//    public CommonsMultipartResolver multipartResolver() {
//        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
//        resolver.setDefaultEncoding("utf-8");
//        return resolver;
//    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        log.info("configuring Content Negotiation");

        val mediaTypes = ImmutableMap.<String, MediaType>builder()



            .put(DATA_TYPE_SHORT, MediaType.parseMediaType(DATA_TYPE_JSON_VALUE))
            .put(EF_INSPIRE_XML_SHORT, MediaType.parseMediaType(EF_INSPIRE_XML_VALUE))

            .put(GEMINI_XML_SHORT, MediaType.parseMediaType(GEMINI_XML_VALUE))
            .put(OSDP_AGENT_SHORT, MediaType.parseMediaType(OSDP_AGENT_JSON_VALUE))
            .put(OSDP_DATASET_SHORT, MediaType.parseMediaType(OSDP_DATASET_JSON_VALUE))
            .put(OSDP_MODEL_SHORT, MediaType.parseMediaType(OSDP_MODEL_JSON_VALUE))
            .put(OSDP_MONITORING_ACTIVITY_SHORT, MediaType.parseMediaType(OSDP_MONITORING_ACTIVITY_JSON_VALUE))
            .put(OSDP_MONITORING_FACILITY_SHORT, MediaType.parseMediaType(OSDP_MONITORING_FACILITY_JSON_VALUE))
            .put(OSDP_MONITORING_PROGRAMME_SHORT, MediaType.parseMediaType(OSDP_MONITORING_PROGRAMME_JSON_VALUE))
            .put(OSDP_PUBLICATION_SHORT, MediaType.parseMediaType(OSDP_PUBLICATION_JSON_VALUE))
            .put(OSDP_SAMPLE_SHORT, MediaType.parseMediaType(OSDP_SAMPLE_JSON_VALUE))
            .put(RESEARCH_INFO_SYSTEMS_SHORT, MediaType.parseMediaType(RESEARCH_INFO_SYSTEMS_VALUE))
            .put(RDF_SCHEMAORG_SHORT, MediaType.parseMediaType(RDF_SCHEMAORG_VALUE))
            .put(RDF_TTL_SHORT, MediaType.parseMediaType(RDF_TTL_VALUE))
            .put(SAMPLE_ARCHIVE_SHORT, MediaType.parseMediaType(SAMPLE_ARCHIVE_JSON_VALUE))
            .put(UPLOAD_DOCUMENT_SHORT, MediaType.parseMediaType(UPLOAD_DOCUMENT_JSON_VALUE))
            .build();

        val forgivingParameterStrategy = new ForgivingParameterContentNegotiationStrategy(mediaTypes);
        val wmsStrategy = new WmsFormatContentNegotiationStrategy("INFO_FORMAT");
        val headerStrategy = new HeaderContentNegotiationStrategy();
        val fixedStrategy = new FixedContentNegotiationStrategy(MediaType.TEXT_HTML);

        val strategies = Arrays.asList(
                forgivingParameterStrategy,
                wmsStrategy,
                headerStrategy,
                fixedStrategy
        );

        configurer
            .favorParameter(true)
            .mediaType(BIBTEX_SHORT, BIBTEX)
            .mediaType(CEH_MODEL_SHORT, CEH_MODEL_JSON)
            .mediaType(CEH_MODEL_APPLICATION_SHORT, CEH_MODEL_APPLICATION_JSON)
            .mediaType(DATACITE_SHORT, DATACITE_XML)
            .mediaType(DATA_TYPE_SHORT, DATA_TYPE_JSON)
            .mediaType(EF_INSPIRE_XML_SHORT, EF_INSPIRE_XML)
            .mediaType(ERAMMP_DATACUBE_SHORT, ERAMMP_DATACUBE_JSON)
            .mediaType(ERAMMP_MODEL_SHORT, ERAMMP_MODEL_JSON)
            .mediaType(GEMINI_JSON_SHORT, GEMINI_JSON)
            .mediaType(GEMINI_XML_SHORT, GEMINI_XML)
            .mediaType("html", MediaType.TEXT_HTML)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType(RDF_SCHEMAORG_SHORT, RDF_SCHEMAORG_JSON)
            .mediaType(RDF_TTL_SHORT, RDF_TTL)
            .mediaType(RESEARCH_INFO_SYSTEMS_SHORT, RESEARCH_INFO_SYSTEMS);
    }
}
