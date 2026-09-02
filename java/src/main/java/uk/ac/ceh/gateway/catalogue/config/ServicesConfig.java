package uk.ac.ceh.gateway.catalogue.config;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
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
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
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
import uk.ac.ceh.gateway.catalogue.researchActivity.ResearchActivity;
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
    public RestTemplate sparqlRestTemplate(JsonMapper objectMapper) {
        log.info("Creating SPARQL RestTemplate");
        val messageConverter = new JacksonJsonHttpMessageConverter(objectMapper);
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
        JsonMapper objectMapper
    ) {
        return new MessageConverterReadingService()
            .addMessageConverter(new JacksonJsonHttpMessageConverter(objectMapper));
    }

    @Bean
    public DocumentTypeLookupService metadataRepresentationService() {
        return new HashMapDocumentTypeLookupService()
            .register(CEH_MODEL, CehModel.class)
            .register(CEH_MODEL_APPLICATION, CehModelApplication.class)
            .register(CODE, CodeDocument.class)
            .register(DATA_TYPE, DataType.class)
            .register(INFRASTRUCTURERECORD, InfrastructureRecord.class)
            .register(RESEARCHACTIVITY, ResearchActivity.class)
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
        GitRepoWrapper gitRepoWrapper,
        JsonMapper objectMapper
    ) {
        return new GitDocumentRepository(
            documentTypeLookupService,
            documentReadingService,
            documentIdentifierService,
            documentWritingService,
            bundledReaderService,
            resourceIdentifierLookupService,
            gitRepoWrapper,
            objectMapper
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
            CatalogueUser::new,
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

    // @Primary because the description cache (below) is a second Dataset bean,
    // and a dozen services inject Dataset without a qualifier. Without this they
    // all fail with "expected single matching bean but found 2".
    @Primary
    @Bean(destroyMethod = "close")
    @Profile("!test")
    public org.apache.jena.query.Dataset tdbModel(
        @Value("${jena.location}") String location
    ) {
        log.info("Creating Dataset at: {}", location);
        return TDB2Factory.connectDataset(location);
    }

    @Primary
    @Bean(destroyMethod = "close")
    @Profile("test")
    public org.apache.jena.query.Dataset tdbModelInMemory() {
        log.info("Creating in-memory Dataset for tests");
        return TDB2Factory.createDataset();
    }

    /**
     * A store of its own for the cached authority descriptions (dri-one #350
     * phase 3), kept apart from the search index: they hold third-party data on
     * a different lifecycle, and rebuilding the index must not discard a
     * fortnight of politely-fetched ORCID records.
     */
    @Bean(name = "descriptionCacheDataset", destroyMethod = "close")
    @Profile("exports")
    public org.apache.jena.query.Dataset descriptionCacheDataset(
        @Value("${jena.descriptionCache.location:}") String location
    ) {
        if (location.isBlank()) {
            log.info("Creating in-memory description cache: no jena.descriptionCache.location set");
            return TDB2Factory.createDataset();
        }
        log.info("Creating description cache at: {}", location);
        return TDB2Factory.connectDataset(location);
    }

    @Bean
    public SolrClient solrClient(
        @Value("${solr.server.url}") String solrServerUrl
    ){
        return new HttpJdkSolrClient.Builder(solrServerUrl).useHttp1_1(true).build();
    }
}
