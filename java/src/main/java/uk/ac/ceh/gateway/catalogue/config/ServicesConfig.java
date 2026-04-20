package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.citation.CitationService;
import uk.ac.ceh.gateway.catalogue.converters.Gml2WmsFeatureInfoMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2WmsCapabilitiesMessageConverter;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.JacksonDocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.*;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.ClassMap;
import uk.ac.ceh.gateway.catalogue.indexing.PrioritisedClassMap;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModel;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModelUse;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.postprocess.ClassMapPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.GeminiDocumentPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitDocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitRepoWrapper;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.services.ResourceIdentifierLookupService;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyRetriever;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyService;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;
import uk.ac.ceh.gateway.catalogue.ukems.UkemsDocument;
import uk.ac.ceh.gateway.catalogue.wms.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService;
import uk.ac.ceh.gateway.catalogue.wms.MapServerGetFeatureInfoErrorHandler;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.DocumentTypes.*;

@Slf4j
@Configuration
public class ServicesConfig {

    @Bean
    @Qualifier("normal")
    public RestTemplate normalRestTemplate() {
        log.info("Creating Normal RestTemplate");
        return new RestTemplate();
    }

    @Bean
    @Qualifier("sparql")
    public RestTemplate sparqlRestTemplate(ObjectMapper objectMapper) {
        log.info("Creating SPARQL RestTemplate");
        val messageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        val supportedMediaTypes = Arrays.asList(
            MediaType.APPLICATION_JSON,
            new MediaType("application", "*+json")
        );
        messageConverter.setSupportedMediaTypes(supportedMediaTypes);
        val restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(List.of(
            messageConverter
        ));
        return restTemplate;
    }

    @Bean
    @Qualifier("wms")
    public RestTemplate getFeatureInfoRestTemplate() throws XPathExpressionException {
        log.info("Creating WMS RestTemplate");
        RestTemplate toReturn = new RestTemplate();
        toReturn.setMessageConverters(Collections.singletonList(
            new Gml2WmsFeatureInfoMessageConverter()
        ));
        toReturn.setErrorHandler(new MapServerGetFeatureInfoErrorHandler());
        return toReturn;
    }

    @Bean
    @SuppressWarnings("rawtypes")
    public PostProcessingService postProcessingService(
        CitationService citationService,
        DataciteService dataciteService
    ) {
        ClassMap<PostProcessingService> mappings = new PrioritisedClassMap<PostProcessingService>()
            .register(GeminiDocument.class, new GeminiDocumentPostProcessingService(citationService, dataciteService));
        return new ClassMapPostProcessingService(mappings);
    }

    @Bean
    public DocumentReadingService documentReadingService(
        ObjectMapper objectMapper
    ) {
        return new MessageConverterReadingService()
            .addMessageConverter(new MappingJackson2HttpMessageConverter(objectMapper));
    }

    @Bean
    public DocumentTypeLookupService metadataRepresentationService() {
        return new HashMapDocumentTypeLookupService()
            .register(CEH_MODEL, CehModel.class)
            .register(CEH_MODEL_APPLICATION, CehModelApplication.class)
            .register(CODE, CodeDocument.class)
            .register(DATA_TYPE, DataType.class)
            .register(INFRASTRUCTURERECORD, InfrastructureRecord.class)
            .register(GEMINI, GeminiDocument.class)
            .register(LINK, LinkDocument.class)
            .register(NERC_MODEL, NercModel.class)
            .register(NERC_MODEL_USE, NercModelUse.class)
            .register(MONITORING_ACTIVITY, MonitoringActivity.class)
            .register(MONITORING_FACILITY, MonitoringFacility.class)
            .register(MONITORING_NETWORK, MonitoringNetwork.class)
            .register(MONITORING_PROGRAMME, MonitoringProgramme.class)
            .register(SAMPLE_ARCHIVE, SampleArchive.class)
            .register(SERVICE_AGREEMENT, ServiceAgreement.class)
            .register(UKEMS_DOCUMENT, UkemsDocument.class);
    }

    @Bean
    public GetCapabilitiesObtainerService getCapabilitiesObtainerService(
        MapServerDetailsService mapServerDetailsService
    ) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Collections.singletonList(
            new Xml2WmsCapabilitiesMessageConverter()
        ));
        return new GetCapabilitiesObtainerService(restTemplate, mapServerDetailsService);
    }

    @Bean
    public DocumentRepository documentRepository(
        BundledReaderService<MetadataDocument> bundledReaderService,
        DocumentIdentifierService documentIdentifierService,
        DocumentReadingService documentReadingService,
        DocumentTypeLookupService documentTypeLookupService,
        DocumentWritingService documentWritingService,
        ResourceIdentifierLookupService resourceIdentifierLookupService,
        GitRepoWrapper gitRepoWrapper
    ) {
        return new GitDocumentRepository(
            documentTypeLookupService,
            documentReadingService,
            documentIdentifierService,
            documentWritingService,
            bundledReaderService,
            resourceIdentifierLookupService,
            gitRepoWrapper
        );
    }

    @Bean
    public VocabularyService vocabularyService(
        @Qualifier("sparql") RestTemplate restTemplate,
        @Value("${ukceh.sparql.endpoint}") String sparqlEndpoint
    ) {
        return new SparqlVocabularyService(new SparqlVocabularyRetriever(restTemplate, sparqlEndpoint).retrieve());
    }

    @Bean
    public EventBus communicationBus() {
        return new EventBus();
    }

    @Bean
    @SneakyThrows
    public DataRepository<CatalogueUser> dataRepository(
        @Value("${data.repository.location}") String dataRepositoryLocation,
        EventBus eventBus
    ) {
        return new GitDataRepository<>(
            new File(dataRepositoryLocation),
            new InMemoryUserStore<>(),
            new AnnotatedUserHelper<>(CatalogueUser.class),
            eventBus
        );
    }

    @Bean
    public DocumentInfoMapper<MetadataInfo> metadataInfoMapper(
        ObjectMapper objectMapper
    ) {
        return new JacksonDocumentInfoMapper<>(objectMapper, MetadataInfo.class);
    }

    @Bean
    public DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper(
        ObjectMapper objectMapper
    ) {
        return new JacksonDocumentInfoMapper<>(objectMapper, ServiceAgreement.class);
    }

    @PostConstruct
    public void initializeGeoSPARQL() {
        log.info("Initializing GeoSPARQL support");
        try {
//            GeoSPARQLConfig.setup(IndexConfiguration.IndexOption.MEMORY);
            GeoSPARQLConfig.setupMemoryIndex();

            log.info("GeoSPARQL initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize GeoSPARQL", e);
        }
        FunctionRegistry registry = FunctionRegistry.get();
        boolean isRegistered = registry.isRegistered("http://www.opengis.net/def/function/geosparql/distance");
        log.info("geof:distance registered: " + isRegistered);
    }

    @Bean(destroyMethod = "close")
    public org.apache.jena.query.Dataset tdbModel(
        @Value("${jena.location}") String location
    ) {
        log.info("Creating Dataset at: {}", location);
        return TDB2Factory.connectDataset(location);
    }

    @Bean
    public SolrClient solrClient(
        @Value("${solr.server.url}") String solrServerUrl
    ){
        return new HttpJdkSolrClient.Builder(solrServerUrl).useHttp1_1(true).build();
    }
}
