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
import uk.ac.ceh.gateway.catalogue.datalabs.DatalabsDocument;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.document.writing.MessageConverterWritingService;
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
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModel;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModelUse;
import uk.ac.ceh.gateway.catalogue.osdp.*;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementModel;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlResponse;
import uk.ac.ceh.gateway.catalogue.ukems.UkemsDocument;
import uk.ac.ceh.gateway.catalogue.wms.WmsFormatParameterFilter;

import java.util.List;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.*;

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
        converters.add(0, datacite());
        converters.add(0, gemini());
        converters.add(0, transparentProxy());
        converters.add(0, wmsFeatureInfo());

        // After standard Spring message converters
        converters.add(activity());
        converters.add(agent());
        converters.add(caseStudy());
        converters.add(cehModel());
        converters.add(cehModelApplication());
        converters.add(citation());
        converters.add(dataset());
        converters.add(datalabs());
        converters.add(dataType());
        converters.add(elter());
        converters.add(erammpDatacube());
        converters.add(erammpModel());
        converters.add(errorResponse());
        converters.add(facility());
        converters.add(link());
        converters.add(maintenanceResponse());
        converters.add(model());
        converters.add(modelApplication());
        converters.add(monitoringActivity());
        converters.add(monitoringFacility());
        converters.add(monitoringProgramme());
        converters.add(nercModel());
        converters.add(nercModelUse());
        converters.add(network());
        converters.add(osdpModel());
        converters.add(permissionResource());
        converters.add(programme());
        converters.add(publication());
        converters.add(sample());
        converters.add(sampleArchive());
        converters.add(searchResults());
        converters.add(serviceAgreementModel());
        converters.add(sparqlResponse());
        converters.add(stateResource());
        converters.add(ukems());
        converters.add(validationResponse());

        if (log.isDebugEnabled()) {
            log.debug("After our message converters added");
            converters.forEach(convert -> log.debug(convert.toString()));
        }
    }

    @Bean
    public Object2TemplatedMessageConverter<Agent> agent() {
        return new Object2TemplatedMessageConverter<>(Agent.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<Activity> activity() {
        return new Object2TemplatedMessageConverter<>(Activity.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<CaseStudy> caseStudy() {
        return new Object2TemplatedMessageConverter<>(CaseStudy.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<CehModel> cehModel() {
        return new Object2TemplatedMessageConverter<>(CehModel.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<CehModelApplication> cehModelApplication() {
        return new Object2TemplatedMessageConverter<>(CehModelApplication.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<Citation> citation() {
        return new Object2TemplatedMessageConverter<>(Citation.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<DataciteResponse> datacite() {
        return new Object2TemplatedMessageConverter<>(DataciteResponse.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<DatalabsDocument> datalabs() {
        return new Object2TemplatedMessageConverter<>(DatalabsDocument.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<Dataset> dataset() {
        return new Object2TemplatedMessageConverter<>(Dataset.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<DataType> dataType() {
        return new Object2TemplatedMessageConverter<>(DataType.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<ElterDocument> elter() {
        return new Object2TemplatedMessageConverter<>(ElterDocument.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<ErammpModel> erammpModel() {
        return new Object2TemplatedMessageConverter<>(ErammpModel.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<ErammpDatacube> erammpDatacube() {
        return new Object2TemplatedMessageConverter<>(ErammpDatacube.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<ErrorResponse> errorResponse() {
        return new Object2TemplatedMessageConverter<>(ErrorResponse.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<Facility> facility() {
        return new Object2TemplatedMessageConverter<>(Facility.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<GeminiDocument> gemini() {
        return new Object2TemplatedMessageConverter<>(GeminiDocument.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<LinkDocument> link() {
        return new Object2TemplatedMessageConverter<>(LinkDocument.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<MaintenanceResponse> maintenanceResponse() {
        return new Object2TemplatedMessageConverter<>(MaintenanceResponse.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<Model> model() {
        return new Object2TemplatedMessageConverter<>(Model.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<ModelApplication> modelApplication() {
        return new Object2TemplatedMessageConverter<>(ModelApplication.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<MonitoringActivity> monitoringActivity() {
        return new Object2TemplatedMessageConverter<>(MonitoringActivity.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<MonitoringFacility> monitoringFacility() {
        return new Object2TemplatedMessageConverter<>(MonitoringFacility.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<MonitoringProgramme> monitoringProgramme() {
        return new Object2TemplatedMessageConverter<>(MonitoringProgramme.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<Network> network() {
        return new Object2TemplatedMessageConverter<>(Network.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<NercModel> nercModel() {
        return new Object2TemplatedMessageConverter<>(NercModel.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<NercModelUse> nercModelUse() {
        return new Object2TemplatedMessageConverter<>(NercModelUse.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<uk.ac.ceh.gateway.catalogue.osdp.Model> osdpModel() {
        return new Object2TemplatedMessageConverter<>(uk.ac.ceh.gateway.catalogue.osdp.Model.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<PermissionResource> permissionResource() {
        return new Object2TemplatedMessageConverter<>(PermissionResource.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<Programme> programme() {
        return new Object2TemplatedMessageConverter<>(Programme.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<Publication> publication() {
        return new Object2TemplatedMessageConverter<>(Publication.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<Sample> sample() {
        return new Object2TemplatedMessageConverter<>(Sample.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<SampleArchive> sampleArchive() {
        return new Object2TemplatedMessageConverter<>(SampleArchive.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<SearchResults> searchResults() {
        return new Object2TemplatedMessageConverter<>(SearchResults.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<ServiceAgreementModel> serviceAgreementModel() {
        return new Object2TemplatedMessageConverter<>(ServiceAgreementModel.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<SparqlResponse> sparqlResponse() {
        return new Object2TemplatedMessageConverter<>(SparqlResponse.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<StateResource> stateResource() {
        return new Object2TemplatedMessageConverter<>(StateResource.class, freemarkerConfiguration);
    }

    @Bean
    public TransparentProxyMessageConverter transparentProxy() {
        return new TransparentProxyMessageConverter(httpClient());
    }

    @Bean
    public Object2TemplatedMessageConverter<UkemsDocument> ukems() {
        return new Object2TemplatedMessageConverter<>(UkemsDocument.class, freemarkerConfiguration);
    }

    @Bean
    public Object2TemplatedMessageConverter<ValidationResponse> validationResponse() {
        return new Object2TemplatedMessageConverter<>(ValidationResponse.class, freemarkerConfiguration);
    }

    @Bean
    public WmsFeatureInfo2XmlMessageConverter wmsFeatureInfo() {
        return new WmsFeatureInfo2XmlMessageConverter();
    }

    @Bean
    public DocumentWritingService documentWritingService(
        List<HttpMessageConverter<?>> messageConverters
    ) {
        log.info("Creating documentWritingService");
        if (log.isDebugEnabled()) {
            messageConverters.forEach(converter -> log.debug("Converter is: {}", converter));
        }
        return new MessageConverterWritingService(messageConverters);
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
         Document types just producing json format do not need to register
         a media type can just append ?format=json to url
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
            .mediaType(RDF_TTL_SHORT, RDF_TTL)
            .mediaType(RESEARCH_INFO_SYSTEMS_SHORT, RESEARCH_INFO_SYSTEMS);
    }
}
