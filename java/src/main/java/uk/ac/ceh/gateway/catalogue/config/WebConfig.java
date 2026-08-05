package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUserHandlerMethodArgumentResolver;
import uk.ac.ceh.gateway.catalogue.citation.Citation;
import uk.ac.ceh.gateway.catalogue.converters.Object2TemplatedMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.TransparentProxyMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.WmsFeatureInfo2XmlMessageConverter;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsReportModel;
import uk.ac.ceh.gateway.catalogue.model.CodeDocument;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.document.writing.MessageConverterWritingService;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.maintenance.AdminDeleteResponse;
import uk.ac.ceh.gateway.catalogue.maintenance.MaintenanceResponse;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModel;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModelUse;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
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
        val cehModel = new Object2TemplatedMessageConverter<>(CehModel.class, freemarkerConfiguration);
        val cehModelApplication = new Object2TemplatedMessageConverter<>(CehModelApplication.class, freemarkerConfiguration);
        val citation = new Object2TemplatedMessageConverter<>(Citation.class, freemarkerConfiguration);
        val code = new Object2TemplatedMessageConverter<>(CodeDocument.class, freemarkerConfiguration);
        val dataType = new Object2TemplatedMessageConverter<>(DataType.class, freemarkerConfiguration);
        val infrastructureRecord = new Object2TemplatedMessageConverter<>(InfrastructureRecord.class, freemarkerConfiguration);
        val errorResponse = new Object2TemplatedMessageConverter<>(ErrorResponse.class, freemarkerConfiguration);
        val gemini = new Object2TemplatedMessageConverter<>(GeminiDocument.class, freemarkerConfiguration);
        val adminDeleteResponse = new Object2TemplatedMessageConverter<>(AdminDeleteResponse.class, freemarkerConfiguration);
        val history = new Object2TemplatedMessageConverter<>(History.class, freemarkerConfiguration);
        val link = new Object2TemplatedMessageConverter<>(LinkDocument.class, freemarkerConfiguration);
        val maintenanceResponse = new Object2TemplatedMessageConverter<>(MaintenanceResponse.class, freemarkerConfiguration);
        val metricsReportModel = new Object2TemplatedMessageConverter<>(MetricsReportModel.class, freemarkerConfiguration);
        val monitoringActivity = new Object2TemplatedMessageConverter<>(MonitoringActivity.class, freemarkerConfiguration);
        val monitoringFacility = new Object2TemplatedMessageConverter<>(MonitoringFacility.class, freemarkerConfiguration);
        val monitoringNetwork = new Object2TemplatedMessageConverter<>(MonitoringNetwork.class, freemarkerConfiguration);
        val monitoringProgramme = new Object2TemplatedMessageConverter<>(MonitoringProgramme.class, freemarkerConfiguration);
        val nercModel = new Object2TemplatedMessageConverter<>(NercModel.class, freemarkerConfiguration);
        val nercModelUse = new Object2TemplatedMessageConverter<>(NercModelUse.class, freemarkerConfiguration);
        val permissionResource = new Object2TemplatedMessageConverter<>(PermissionResource.class, freemarkerConfiguration);
        val sampleArchive = new Object2TemplatedMessageConverter<>(SampleArchive.class, freemarkerConfiguration);
        val searchResults = new Object2TemplatedMessageConverter<>(SearchResults.class, freemarkerConfiguration);
        val serviceAgreementModel = new Object2TemplatedMessageConverter<>(ServiceAgreementModel.class, freemarkerConfiguration);
        val serviceAgreementPermissionResource = new Object2TemplatedMessageConverter<>(ServiceAgreementPermissionResource.class, freemarkerConfiguration);
        val sparqlResponse = new Object2TemplatedMessageConverter<>(SparqlResponse.class, freemarkerConfiguration);
        val stateResource = new Object2TemplatedMessageConverter<>(StateResource.class, freemarkerConfiguration);
        val ukems = new Object2TemplatedMessageConverter<>(UkemsDocument.class, freemarkerConfiguration);
        val wmsFeatureInfo = new WmsFeatureInfo2XmlMessageConverter();

        this.beforeStandardMessageConverters = Arrays.asList(
            gemini,
            wmsFeatureInfo
        );
        this.afterStandardMessageConverters = Arrays.asList(
            adminDeleteResponse,
            cehModel,
            cehModelApplication,
            citation,
            code,
            dataType,
            infrastructureRecord,
            errorResponse,
            history,
            link,
            maintenanceResponse,
            metricsReportModel,
            monitoringActivity,
            monitoringFacility,
            monitoringNetwork,
            monitoringProgramme,
            nercModel,
            nercModelUse,
            permissionResource,
            sampleArchive,
            searchResults,
            serviceAgreementModel,
            serviceAgreementPermissionResource,
            sparqlResponse,
            stateResource,
            ukems
        );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new ActiveUserHandlerMethodArgumentResolver());
    }

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.addCustomConverter(new TransparentProxyMessageConverter(proxyRequestFactory()));
        beforeStandardMessageConverters.forEach(builder::addCustomConverter);
        builder.configureMessageConvertersList(converters -> converters.addAll(afterStandardMessageConverters));
    }

    @Bean
    public DocumentWritingService documentWritingService(JsonMapper objectMapper, List<HttpMessageConverter<?>> messageConverters) {
        val allMessageConverters = Stream.concat(
            beforeStandardMessageConverters.stream(),
            afterStandardMessageConverters.stream()
        ).collect(Collectors.toList());
        allMessageConverters.addAll(messageConverters);
        allMessageConverters.add(new JacksonJsonHttpMessageConverter(objectMapper));
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
    public JsonMapper objectMapper() {
        return JsonMapper.builder()
            .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_EMPTY))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .findAndAddModules()
            .build();
    }

    @Bean
    public ClientHttpRequestFactory proxyRequestFactory() {
        log.info("Creating proxy ClientHttpRequestFactory");
        return new JdkClientHttpRequestFactory();
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
            .mediaType(GEMINI_XML_SHORT, GEMINI_XML)
            .mediaType("html", MediaType.TEXT_HTML)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType(RDF_SCHEMAORG_SHORT, RDF_SCHEMAORG_JSON)
            .mediaType(CROISSANT_SHORT, CROISSANT_JSON)
            .mediaType(ROCRATE_SHORT, ROCRATE_JSON)
            .mediaType(ROCRATE_ATTACHED_SHORT, ROCRATE_ATTACHED_JSON)
            .mediaType(RDF_TTL_SHORT, RDF_TTL)
            .mediaType(RESEARCH_INFO_SYSTEMS_SHORT, RESEARCH_INFO_SYSTEMS);
    }
}
