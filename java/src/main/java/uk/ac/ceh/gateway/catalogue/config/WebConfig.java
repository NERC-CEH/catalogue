package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUserHandlerMethodArgumentResolver;
import uk.ac.ceh.gateway.catalogue.citation.Citation;
import uk.ac.ceh.gateway.catalogue.converters.Object2TemplatedMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.TransparentProxyMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.WmsFeatureInfo2XmlMessageConverter;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteResponse;
import uk.ac.ceh.gateway.catalogue.model.CodeDocument;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.document.writing.MessageConverterWritingService;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Network;
import uk.ac.ceh.gateway.catalogue.ef.Programme;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpModel;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.CaseStudy;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.imp.ModelApplication;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModel;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModelUse;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.osdp.*;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.serviceagreement.History;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementModel;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementPermissionResource;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlResponse;
import uk.ac.ceh.gateway.catalogue.ukems.UkemsDocument;
import uk.ac.ceh.gateway.catalogue.wms.WmsFormatParameterFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.*;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final List<HttpMessageConverter<?>> beforeStandardMessageConverters;
    private final List<HttpMessageConverter<?>> afterStandardMessageConverters;

    public WebConfig(freemarker.template.Configuration freemarkerConfiguration) {
        val activity = new Object2TemplatedMessageConverter<>(Activity.class, freemarkerConfiguration);
        val agent = new Object2TemplatedMessageConverter<>(Agent.class, freemarkerConfiguration);
        val caseStudy = new Object2TemplatedMessageConverter<>(CaseStudy.class, freemarkerConfiguration);
        val cehModel = new Object2TemplatedMessageConverter<>(CehModel.class, freemarkerConfiguration);
        val cehModelApplication = new Object2TemplatedMessageConverter<>(CehModelApplication.class, freemarkerConfiguration);
        val citation = new Object2TemplatedMessageConverter<>(Citation.class, freemarkerConfiguration);
        val datacite = new Object2TemplatedMessageConverter<>(DataciteResponse.class, freemarkerConfiguration);
        val code = new Object2TemplatedMessageConverter<>(CodeDocument.class, freemarkerConfiguration);
        val dataset = new Object2TemplatedMessageConverter<>(Dataset.class, freemarkerConfiguration);
        val dataType = new Object2TemplatedMessageConverter<>(DataType.class, freemarkerConfiguration);
        val elter = new Object2TemplatedMessageConverter<>(ElterDocument.class, freemarkerConfiguration);
        val erammpDatacube = new Object2TemplatedMessageConverter<>(ErammpDatacube.class, freemarkerConfiguration);
        val erammpModel = new Object2TemplatedMessageConverter<>(ErammpModel.class, freemarkerConfiguration);
        val infrastructureRecord = new Object2TemplatedMessageConverter<>(InfrastructureRecord.class, freemarkerConfiguration);
        val errorResponse = new Object2TemplatedMessageConverter<>(ErrorResponse.class, freemarkerConfiguration);
        val facility = new Object2TemplatedMessageConverter<>(Facility.class, freemarkerConfiguration);
        val gemini = new Object2TemplatedMessageConverter<>(GeminiDocument.class, freemarkerConfiguration);
        val history = new Object2TemplatedMessageConverter<>(History.class, freemarkerConfiguration);
        val link = new Object2TemplatedMessageConverter<>(LinkDocument.class, freemarkerConfiguration);
        val maintenanceResponse = new Object2TemplatedMessageConverter<>(MaintenanceResponse.class, freemarkerConfiguration);
        val model = new Object2TemplatedMessageConverter<>(Model.class, freemarkerConfiguration);
        val modelApplication = new Object2TemplatedMessageConverter<>(ModelApplication.class, freemarkerConfiguration);
        val monitoringActivity = new Object2TemplatedMessageConverter<>(MonitoringActivity.class, freemarkerConfiguration);
        val monitoringFacility = new Object2TemplatedMessageConverter<>(MonitoringFacility.class, freemarkerConfiguration);
        val monitoringNetwork = new Object2TemplatedMessageConverter<>(MonitoringNetwork.class, freemarkerConfiguration);
        val monitoringProgramme = new Object2TemplatedMessageConverter<>(MonitoringProgramme.class, freemarkerConfiguration);
        val network = new Object2TemplatedMessageConverter<>(Network.class, freemarkerConfiguration);
        val nercModel = new Object2TemplatedMessageConverter<>(NercModel.class, freemarkerConfiguration);
        val nercModelUse = new Object2TemplatedMessageConverter<>(NercModelUse.class, freemarkerConfiguration);
        val osdpModel = new Object2TemplatedMessageConverter<>(uk.ac.ceh.gateway.catalogue.osdp.Model.class, freemarkerConfiguration);
        val permissionResource = new Object2TemplatedMessageConverter<>(PermissionResource.class, freemarkerConfiguration);
        val programme = new Object2TemplatedMessageConverter<>(Programme.class, freemarkerConfiguration);
        val publication = new Object2TemplatedMessageConverter<>(Publication.class, freemarkerConfiguration);
        val sample = new Object2TemplatedMessageConverter<>(Sample.class, freemarkerConfiguration);
        val sampleArchive = new Object2TemplatedMessageConverter<>(SampleArchive.class, freemarkerConfiguration);
        val searchResults = new Object2TemplatedMessageConverter<>(SearchResults.class, freemarkerConfiguration);
        val serviceAgreementModel = new Object2TemplatedMessageConverter<>(ServiceAgreementModel.class, freemarkerConfiguration);
        val serviceAgreementPermissionResource = new Object2TemplatedMessageConverter<>(ServiceAgreementPermissionResource.class, freemarkerConfiguration);
        val sparqlResponse = new Object2TemplatedMessageConverter<>(SparqlResponse.class, freemarkerConfiguration);
        val stateResource = new Object2TemplatedMessageConverter<>(StateResource.class, freemarkerConfiguration);
        val ukems = new Object2TemplatedMessageConverter<>(UkemsDocument.class, freemarkerConfiguration);
        val validationResponse = new Object2TemplatedMessageConverter<>(ValidationResponse.class, freemarkerConfiguration);
        val wmsFeatureInfo = new WmsFeatureInfo2XmlMessageConverter();

        this.beforeStandardMessageConverters = Arrays.asList(
            datacite,
            gemini,
            wmsFeatureInfo
        );
        this.afterStandardMessageConverters = Arrays.asList(
            activity,
            agent,
            caseStudy,
            cehModel,
            cehModelApplication,
            citation,
            code,
            dataset,
            dataType,
            elter,
            erammpDatacube,
            erammpModel,
            infrastructureRecord,
            errorResponse,
            facility,
            history,
            link,
            maintenanceResponse,
            model,
            modelApplication,
            monitoringActivity,
            monitoringFacility,
            monitoringNetwork,
            monitoringProgramme,
            network,
            nercModel,
            nercModelUse,
            osdpModel,
            permissionResource,
            programme,
            publication,
            sample,
            sampleArchive,
            searchResults,
            serviceAgreementModel,
            serviceAgreementPermissionResource,
            sparqlResponse,
            stateResource,
            ukems,
            validationResponse
        );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new ActiveUserHandlerMethodArgumentResolver());
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Before standard Spring message converters
        converters.addAll(0, beforeStandardMessageConverters);
        // Cannot add to beforeStandardMessageConverters as need to call 'httpClient()' once bean created
        converters.add(0, new TransparentProxyMessageConverter(httpClient()));
        // After standard Spring message converters
        converters.addAll(afterStandardMessageConverters);
    }

    @Bean
    public DocumentWritingService documentWritingService(List<HttpMessageConverter<?>> messageConverters) {
        val allMessageConverters = Stream.concat(
            beforeStandardMessageConverters.stream(),
            afterStandardMessageConverters.stream()
        ).collect(Collectors.toList());
        allMessageConverters.addAll(messageConverters);
        return new MessageConverterWritingService(allMessageConverters);
    }

    @Bean
    public FilterRegistrationBean<WmsFormatParameterFilter> wmsFormatParameterFilter() {
        FilterRegistrationBean<WmsFormatParameterFilter> registrationBean
            = new FilterRegistrationBean<>();
        registrationBean.setFilter(new WmsFormatParameterFilter());
        registrationBean.addUrlPatterns("/maps/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
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
        /*
         * Document types just producing json format do not need to register
         * a media type can just append ?format=json to url
         */
        configurer
            .favorParameter(true)
            .mediaType(BIBTEX_SHORT, BIBTEX)
            .mediaType(CSV_SHORT, TEXT_CSV)
            .mediaType(DATACITE_SHORT, DATACITE_XML)
            .mediaType(EF_INSPIRE_XML_SHORT, EF_INSPIRE_XML)
            .mediaType(GEMINI_XML_SHORT, GEMINI_XML)
            .mediaType("html", MediaType.TEXT_HTML)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType(RDF_SCHEMAORG_SHORT, RDF_SCHEMAORG_JSON)
            .mediaType(CEDA_YAML_SHORT, CEDA_YAML_JSON)
            .mediaType(RDF_TTL_SHORT, RDF_TTL)
            .mediaType(RESEARCH_INFO_SYSTEMS_SHORT, RESEARCH_INFO_SYSTEMS);
    }
}
