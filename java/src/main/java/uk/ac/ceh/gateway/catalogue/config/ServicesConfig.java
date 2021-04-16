package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import freemarker.template.TemplateExceptionHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.tdb.TDBFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.converters.Gml2WmsFeatureInfoMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.UkeofXml2EFDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2GeminiDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2WmsCapabilitiesMessageConverter;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpModel;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.indexing.*;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.osdp.*;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.postprocess.BaseMonitoringTypePostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.ClassMapPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.GeminiDocumentPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitDocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitRepoWrapper;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.services.*;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyRetriever;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyService;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.GeminiExtractor;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;
import uk.ac.ceh.gateway.catalogue.util.MapServerGetFeatureInfoErrorHandler;
import uk.ac.ceh.gateway.catalogue.util.PrioritisedClassMap;
import uk.ac.ceh.gateway.catalogue.validation.MediaTypeValidator;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.XSDSchemaValidator;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static java.util.stream.Stream.of;
import static uk.ac.ceh.gateway.catalogue.config.CatalogueServiceConfig.*;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Slf4j
@Configuration
public class ServicesConfig {
    @Value("${documents.baseUri}") private String baseUri;
    @Value("${data.repository.location}") private String dataRepositoryLocation;
    @Value("${maps.location}") private File mapsLocation;
    @Value("${solr.server.documents.url}") private String solrDocumentServerUrl;
    @Value("${sparql.endpoint}") private String sparqlEndpoint;
    @Value("${sparql.graph}") private String sparqlGraph;

    @Autowired private BundledReaderService<MetadataDocument> bundledReaderService;
    @Autowired private CatalogueService catalogueService;
    @Autowired private CitationService citationService;
    @Autowired private DataciteService dataciteService;
    @Autowired private DocumentIdentifierService documentIdentifierService;
    @Autowired private DocumentListingService documentListingService;
    @Autowired private DocumentWritingService documentWritingService;
    @Autowired private freemarker.template.Configuration freemarkerConfiguration;
    @Autowired private GeminiExtractor geminiExtractor;
    @Autowired private GitRepoWrapper gitRepoWrapper;
    @Autowired private JenaLookupService jenaLookupService;
    @Autowired private MapServerDetailsService mapServerDetailsService;
    @Autowired private MetadataQualityService metadataQualityService;
    @Autowired private List<HttpMessageConverter<?>> messageConverters;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PermissionService permissionService;
    @Autowired private org.apache.jena.query.Dataset tdbModel;

    @Configuration
    static class RestTemplateConfig {
        @Autowired private CloseableHttpClient httpClient;

        @Bean
        @Qualifier("normal")
        public RestTemplate normalRestTemplate() {
            log.info("Creating Normal RestTemplate");
            val requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            return new RestTemplate(requestFactory);
        }

        @Bean
        @Qualifier("wms")
        public RestTemplate getFeatureInfoRestTemplate() throws XPathExpressionException {
            RestTemplate toReturn = new RestTemplate();
            toReturn.setMessageConverters(Collections.singletonList(
                new Gml2WmsFeatureInfoMessageConverter()
            ));
            toReturn.setErrorHandler(new MapServerGetFeatureInfoErrorHandler());
            return toReturn;
        }
    }

    @SneakyThrows
    @PostConstruct
    public void configureFreemarkerSharedVariables() {
        freemarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarkerConfiguration.setSharedVariable("catalogues", catalogueService);
        freemarkerConfiguration.setSharedVariable("codes", codeNameLookupService());
        freemarkerConfiguration.setSharedVariable("downloadOrderDetails", downloadOrderDetailsService());
        freemarkerConfiguration.setSharedVariable("geminiHelper", geminiExtractor);
        freemarkerConfiguration.setSharedVariable("jena", jenaLookupService);
        freemarkerConfiguration.setSharedVariable("mapServerDetails", mapServerDetailsService);
        freemarkerConfiguration.setSharedVariable("metadataQuality", metadataQualityService);
        freemarkerConfiguration.setSharedVariable("permission", permissionService);
    }

    @Bean
    public DownloadOrderDetailsService downloadOrderDetailsService() {
        return new DownloadOrderDetailsService(
            "https:\\/\\/data-package\\.ceh\\.ac\\.uk\\/sd\\/.*",
            Arrays.asList(
                "http(s?):\\/\\/catalogue\\.ceh\\.ac\\.uk\\/download\\?fileIdentifier=.*",
                "https:\\/\\/order-eidc\\.ceh\\.ac\\.uk\\/resources\\/.{8}\\/order\\?*.*"
            )
        );
    }

    @Bean(initMethod = "initialIndex") @Qualifier("jena-index")
    @SuppressWarnings("rawtypes")
    public JenaIndexingService documentLinkingService() {
        JenaIndexMetadataDocumentGenerator metadataDocument = new JenaIndexMetadataDocumentGenerator(documentIdentifierService);

        ClassMap<IndexGenerator<?, List<Statement>>> mappings = new PrioritisedClassMap<IndexGenerator<?, List<Statement>>>()
            .register(BaseMonitoringType.class, new JenaIndexBaseMonitoringTypeGenerator(metadataDocument))
            .register(GeminiDocument.class, new JenaIndexGeminiDocumentGenerator(metadataDocument, baseUri))
            .register(LinkDocument.class, new JenaIndexLinkDocumentGenerator(metadataDocument))
            .register(MetadataDocument.class, metadataDocument);

        return new JenaIndexingService<>(
            bundledReaderService,
            documentListingService,
            dataRepository(),
            new IndexGeneratorRegistry<>(mappings),
            documentIdentifierService,
            tdbModel
        );
    }

    @Bean(initMethod = "initialIndex") @Qualifier("mapserver-index")
    @SuppressWarnings("rawtypes")
    public MapServerIndexingService mapServerIndexingService(
        MapServerDetailsService mapServerDetailsService
    ) {
        MapServerIndexGenerator generator = new MapServerIndexGenerator(freemarkerConfiguration, mapServerDetailsService);
        return new MapServerIndexingService<>(
            bundledReaderService,
            documentListingService,
            dataRepository(),
            generator,
            mapsLocation);
    }


    @Bean
    @SuppressWarnings("rawtypes")
    public PostProcessingService postProcessingService() {
        ClassMap<PostProcessingService> mappings = new PrioritisedClassMap<PostProcessingService>()
            .register(GeminiDocument.class, new GeminiDocumentPostProcessingService(citationService, dataciteService, tdbModel, documentIdentifierService))
            .register(BaseMonitoringType.class, new BaseMonitoringTypePostProcessingService(tdbModel));
        return new ClassMapPostProcessingService(mappings);
    }

    @Bean
    public DocumentReadingService documentReadingService() {
        return new MessageConverterReadingService()
            .addMessageConverter(new Xml2GeminiDocumentMessageConverter(codeNameLookupService()))
            .addMessageConverter(new UkeofXml2EFDocumentMessageConverter())
            .addMessageConverter(new MappingJackson2HttpMessageConverter(objectMapper));
    }

    @Bean
    public DocumentTypeLookupService metadataRepresentationService() {
        return new HashMapDocumentTypeLookupService()
            .register(GEMINI_DOCUMENT, GeminiDocument.class)
            .register(EF_DOCUMENT, BaseMonitoringType.class)
            .register(IMP_DOCUMENT, ImpDocument.class)
            .register(LINK_DOCUMENT, LinkDocument.class)
            .register(CEH_MODEL, CehModel.class)
            .register(CEH_MODEL_APPLICATION, CehModelApplication.class)
            .register(OSDP_AGENT_SHORT, Agent.class)
            .register(OSDP_DATASET_SHORT, uk.ac.ceh.gateway.catalogue.osdp.Dataset.class)
            .register(OSDP_MODEL_SHORT, uk.ac.ceh.gateway.catalogue.osdp.Model.class)
            .register(OSDP_MONITORING_ACTIVITY_SHORT, MonitoringActivity.class)
            .register(OSDP_MONITORING_FACILITY_SHORT, MonitoringFacility.class)
            .register(OSDP_MONITORING_PROGRAMME_SHORT, MonitoringProgramme.class)
            .register(OSDP_PUBLICATION_SHORT, Publication.class)
            .register(OSDP_SAMPLE_SHORT, Sample.class)
            .register(ERAMMP_MODEL_SHORT, ErammpModel.class)
            .register(ERAMMP_DATACUBE_SHORT, ErammpDatacube.class)
            .register(SAMPLE_ARCHIVE_SHORT, SampleArchive.class)
            .register(DATA_TYPE_SHORT, DataType.class);
    }

    @Bean
    public GetCapabilitiesObtainerService getCapabilitiesObtainerService() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Collections.singletonList(
            new Xml2WmsCapabilitiesMessageConverter()
        ));
        return new GetCapabilitiesObtainerService(restTemplate, mapServerDetailsService);
    }

    @Bean
    public DocumentRepository documentRepository() {
        return new GitDocumentRepository(
            metadataRepresentationService(),
            documentReadingService(),
            documentIdentifierService,
            documentWritingService(),
            bundledReaderService,
            gitRepoWrapper
        );
    }

    @Bean
    public VocabularyService vocabularyService() {
        val messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);
        messageConverter.setSupportedMediaTypes(
            Arrays.asList(
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json"),
                new MediaType("application", "*+json-simple")
            )
        );
        val restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Collections.singletonList(messageConverter));
        return new SparqlVocabularyService(new SparqlVocabularyRetriever(restTemplate, sparqlEndpoint, sparqlGraph).retrieve());
    }

    @Bean
    @SuppressWarnings("UnstableApiUsage") // Because EventBus is still @Beta!
    public EventBus communicationBus() {
        return new EventBus();
    }

    @Bean
    @SuppressWarnings("UnstableApiUsage")
    @SneakyThrows
    public DataRepository<CatalogueUser> dataRepository() {
        return new GitDataRepository<>(
            new File(dataRepositoryLocation),
            new InMemoryUserStore<>(),
            phantomUserBuilderFactory(),
            communicationBus()
        );
    }

    @Bean
    public AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory() {
        return new AnnotatedUserHelper<>(CatalogueUser.class);
    }

    @Bean
    @SneakyThrows
    public CodeLookupService codeNameLookupService() {
        Properties properties = PropertiesLoaderUtils.loadAllProperties("codelist.properties");
        return new CodeLookupService(properties);
    }

    @Bean
    public DocumentInfoMapper<MetadataInfo> documentInfoMapper() {
        return new JacksonDocumentInfoMapper<>(objectMapper, MetadataInfo.class);
    }

    @Bean(destroyMethod = "close")
    public org.apache.jena.query.Dataset tdbModel(
        @Value("${jena.location}") String location
    ) {
        log.info("Creating Dataset at: {}", location);
        return TDBFactory.createDataset(location);
    }

    @Bean
    public SolrClient solrClient(){
        return new HttpSolrClient.Builder(solrDocumentServerUrl).build();
    }

    @Bean
    DocumentWritingService documentWritingService() {
        return new MessageConverterWritingService(messageConverters);
    }

    @Bean @Qualifier("datacite-index")
    public DocumentIndexingService dataciteIndexingService() {
        return new AsyncDocumentIndexingService(
            new DataciteIndexingService(bundledReaderService, dataciteService)
        );
    }

    @Bean(initMethod = "initialIndex") @Qualifier("solr-index")
    public SolrIndexingService<MetadataDocument> documentIndexingService(
        JenaLookupService jenaLookupService
    ) {
        val metadataDocumentGenerator = new SolrIndexMetadataDocumentGenerator(
            codeNameLookupService(),
            documentIdentifierService,
            vocabularyService()
        );
        val linkDocumentGenerator = new SolrIndexLinkDocumentGenerator();
        linkDocumentGenerator.setRepository(documentRepository());

        val mappings = new PrioritisedClassMap<IndexGenerator<?, SolrIndex>>()
            .register(GeminiDocument.class, new SolrIndexGeminiDocumentGenerator(new ExtractTopicFromDocument(), metadataDocumentGenerator, codeNameLookupService()))
            .register(LinkDocument.class, linkDocumentGenerator)
            .register(MetadataDocument.class, metadataDocumentGenerator);

        IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry = new IndexGeneratorRegistry<>(mappings);
        linkDocumentGenerator.setIndexGeneratorRegistry(indexGeneratorRegistry);
        log.info("Set repository & registry on {}", linkDocumentGenerator);

        return new SolrIndexingService<>(
            bundledReaderService,
            documentListingService,
            dataRepository(),
            indexGeneratorRegistry,
            solrClient(),
            jenaLookupService,
            documentIdentifierService
        );
    }

    @Bean
    @Qualifier("validation-index")
    public DocumentIndexingService asyncValidationIndexingService(
        @Value("${schemas.location}") String schemas
    ) {
        return new AsyncDocumentIndexingService(validationIndexingService(schemas));
    }


    @Bean
    @SneakyThrows
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ValidationIndexingService validationIndexingService(
        @Value("${schemas.location}") String schemas
    ) {
        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        val sources = of("gemini/srv/srv.xsd", "gemini/gmx/gmx.xsd")
            .map( (s) -> new StreamSource(new File(schemas, s)))
            .toArray(Source[]::new);

        val geminiSchema = schemaFactory.newSchema(sources);

        val htmlValidator = new MediaTypeValidator("HTML Generation", MediaType.TEXT_HTML, documentWritingService);

        ClassMap<IndexGenerator<?, ValidationReport>> mappings = new PrioritisedClassMap<IndexGenerator<?, ValidationReport>>()
            .register(GeminiDocument.class, new ValidationIndexGenerator(Arrays.asList(
                new XSDSchemaValidator("Gemini", MediaType.parseMediaType(GEMINI_XML_VALUE), documentWritingService, geminiSchema),
                htmlValidator
            )))
            .register(MetadataDocument.class, new ValidationIndexGenerator(Collections.singletonList(htmlValidator)));

        return new ValidationIndexingService<MetadataDocument>(
            bundledReaderService,
            documentListingService,
            dataRepository(),
            postProcessingService(),
            documentIdentifierService,
            new IndexGeneratorRegistry<>(mappings)
        );
    }
}
