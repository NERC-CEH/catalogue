package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
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
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUserHandlerMethodArgumentResolver;
import uk.ac.ceh.gateway.catalogue.converters.Object2TemplatedMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.TransparentProxyMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.WmsFeatureInfo2XmlMessageConverter;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteResponse;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Network;
import uk.ac.ceh.gateway.catalogue.ef.Programme;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
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
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;

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
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("Adding message converters");
        // Before standard Spring message converters
        converters.add(0, new Object2TemplatedMessageConverter<>(DataciteResponse.class, freemarkerConfiguration));
        converters.add(0, new Object2TemplatedMessageConverter<>(GeminiDocument.class, freemarkerConfiguration));
        converters.add(0, new TransparentProxyMessageConverter(httpClient()));
        converters.add(0, new WmsFeatureInfo2XmlMessageConverter());

        // After standard Spring message converters
        converters.add(new Object2TemplatedMessageConverter<>(Activity.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Agent.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(CaseStudy.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(CehModel.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(CehModelApplication.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Citation.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Dataset.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(DataType.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ElterDocument.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ErammpModel.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ErammpDatacube.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ErrorResponse.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Facility.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(LinkDocument.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(MaintenanceResponse.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Model.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(uk.ac.ceh.gateway.catalogue.osdp.Model.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ModelApplication.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(MonitoringActivity.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(MonitoringFacility.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(MonitoringProgramme.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Network.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(PermissionResource.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Programme.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Publication.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(Sample.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(SampleArchive.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(SearchResults.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ServiceAgreement.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(SparqlResponse.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(StateResource.class, freemarkerConfiguration));
        converters.add(new Object2TemplatedMessageConverter<>(ValidationResponse.class, freemarkerConfiguration));

        if (log.isDebugEnabled()) {
            log.debug("After our message converters added");
            converters.forEach(convert -> log.debug(convert.toString()));
        }
    }

    @Bean
    public Module guavaModule() {
        return new GuavaModule();
    }

    @Bean Module jaxbAnnotationModule() {
        return new JaxbAnnotationModule();
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

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        log.info("configuring Content Negotiation");
        configurer
            .favorParameter(true)
            .mediaType(BIBTEX_SHORT, BIBTEX)
            .mediaType(CEH_MODEL_SHORT, CEH_MODEL_JSON)
            .mediaType(CEH_MODEL_APPLICATION_SHORT, CEH_MODEL_APPLICATION_JSON)
            .mediaType(CSV_SHORT, TEXT_CSV)
            .mediaType(DATACITE_SHORT, DATACITE_XML)
            .mediaType(DATA_TYPE_SHORT, DATA_TYPE_JSON)
            .mediaType(EF_INSPIRE_XML_SHORT, EF_INSPIRE_XML)
            .mediaType(ERAMMP_DATACUBE_SHORT, ERAMMP_DATACUBE_JSON)
            .mediaType(ERAMMP_MODEL_SHORT, ERAMMP_MODEL_JSON)
            .mediaType(ELTER_SHORT, ELTER_JSON)
            .mediaType(GEMINI_JSON_SHORT, GEMINI_JSON)
            .mediaType(GEMINI_XML_SHORT, GEMINI_XML)
            .mediaType("html", MediaType.TEXT_HTML)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType(LINKED_SHORT, LINKED_JSON)
            .mediaType(MODEL_SHORT, MODEL_JSON)
            .mediaType(OSDP_AGENT_SHORT, OSDP_AGENT_JSON)
            .mediaType(OSDP_DATASET_SHORT, OSDP_DATASET_JSON)
            .mediaType(OSDP_MODEL_SHORT, OSDP_MODEL_JSON)
            .mediaType(OSDP_MONITORING_ACTIVITY_SHORT, OSDP_MONITORING_ACTIVITY_JSON)
            .mediaType(OSDP_MONITORING_FACILITY_SHORT, OSDP_MONITORING_FACILITY_JSON)
            .mediaType(OSDP_MONITORING_PROGRAMME_SHORT, OSDP_MONITORING_PROGRAMME_JSON)
            .mediaType(OSDP_PUBLICATION_SHORT, OSDP_PUBLICATION_JSON)
            .mediaType(OSDP_SAMPLE_SHORT, OSDP_SAMPLE_JSON)
            .mediaType(RDF_SCHEMAORG_SHORT, RDF_SCHEMAORG_JSON)
            .mediaType(RDF_TTL_SHORT, RDF_TTL)
            .mediaType(RESEARCH_INFO_SYSTEMS_SHORT, RESEARCH_INFO_SYSTEMS)
            .mediaType(SAMPLE_ARCHIVE_SHORT, SAMPLE_ARCHIVE_JSON);
    }
}
