package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.TemplateExceptionHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.tdb.TDBFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUserHandlerMethodArgumentResolver;
import uk.ac.ceh.gateway.catalogue.converters.*;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.ef.*;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpModel;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.CaseStudy;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.imp.ModelApplication;
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
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitDocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitRepoWrapper;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.services.*;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyRetriever;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyService;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.GeminiExtractor;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadDocument;
import uk.ac.ceh.gateway.catalogue.util.*;
import uk.ac.ceh.gateway.catalogue.validation.MediaTypeValidator;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.XSDSchemaValidator;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Stream.of;
import static uk.ac.ceh.gateway.catalogue.config.CatalogueServiceConfig.*;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    public static final String BIBTEX_SHORT                 = "bib";
    public static final String BIBTEX_VALUE                 = "application/x-bibtex";
    public static final String GEMINI_XML_SHORT             = "gemini";
    public static final String GEMINI_XML_VALUE             = "application/x-gemini+xml";
    public static final String RDF_TTL_SHORT                = "ttl";
    public static final String RDF_TTL_VALUE                = "text/turtle";
    public static final String RDF_SCHEMAORG_SHORT          = "schema.org";
    public static final String RDF_SCHEMAORG_VALUE          = "application/vnd.schemaorg.ld+json";
    public static final String RESEARCH_INFO_SYSTEMS_SHORT  = "ris";
    public static final String RESEARCH_INFO_SYSTEMS_VALUE  = "application/x-research-info-systems";
    public static final String DATACITE_XML_VALUE           = "application/x-datacite+xml";
    public static final String GEMINI_SHORT                 = "gemini";
    public static final String GEMINI_JSON_VALUE            = "application/gemini+json";
    public static final String MODEL_SHORT                  = "model";
    public static final String MODEL_JSON_VALUE             = "application/model+json";
    public static final String CEH_MODEL_SHORT              = "ceh-model";
    public static final String CEH_MODEL_JSON_VALUE         = "application/vnd.ceh.model+json";
    public static final String CEH_MODEL_APPLICATION_SHORT  = "ceh-model-application";
    public static final String CEH_MODEL_APPLICATION_JSON_VALUE = "application/vnd.ceh.model.application+json";
    public static final String LINKED_SHORT                 = "link";
    public static final String LINKED_JSON_VALUE            = "application/link+json";
    public static final String UKEOF_XML_SHORT              = "ukeof";
    public static final String UKEOF_XML_VALUE              = "application/ukeof+xml";
    public static final String EF_INSPIRE_XML_SHORT         = "efinspire";
    public static final String EF_INSPIRE_XML_VALUE         = "application/vnd.ukeof.inspire+xml";
    public static final String MAPSERVER_GML_VALUE          = "application/vnd.ogc.gml";
    public static final String OSDP_AGENT_JSON_VALUE        = "application/vnd.osdp.agent+json";
    public static final String OSDP_AGENT_SHORT             = "osdp-agent";
    public static final String OSDP_DATASET_JSON_VALUE      = "application/vnd.osdp.dataset+json";
    public static final String OSDP_DATASET_SHORT           = "osdp-dataset";
    public static final String OSDP_MODEL_JSON_VALUE        = "application/vnd.osdp.model+json";
    public static final String OSDP_MODEL_SHORT             = "osdp-model";
    public static final String OSDP_MONITORING_ACTIVITY_JSON_VALUE  = "application/vnd.osdp.monitoring-activity+json";
    public static final String OSDP_MONITORING_ACTIVITY_SHORT       = "osdp-monitoring-activity";
    public static final String OSDP_MONITORING_FACILITY_JSON_VALUE  = "application/vnd.osdp.monitoring-facility+json";
    public static final String OSDP_MONITORING_FACILITY_SHORT       = "osdp-monitoring-facility";
    public static final String OSDP_MONITORING_PROGRAMME_JSON_VALUE = "application/vnd.osdp.monitoring-programme+json";
    public static final String OSDP_MONITORING_PROGRAMME_SHORT      = "osdp-monitoring-programme";
    public static final String OSDP_PUBLICATION_JSON_VALUE  = "application/vnd.osdp.publication+json";
    public static final String OSDP_PUBLICATION_SHORT       = "osdp-publication";
    public static final String OSDP_SAMPLE_JSON_VALUE       = "application/vnd.osdp.sample+json";
    public static final String OSDP_SAMPLE_SHORT            = "osdp-sample";
    public static final String ERAMMP_MODEL_SHORT           = "erammp-model";
    public static final String ERAMMP_MODEL_JSON_VALUE      = "application/vnd.erammp-model+json";
    public static final String ERAMMP_DATACUBE_SHORT        = "erammp-datacube";
    public static final String ERAMMP_DATACUBE_JSON_VALUE   = "application/vnd.erammp-datacube+json";
    public static final String SAMPLE_ARCHIVE_SHORT         = "sample-archive";
    public static final String SAMPLE_ARCHIVE_JSON_VALUE    = "application/vnd.sample-archive+json";
    public static final String UPLOAD_DOCUMENT_JSON_VALUE   = "application/vnd.upload-document+json";
    public static final String UPLOAD_DOCUMENT_SHORT        = "Upload";
    public static final String DATA_TYPE_JSON_VALUE         = "application/vnd.data-type+json";
    public static final String DATA_TYPE_SHORT              = "data-type";

    @Value("${documents.baseUri}") private String baseUri;
    @Value("${data.repository.location}") private String dataRepositoryLocation;
    @Value("${maps.location}") private File mapsLocation;
    @Value("${jena.location}") private String location;
    @Value("${schemas.location}") private String schemas;
    @Value("${solr.server.documents.url}") String solrDocumentServerUrl;
    @Value("${sparql.endpoint}") private String sparqlEndpoint;
    @Value("${sparql.graph}") private String sparqlGraph;
    @Value("${template.location}") private File templates;

    @Autowired private CatalogueService catalogueService;
    @Autowired private CitationService citationService;
    @Autowired private DataciteService dataciteService;
    @Autowired private DocumentReader<MetadataDocument> documentReader;
    @Autowired private DocumentWritingService documentWritingService;
    @Autowired private PermissionService permissionService;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addAll(messageConverters());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new ActiveUserHandlerMethodArgumentResolver());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/static/**")
                .addResourceLocations("/static/")
                .setCacheControl(CacheControl.maxAge(2, TimeUnit.DAYS));
    }

    @Bean
    public DocumentRepository documentRepository() {
        return new GitDocumentRepository(
                metadataRepresentationService(),
                documentReadingService(),
                documentIdentifierService(),
                documentWritingService(),
                bundledReaderService(),
                gitRepoWrapper());
    }

    @Bean public GitRepoWrapper gitRepoWrapper() {
        return new GitRepoWrapper(dataRepository(), documentInfoMapper());
    }

    @Bean
    public MetadataQualityService metadataQualityService() {
        return new MetadataQualityService(documentReader, objectMapper());
    }

    @Bean
    public MapServerDetailsService mapServerDetailsService() {
        return new MapServerDetailsService(baseUri);
    }

    @Bean
    public SolrClient solrClient(){
        return new HttpSolrClient.Builder(solrDocumentServerUrl).build();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
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
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .registerModule(new GuavaModule())
                .registerModule(new JaxbAnnotationModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public DocumentInfoMapper<MetadataInfo> documentInfoMapper() {
        return new JacksonDocumentInfoMapper<>(objectMapper(), MetadataInfo.class);
    }

    @Bean(destroyMethod="close")
    public org.apache.jena.query.Dataset tdbModel() {
        log.info("Creating Dataset at: {}", location);
        return TDBFactory.createDataset(location);
    }

    @Bean
    public JenaLookupService jenaLookupService() {
        return new JenaLookupService(tdbModel());
    }

    @Bean GeminiExtractor geminiExtractor() {
        return new GeminiExtractor();
    }

    @Bean
    public freemarker.template.Configuration freemarkerConfiguration() {
        return new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_22);
    }

    @SneakyThrows
    @PostConstruct
    public void configureFreemarker() {
        Map<String, Object> shared = new HashMap<>();
        shared.put("catalogues", catalogueService);
        shared.put("codes", codeNameLookupService());
        shared.put("downloadOrderDetails", downloadOrderDetailsService());
        shared.put("geminiHelper", geminiExtractor());
        shared.put("jena", jenaLookupService());
        shared.put("mapServerDetails", mapServerDetailsService());
        shared.put("metadataQuality", metadataQualityService());
        shared.put("permission", permissionService);
        log.info("Freemarker shared variables:");
        shared.forEach((key, value) -> log.info("{}: {}", key, value));

        val config = freemarkerConfiguration();
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setSharedVariables(shared);
        config.setDefaultEncoding("UTF-8");
        config.setTemplateLoader(new FileTemplateLoader(templates));
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

    @Bean("converters")
    public List<HttpMessageConverter<?>> messageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper());

        // EF Message Converters
        converters.add(new Object2TemplatedMessageConverter<>(Activity.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Facility.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Network.class,  freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Programme.class, freemarkerConfiguration()));
        converters.add(new UkeofXml2EFDocumentMessageConverter());

        // IMP Message Converters
        converters.add(new Object2TemplatedMessageConverter<>(uk.ac.ceh.gateway.catalogue.imp.Model.class, freemarkerConfiguration()));
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
    DocumentWritingService documentWritingService() {
        return new MessageConverterWritingService(messageConverters());
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

    @Bean
    public DocumentReadingService documentReadingService() {
        return new MessageConverterReadingService()
                .addMessageConverter(new Xml2GeminiDocumentMessageConverter(codeNameLookupService()))
                .addMessageConverter(new UkeofXml2EFDocumentMessageConverter())
                .addMessageConverter(new MappingJackson2HttpMessageConverter(objectMapper()));
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
        return new GetCapabilitiesObtainerService(restTemplate, mapServerDetailsService());
    }

    @Bean
    public MetadataListingService getWafListingService() {
        return new MetadataListingService(dataRepository(), documentListingService(), bundledReaderService());
    }

    @Bean
    public TMSToWMSGetMapService tmsToWmsGetMapService() {
        return new TMSToWMSGetMapService();
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

    @Bean
    @SuppressWarnings("unchecked")
    public MetadataInfoBundledReaderService bundledReaderService() {
        return new MetadataInfoBundledReaderService(
                dataRepository(),
                documentReadingService(),
                documentInfoMapper(),
                metadataRepresentationService(),
                postProcessingService(),
                documentIdentifierService()
        );
    }

    @Bean
    public ExtensionDocumentListingService documentListingService() {
        return new ExtensionDocumentListingService();
    }

    @Bean
    public DocumentIdentifierService documentIdentifierService() {
        return new DocumentIdentifierService(baseUri, '-');
    }

    @Bean
    @SuppressWarnings("rawtypes")
    public PostProcessingService postProcessingService() {
        ClassMap<PostProcessingService> mappings = new PrioritisedClassMap<PostProcessingService>()
                .register(GeminiDocument.class, new GeminiDocumentPostProcessingService(citationService, dataciteService, tdbModel(), documentIdentifierService()))
                .register(BaseMonitoringType.class, new BaseMonitoringTypePostProcessingService(tdbModel()));
        return new ClassMapPostProcessingService(mappings);
    }

    @Bean
    public VocabularyService vocabularyService() {
        val messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper());
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

    @Bean(initMethod = "initialIndex") @Qualifier("solr-index")
    public SolrIndexingService<MetadataDocument> documentIndexingService() {
        val metadataDocumentGenerator = new SolrIndexMetadataDocumentGenerator(
                codeNameLookupService(),
                documentIdentifierService(),
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
                bundledReaderService(),
                documentListingService(),
                dataRepository(),
                indexGeneratorRegistry,
                solrClient(),
                jenaLookupService(),
                documentIdentifierService()
        );
    }

    @Bean(initMethod = "initialIndex") @Qualifier("jena-index")
    @SuppressWarnings("rawtypes")
    public JenaIndexingService documentLinkingService() {
        JenaIndexMetadataDocumentGenerator metadataDocument = new JenaIndexMetadataDocumentGenerator(documentIdentifierService());

        ClassMap<IndexGenerator<?, List<Statement>>> mappings = new PrioritisedClassMap<IndexGenerator<?, List<Statement>>>()
                .register(BaseMonitoringType.class, new JenaIndexBaseMonitoringTypeGenerator(metadataDocument))
                .register(GeminiDocument.class, new JenaIndexGeminiDocumentGenerator(metadataDocument, baseUri))
                .register(LinkDocument.class, new JenaIndexLinkDocumentGenerator(metadataDocument))
                .register(MetadataDocument.class, metadataDocument);

        return new JenaIndexingService<>(
                bundledReaderService(),
                documentListingService(),
                dataRepository(),
                new IndexGeneratorRegistry<>(mappings),
                documentIdentifierService(),
                tdbModel()
        );
    }

    @Bean @Qualifier("datacite-index")
    public DocumentIndexingService dataciteIndexingService() {
        return new AsyncDocumentIndexingService(
                new DataciteIndexingService(bundledReaderService(), dataciteService)
        );
    }

    @Bean @Qualifier("validation-index")
    public DocumentIndexingService asyncValidationIndexingService() {
        return new AsyncDocumentIndexingService(validationIndexingService());
    }

    @Bean(initMethod = "initialIndex") @Qualifier("mapserver-index")
    @SuppressWarnings("rawtypes")
    public MapServerIndexingService mapServerIndexingService() {
        MapServerIndexGenerator generator = new MapServerIndexGenerator(freemarkerConfiguration(), mapServerDetailsService());
        return new MapServerIndexingService<>(
                bundledReaderService(),
                documentListingService(),
                dataRepository(),
                generator,
                mapsLocation);
    }

    @Bean
    @SneakyThrows
    public Schema geminiSchema() {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source[] sources = of("gemini/srv/srv.xsd", "gemini/gmx/gmx.xsd")
                .map( (s) -> new StreamSource(new File(schemas, s)))
                .toArray(Source[]::new);

        return schemaFactory.newSchema(sources);
    }

    @Bean
    @SneakyThrows
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ValidationIndexingService validationIndexingService() {
        MediaTypeValidator html = new MediaTypeValidator("HTML Generation", MediaType.TEXT_HTML, documentWritingService);

        ClassMap<IndexGenerator<?, ValidationReport>> mappings = new PrioritisedClassMap<IndexGenerator<?, ValidationReport>>()
                .register(GeminiDocument.class, new ValidationIndexGenerator(Arrays.asList(
                        new XSDSchemaValidator("Gemini", MediaType.parseMediaType(GEMINI_XML_VALUE), documentWritingService, geminiSchema()),
                        html
                )))
                .register(MetadataDocument.class, new ValidationIndexGenerator(Collections.singletonList(html)));

        return new ValidationIndexingService<>(
                bundledReaderService(),
                documentListingService(),
                dataRepository(),
                postProcessingService(),
                documentIdentifierService(),
                new IndexGeneratorRegistry<>(mappings)
        );
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

    @Bean
    @Qualifier("normal")
    public RestTemplate normalRestTemplate() {
        val requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient());
        return new RestTemplate(requestFactory);
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .ignoreAcceptHeader(true) // Define accept header handling manually
            .defaultContentTypeStrategy(new ContentNegotiationManager(
                    new ForgivingParameterContentNegotiationStrategy(ImmutableMap.<String, MediaType>builder()
                        .put("html", MediaType.TEXT_HTML)
                        .put("json", MediaType.APPLICATION_JSON)
                        .put(GEMINI_XML_SHORT, MediaType.parseMediaType(GEMINI_XML_VALUE))
                        .put(UKEOF_XML_SHORT, MediaType.parseMediaType(UKEOF_XML_VALUE))
                        .put(EF_INSPIRE_XML_SHORT, MediaType.parseMediaType(EF_INSPIRE_XML_VALUE))
                        .put(RDF_TTL_SHORT, MediaType.parseMediaType(RDF_TTL_VALUE))
                        .put(RDF_SCHEMAORG_SHORT, MediaType.parseMediaType(RDF_SCHEMAORG_VALUE))
                        .put(BIBTEX_SHORT, MediaType.parseMediaType(BIBTEX_VALUE))
                        .put(RESEARCH_INFO_SYSTEMS_SHORT, MediaType.parseMediaType(RESEARCH_INFO_SYSTEMS_VALUE))
                        .put(CEH_MODEL_SHORT, MediaType.parseMediaType(CEH_MODEL_JSON_VALUE))
                        .put(CEH_MODEL_APPLICATION_SHORT, MediaType.parseMediaType(CEH_MODEL_APPLICATION_JSON_VALUE))
                        .put(OSDP_AGENT_SHORT, MediaType.parseMediaType(OSDP_AGENT_JSON_VALUE))
                        .put(OSDP_DATASET_SHORT, MediaType.parseMediaType(OSDP_DATASET_JSON_VALUE))
                        .put(OSDP_MODEL_SHORT, MediaType.parseMediaType(OSDP_MODEL_JSON_VALUE))
                        .put(OSDP_MONITORING_ACTIVITY_SHORT, MediaType.parseMediaType(OSDP_MONITORING_ACTIVITY_JSON_VALUE))
                        .put(OSDP_MONITORING_FACILITY_SHORT, MediaType.parseMediaType(OSDP_MONITORING_FACILITY_JSON_VALUE))
                        .put(OSDP_MONITORING_PROGRAMME_SHORT, MediaType.parseMediaType(OSDP_MONITORING_PROGRAMME_JSON_VALUE))
                        .put(OSDP_PUBLICATION_SHORT, MediaType.parseMediaType(OSDP_PUBLICATION_JSON_VALUE))
                        .put(OSDP_SAMPLE_SHORT, MediaType.parseMediaType(OSDP_SAMPLE_JSON_VALUE))
                        .put(ERAMMP_MODEL_SHORT, MediaType.parseMediaType(ERAMMP_MODEL_JSON_VALUE))
                        .put(ERAMMP_DATACUBE_SHORT, MediaType.parseMediaType(ERAMMP_DATACUBE_JSON_VALUE))
                        .put(SAMPLE_ARCHIVE_SHORT, MediaType.parseMediaType(SAMPLE_ARCHIVE_JSON_VALUE))
                        .put(UPLOAD_DOCUMENT_SHORT, MediaType.parseMediaType(UPLOAD_DOCUMENT_JSON_VALUE))
                        .put(DATA_TYPE_SHORT, MediaType.parseMediaType(DATA_TYPE_JSON_VALUE))
                        .build()
                    ),
                    new WmsFormatContentNegotiationStrategy("INFO_FORMAT"), // GetFeatureInfo
                    new HeaderContentNegotiationStrategy(),
                    new FixedContentNegotiationStrategy(MediaType.TEXT_HTML)
            ));
    }
}
