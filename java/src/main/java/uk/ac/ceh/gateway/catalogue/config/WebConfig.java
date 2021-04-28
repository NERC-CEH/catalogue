package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.util.ForgivingParameterContentNegotiationStrategy;
import uk.ac.ceh.gateway.catalogue.util.WmsFormatContentNegotiationStrategy;

import java.util.List;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.*;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private freemarker.template.Configuration freemarkerConfiguration;

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
        converters.add(new Object2TemplatedMessageConverter<>(Citation.class, freemarkerConfiguration));
    }

    @Bean
    public ObjectMapper objectMapper() {
        val mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new GuavaModule())
            .registerModule(new JaxbAnnotationModule());
        log.info("Creating {}", mapper);
        return mapper;
    }

    @Bean
    public CloseableHttpClient httpClient() {
        log.info("Creating httpClient");
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
            .put("html", MediaType.TEXT_HTML)
            .put("json", MediaType.APPLICATION_JSON)
//            .put(BIBTEX_SHORT, MediaType.parseMediaType(BIBTEX_VALUE))
            .put(CEH_MODEL_APPLICATION_SHORT, MediaType.parseMediaType(CEH_MODEL_APPLICATION_JSON_VALUE))
            .put(CEH_MODEL_SHORT, MediaType.parseMediaType(CEH_MODEL_JSON_VALUE))
            .put(DATA_TYPE_SHORT, MediaType.parseMediaType(DATA_TYPE_JSON_VALUE))
            .put(EF_INSPIRE_XML_SHORT, MediaType.parseMediaType(EF_INSPIRE_XML_VALUE))
            .put(ERAMMP_DATACUBE_SHORT, MediaType.parseMediaType(ERAMMP_DATACUBE_JSON_VALUE))
            .put(ERAMMP_MODEL_SHORT, MediaType.parseMediaType(ERAMMP_MODEL_JSON_VALUE))
            .put(GEMINI_XML_SHORT, MediaType.parseMediaType(GEMINI_XML_VALUE))
            .put(OSDP_AGENT_SHORT, MediaType.parseMediaType(OSDP_AGENT_JSON_VALUE))
            .put(OSDP_DATASET_SHORT, MediaType.parseMediaType(OSDP_DATASET_JSON_VALUE))
            .put(OSDP_MODEL_SHORT, MediaType.parseMediaType(OSDP_MODEL_JSON_VALUE))
            .put(OSDP_MONITORING_ACTIVITY_SHORT, MediaType.parseMediaType(OSDP_MONITORING_ACTIVITY_JSON_VALUE))
            .put(OSDP_MONITORING_FACILITY_SHORT, MediaType.parseMediaType(OSDP_MONITORING_FACILITY_JSON_VALUE))
            .put(OSDP_MONITORING_PROGRAMME_SHORT, MediaType.parseMediaType(OSDP_MONITORING_PROGRAMME_JSON_VALUE))
            .put(OSDP_PUBLICATION_SHORT, MediaType.parseMediaType(OSDP_PUBLICATION_JSON_VALUE))
            .put(OSDP_SAMPLE_SHORT, MediaType.parseMediaType(OSDP_SAMPLE_JSON_VALUE))
            .put(RDF_TTL_SHORT, MediaType.parseMediaType(RDF_TTL_VALUE))
            .put(RDF_SCHEMAORG_SHORT, MediaType.parseMediaType(RDF_SCHEMAORG_VALUE))
//            .put(RESEARCH_INFO_SYSTEMS_SHORT, MediaType.parseMediaType(RESEARCH_INFO_SYSTEMS_VALUE))
            .put(SAMPLE_ARCHIVE_SHORT, MediaType.parseMediaType(SAMPLE_ARCHIVE_JSON_VALUE))
            .put(UPLOAD_DOCUMENT_SHORT, MediaType.parseMediaType(UPLOAD_DOCUMENT_JSON_VALUE))
            .put(UKEOF_XML_SHORT, MediaType.parseMediaType(UKEOF_XML_VALUE))
            .build();

        val forgivingContent = new ForgivingParameterContentNegotiationStrategy(mediaTypes);
        val wms = new WmsFormatContentNegotiationStrategy("INFO_FORMAT");
        val headerContent = new HeaderContentNegotiationStrategy();
        val fixedContent = new FixedContentNegotiationStrategy(MediaType.TEXT_HTML);

        configurer
            .favorParameter(true)
            .mediaType(BIBTEX_SHORT, MediaType.parseMediaType(BIBTEX_VALUE))
            .mediaType(RESEARCH_INFO_SYSTEMS_SHORT, MediaType.parseMediaType(RESEARCH_INFO_SYSTEMS_VALUE));
//            .strategies(Arrays.asList(
//                forgivingContent,
//                wms,
//                headerContent,
//                fixedContent
//            ));
    }
}
