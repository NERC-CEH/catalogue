package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Statement;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.converters.*;
import uk.ac.ceh.gateway.catalogue.ef.*;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpModel;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.CaseStudy;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.imp.ModelApplication;
import uk.ac.ceh.gateway.catalogue.indexing.*;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.osdp.*;
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
import uk.ac.ceh.gateway.catalogue.search.FacetFactory;
import uk.ac.ceh.gateway.catalogue.search.HardcodedFacetFactory;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.services.*;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyRetriever;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyService;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.GeminiExtractor;
import uk.ac.ceh.gateway.catalogue.upload.UploadDocument;
import uk.ac.ceh.gateway.catalogue.upload.UploadDocumentService;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;
import uk.ac.ceh.gateway.catalogue.util.PrioritisedClassMap;
import uk.ac.ceh.gateway.catalogue.validation.MediaTypeValidator;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.XSDSchemaValidator;

import javax.xml.validation.Schema;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueServiceConfig.*;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;
@SuppressWarnings("Unused")
@Configuration
public class ServiceConfig {
    @Value("${documents.baseUri}") private String baseUri;
    @Value("${template.location}") private File templates;
    @Value("${maps.location}") private File mapsLocation;
    @Value("${doi.prefix}") private String doiPrefix;
    @Value("${doi.username}") private String doiUsername;
    @Value("${doi.password}") private String doiPassword;
    @Value("${jira.username}") private String jiraUsername;
    @Value("${jira.password}") private String jiraPassword;
    @Value("${jira.address}") private String jiraAddress;
    @Value("${sparql.endpoint}") private String sparqlEndpoint;
    @Value("${sparql.graph}") private String sparqlGraph;

    @Value("${hubbub.url}") private String hubbubUrl;
    @Value("${hubbub.username}") private String hubbubUsername;
    @Value("${hubbub.password}") private String hubbubPassword;
    
    @Autowired private ObjectMapper jacksonMapper;
    @Autowired private DataRepository<CatalogueUser> dataRepository;
    @Autowired private Dataset jenaTdb;
    @Autowired private SolrClient solrClient;
    @Autowired private CodeLookupService codeLookupService;
    @Autowired private GroupStore<CatalogueUser> groupStore;
    @Autowired private CatalogueService catalogueService;
    @Autowired @Qualifier("gemini") private Schema geminiSchema;
    @Autowired private MetadataQualityService metadataQualityService;
    @Autowired private GeminiExtractor geminiExtractor;

    @Bean
    public SolrScheduledReindexService solrScheduledReindexService() {
        return new SolrScheduledReindexService(documentIndexingService());
    }

    @Bean
    FacetFactory facetFactory() {
        return new HardcodedFacetFactory();
    }
    
    @Bean
    public PermissionService permission() {
        return new PermissionService(dataRepository, documentInfoMapper(), groupStore);
    }

    @Bean
    public DepositRequestService depositRequestService() {
        return new DepositRequestService(documentRepository(), solrClient);
    }

    @Bean
    public UploadDocumentService uploadDocumentService(HubbubService hubbubService) {
        Map<String, File> folders = Maps.newHashMap();
        folders.put("documents", new File("/var/ceh-catalogue/dropbox"));
        return new UploadDocumentService(hubbubService, folders,  Executors.newCachedThreadPool());
    }
    
    @Bean
    public JiraService jiraService() {
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(jiraUsername, jiraPassword));
        WebResource jira = client.resource(jiraAddress);
        return new JiraService(jira);
    }

    @Bean
    public HubbubService hubbubService() {
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(hubbubUsername, hubbubPassword));
        WebResource hubbub = client.resource(hubbubUrl);
        return new HubbubService(hubbub);
    }

    @Bean
    public CitationService citationService() {
        return new CitationService(doiPrefix);
    }
    
    @Bean
    public MapServerDetailsService mapServerDetailsService() {
        return new MapServerDetailsService(baseUri);
    }
    
    @Bean
    @SneakyThrows
    public DataciteService dataciteService() {
        Template dataciteTemplate = freemarkerConfiguration().getTemplate("/datacite/datacite.xml.tpl");
        return new DataciteService(
                doiPrefix, 
                "NERC Environmental Information Data Centre", 
                doiUsername, 
                doiPassword,
                documentIdentifierService(),
                dataciteTemplate, 
                new RestTemplate()
        );
    }
    
    @Bean
    public MessageConvertersHolder messageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(jacksonMapper);

        // EF Message Converters  
        converters.add(new Object2TemplatedMessageConverter<>(Activity.class,  freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Facility.class,  freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Network.class,   freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Programme.class, freemarkerConfiguration()));
        converters.add(new UkeofXml2EFDocumentMessageConverter());
        
        // IMP Message Converters
        converters.add(new Object2TemplatedMessageConverter<>(Model.class,            freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(ModelApplication.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(CaseStudy.class,        freemarkerConfiguration()));
        
        // CEH model catalogue
        converters.add(new Object2TemplatedMessageConverter<>(CehModel.class,             freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(CehModelApplication.class,  freemarkerConfiguration()));

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
        converters.add(new Object2TemplatedMessageConverter<>(GeminiDocument.class,       freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(LinkDocument.class,         freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(SearchResults.class,        freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(Citation.class,             freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(StateResource.class,        freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(PermissionResource.class,   freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(CatalogueResource.class,    freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(MaintenanceResponse.class,  freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(SparqlResponse.class,       freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(ValidationResponse.class,   freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter<>(ErrorResponse.class,        freemarkerConfiguration()));
        converters.add(new TransparentProxyMessageConverter(httpClient()));
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        converters.add(new WmsFeatureInfo2XmlMessageConverter());
        converters.add(mappingJackson2HttpMessageConverter);

        converters.add(new Object2TemplatedMessageConverter<>(UploadDocument.class, freemarkerConfiguration()));

        converters.add(new Object2TemplatedMessageConverter<>(DataType.class, freemarkerConfiguration()));

        return new MessageConvertersHolder(converters);
    }
    
    @Data
    public static class MessageConvertersHolder {
        private final List<HttpMessageConverter<?>> converters;
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
    @SneakyThrows
    public freemarker.template.Configuration freemarkerConfiguration() {
        Map<String, Object> shared = new HashMap<>();
        shared.put("jena", jenaLookupService());
        shared.put("codes", codeLookupService);
        shared.put("downloadOrderDetails", downloadOrderDetailsService());
        shared.put("permission", permission());
        shared.put("jira", jiraService());
        shared.put("mapServerDetails", mapServerDetailsService());
        shared.put("geminiHelper", geminiExtractor);
        shared.put("catalogues", catalogueService);
        shared.put("metadataQuality", metadataQualityService);
        
        freemarker.template.Configuration config = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_22);
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setSharedVaribles(shared);
        config.setDefaultEncoding("UTF-8");
        config.setTemplateLoader(new FileTemplateLoader(templates));
        return config;
    }
    
    @Bean
    public DataRepositoryOptimizingService dataRepositoryOptimizingService() {
        return new DataRepositoryOptimizingService(dataRepository);
    }
    
    @Bean
    GitRepoWrapper gitRepoWrapper() {
        return new GitRepoWrapper(dataRepository, documentInfoMapper());
    }
    
    @Bean
    DocumentRepository documentRepository() {
        return new GitDocumentRepository(
            metadataRepresentationService(),
            documentReadingService(),
            documentIdentifierService(),
            documentWritingService(),
            bundledReaderService(),
            gitRepoWrapper()
        );
    }
    
    @Bean
    public DocumentReadingService documentReadingService() {
        return new MessageConverterReadingService()
                .addMessageConverter(new Xml2GeminiDocumentMessageConverter(codeLookupService))
                .addMessageConverter(new UkeofXml2EFDocumentMessageConverter())
                .addMessageConverter(new MappingJackson2HttpMessageConverter(jacksonMapper));
    }
    
    @Bean
    public DocumentWritingService documentWritingService() {
        return new MessageConverterWritingService(messageConverters().getConverters());
    }
    
    @Bean
    public DocumentTypeLookupService metadataRepresentationService() {
        return new HashMapDocumentTypeLookupService()
                .register(DEPOSIT_REQUEST_DOCUMENT, DepositRequestDocument.class)
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
        restTemplate.setMessageConverters(Arrays.asList(
                new Xml2WmsCapabilitiesMessageConverter()
        ));
        return new GetCapabilitiesObtainerService(restTemplate, mapServerDetailsService());
    }
    
    @Bean
    public MetadataListingService getWafListingService() {
        return new MetadataListingService(dataRepository, documentListingService(), bundledReaderService());
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
    public DocumentInfoMapper<MetadataInfo> documentInfoMapper() {
        return new JacksonDocumentInfoMapper<>(jacksonMapper, MetadataInfo.class);
    }
    
    @Bean
    public MetadataInfoBundledReaderService bundledReaderService() {
        return new MetadataInfoBundledReaderService(
            dataRepository,
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
    public JenaLookupService jenaLookupService() {
        return new JenaLookupService(jenaTdb);
    }
    
    @Bean
    public PostProcessingService postProcessingService() {
        ClassMap<PostProcessingService> mappings = new PrioritisedClassMap<PostProcessingService>()
                .register(GeminiDocument.class, new GeminiDocumentPostProcessingService(citationService(), dataciteService(), jenaTdb, documentIdentifierService()))
                .register(BaseMonitoringType.class, new BaseMonitoringTypePostProcessingService(jenaTdb));
        return new ClassMapPostProcessingService(mappings);
    }

    @Bean
    public VocabularyService vocabularyService() {
        val messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(jacksonMapper);
        messageConverter.setSupportedMediaTypes(
            Arrays.asList(
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json"),
                new MediaType("application", "*+json-simple")
            )
        );
        val restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Arrays.asList(messageConverter));
        return new SparqlVocabularyService(new SparqlVocabularyRetriever(restTemplate, sparqlEndpoint, sparqlGraph).retrieve());
    }
    
    @Bean(initMethod = "initialIndex") @Qualifier("solr-index")
    public SolrIndexingService<MetadataDocument> documentIndexingService() {
        SolrIndexMetadataDocumentGenerator metadataDocumentGenerator = new SolrIndexMetadataDocumentGenerator(
            codeLookupService,
            documentIdentifierService(),
            vocabularyService()
        );
        SolrIndexLinkDocumentGenerator solrIndexLinkDocumentGenerator = new SolrIndexLinkDocumentGenerator();
        solrIndexLinkDocumentGenerator.setRepository(documentRepository());
        
        ClassMap<IndexGenerator<?, SolrIndex>> mappings = new PrioritisedClassMap<IndexGenerator<?, SolrIndex>>()
            .register(GeminiDocument.class, new SolrIndexGeminiDocumentGenerator(new ExtractTopicFromDocument(), metadataDocumentGenerator, codeLookupService))
            .register(LinkDocument.class, solrIndexLinkDocumentGenerator)
            .register(MetadataDocument.class, metadataDocumentGenerator);
        
        IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry = new IndexGeneratorRegistry<>(mappings);
        solrIndexLinkDocumentGenerator.setIndexGeneratorRegistry(indexGeneratorRegistry);

        return new SolrIndexingService<>(
            bundledReaderService(),
            documentListingService(),
            dataRepository,
            indexGeneratorRegistry,
            solrClient,
            jenaLookupService(),
            documentIdentifierService()
        );
    }
    
    @Bean(initMethod = "initialIndex") @Qualifier("jena-index")
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
                dataRepository,
                new IndexGeneratorRegistry<>(mappings),
                documentIdentifierService(),
                jenaTdb
        );
    }
    
    @Bean @Qualifier("datacite-index")
    public DocumentIndexingService dataciteIndexingService() {
        return new AsyncDocumentIndexingService(
                new DataciteIndexingService(bundledReaderService(), dataciteService())
        );
    }
    
    @Bean @Qualifier("validation-index")
    public DocumentIndexingService asyncValidationIndexingService() {
        return new AsyncDocumentIndexingService(validationIndexingService());
    }
    
    @Bean(initMethod = "initialIndex") @Qualifier("mapserver-index")
    public MapServerIndexingService mapServerIndexingService() {
        MapServerIndexGenerator generator = new MapServerIndexGenerator(freemarkerConfiguration(), mapServerDetailsService());
        return new MapServerIndexingService<>(
                bundledReaderService(),
                documentListingService(),
                dataRepository,
                generator,
                mapsLocation);
    }
        
    @Bean
    @SneakyThrows
    public ValidationIndexingService validationIndexingService() {
        MediaTypeValidator html = new MediaTypeValidator("HTML Generation", MediaType.TEXT_HTML, documentWritingService());
        
        ClassMap<IndexGenerator<?, ValidationReport>> mappings = new PrioritisedClassMap<IndexGenerator<?, ValidationReport>>()
                .register(GeminiDocument.class, new ValidationIndexGenerator(Arrays.asList(
                        new XSDSchemaValidator("Gemini", MediaType.parseMediaType(GEMINI_XML_VALUE), documentWritingService(), geminiSchema),
                        html
                )))
                .register(MetadataDocument.class, new ValidationIndexGenerator(Collections.singletonList(html)));
        
        return new ValidationIndexingService<>(
                bundledReaderService(),
                documentListingService(),
                dataRepository,
                postProcessingService(),
                documentIdentifierService(),
                new IndexGeneratorRegistry<>(mappings)
        );
    }
}
