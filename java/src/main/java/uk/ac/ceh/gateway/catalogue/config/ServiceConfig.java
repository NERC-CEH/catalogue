package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.vividsolutions.jts.io.WKTReader;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.validation.Schema;
import javax.xml.xpath.XPathExpressionException;
import lombok.Data;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Statement;
import org.apache.solr.client.solrj.SolrServer;
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
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.GroupStore;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_XML_VALUE;
import uk.ac.ceh.gateway.catalogue.converters.Object2TemplatedMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.TransparentProxyMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.UkeofXml2EFDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.WmsFeatureInfo2XmlMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2GeminiDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2WmsCapabilitiesMessageConverter;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Network;
import uk.ac.ceh.gateway.catalogue.ef.Programme;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.CaseStudy;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.imp.ModelApplication;
import uk.ac.ceh.gateway.catalogue.indexing.AsyncDocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.DataciteIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.ExtractTopicFromDocument;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGeneratorRegistry;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexBaseMonitoringTypeGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexGeminiDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexLinkDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexMetadataDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.MapServerIndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.MapServerIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndex;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexBaseMonitoringTypeGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexFacilityGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexImpDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexLinkDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.ValidationIndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.ValidationIndexingService;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.Catalogue.DocumentType;
import uk.ac.ceh.gateway.catalogue.model.CatalogueResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.model.ErrorResponse;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MaintenanceResponse;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource;
import uk.ac.ceh.gateway.catalogue.model.SparqlResponse;
import uk.ac.ceh.gateway.catalogue.model.ValidationResponse;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.postprocess.BaseMonitoringTypePostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.ClassMapPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.GeminiDocumentPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitDocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitRepoWrapper;
import uk.ac.ceh.gateway.catalogue.search.FacetFactory;
import uk.ac.ceh.gateway.catalogue.search.HardcodedFacetFactory;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DataRepositoryOptimizingService;
import uk.ac.ceh.gateway.catalogue.services.DataciteService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.services.DownloadOrderDetailsService;
import uk.ac.ceh.gateway.catalogue.services.ExtensionDocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.GeminiExtractorService;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.services.HashMapDocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.services.InMemoryCatalogueService;
import uk.ac.ceh.gateway.catalogue.services.JacksonDocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;
import uk.ac.ceh.gateway.catalogue.services.MessageConverterReadingService;
import uk.ac.ceh.gateway.catalogue.services.MessageConverterWritingService;
import uk.ac.ceh.gateway.catalogue.services.MetadataInfoBundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;
import uk.ac.ceh.gateway.catalogue.services.TMSToWMSGetMapService;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;
import uk.ac.ceh.gateway.catalogue.util.PrioritisedClassMap;
import uk.ac.ceh.gateway.catalogue.validation.MediaTypeValidator;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.XSDSchemaValidator;

/**
 * The following spring configuration will populate service beans
 * @author cjohn
 */
@Configuration
public class ServiceConfig {
    @Value("${documents.baseUri}") String baseUri;
    @Value("${template.location}") File templates;
    @Value("${maps.location}") File mapsLocation;
    @Value("${doi.prefix}") String doiPrefix;
    @Value("${doi.username}") String doiUsername;
    @Value("${doi.password}") String doiPassword;
    @Autowired ObjectMapper jacksonMapper;
    @Autowired DataRepository<CatalogueUser> dataRepository;
    @Autowired Dataset jenaTdb;
    @Autowired SolrServer solrServer;
    @Autowired EventBus bus;
    @Autowired CodeLookupService codeLookupService;
    @Autowired AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory;
    @Autowired GroupStore<CatalogueUser> groupStore;
    @Autowired @Qualifier("gemini") Schema geminiSchema;
    
    private static final String GEMINI_DOCUMENT = "GEMINI_DOCUMENT";
    private static final String EF_DOCUMENT = "EF_DOCUMENT";
    private static final String IMP_DOCUMENT = "IMP_DOCUMENT";
    private static final String CEH_MODEL = "CEH_MODEL";
    private static final String CEH_MODEL_APPLICATION = "CEH_MODEL_APPLICATION";
    private static final String LINK_DOCUMENT = "LINK_DOCUMENT";
    
    @Bean
    public CatalogueService catalogueService() {
        String defaultCatalogueKey = "eidc";
        
        DocumentType gemini = DocumentType.builder()
            .title("Data Resource")
            .type(GEMINI_DOCUMENT)
            .build();
        
        DocumentType ef = DocumentType.builder()
            .title("Monitoring")
            .type(EF_DOCUMENT)
            .build();
        
        DocumentType imp = DocumentType.builder()
            .title("Model")
            .type(IMP_DOCUMENT)
            .build();
        
        DocumentType cehModel = DocumentType.builder()
            .title("Model")
            .type(CEH_MODEL)
            .build();
        
        DocumentType cehModelApplication = DocumentType.builder()
            .title("Model Application")
            .type(CEH_MODEL_APPLICATION)
            .build();
        
        DocumentType link = DocumentType.builder()
            .title("Link")
            .type(LINK_DOCUMENT)
            .build();
        
        return new InMemoryCatalogueService(
            defaultCatalogueKey,
            
            Catalogue.builder()
                .id("osdp")
                .title("Open Soils Data Platform")
                .url("http://www.ceh.ac.uk")
                .facetKey("resourceType")
                .fileUpload(false)
                .build(),
            
            Catalogue.builder()
                .id("m")
                .title("Modelling")
                .url("http://www.ceh.ac.uk")
                .facetKey("resourceType")
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .fileUpload(false)
                .build(),
            
            Catalogue.builder()
                .id("nc")
                .title("Natural Capital")
                .url("http://www.ceh.ac.uk")
                .facetKey("resourceType")
                .documentType(gemini)
                .documentType(link)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("inms")
                .title("International Nitrogen Management System")
                .url("http://www.ceh.ac.uk")
                .facetKey("resourceType")
                .documentType(gemini)
                .documentType(imp)
                .documentType(link)
                .fileUpload(true)
                .build(),
            
            Catalogue.builder()
                .id("edge")
                .title("EDgE")
                .url("https://edge.climate.copernicus.eu")
                .facetKey("resourceType")
                .documentType(gemini)
                .documentType(link)
                .fileUpload(true)
                .build(),
            
            Catalogue.builder()
                .id("ceh")
                .title("Centre for Ecology & Hydrology")
                .url("https://eip.ceh.ac.uk")
                .facetKey("topic")
                .facetKey("resourceType")
                .facetKey("licence")
                .fileUpload(false)
                .build(),
        
            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("Environmental Information Data Centre")
                .url("http://eidc.ceh.ac.uk")
                .facetKey("topic")
                .facetKey("resourceType")
                .facetKey("licence")
                .documentType(gemini)
                .fileUpload(false)
                .build(),
        
            Catalogue.builder()
                .id("cmp")
                .title("Catchment Management Modelling Platform")
                .url("http://www.cammp.org.uk/index.php")
                .facetKey("resourceType")
                .facetKey("impCaMMPIssues")
                .facetKey("impDataType")
                .facetKey("impScale")
                .facetKey("impTopic")
                .facetKey("impWaterPollutant")
                .documentType(gemini)
                .documentType(imp)
                .documentType(link)
                .fileUpload(true)
                .build(),
        
            Catalogue.builder()
                .id("assist")
                .title("Achieving Sustainable Agricultural Systems")
                .url("http://www.ceh.ac.uk/ASSIST")
                .facetKey("resourceType")
                .facetKey("licence")
                .documentType(gemini)
                .documentType(link)
                .fileUpload(false)
                .build(),
            
            Catalogue.builder()
                .id("inlicensed")
                .title("CEH In-licensed Datasets")
                .url("http://intranet.ceh.ac.uk/procedures/commercialisation/data-licensing-ipr/in-licensed-data-list")
                .facetKey("resourceType")
                .documentType(gemini)
                .fileUpload(false)
                .build()
        );
    }
    
    @Bean FacetFactory facetFactory() {
        return new HardcodedFacetFactory();
    }
    
    @Bean
    public PermissionService permission() {
        return new PermissionService(dataRepository, documentInfoMapper(), groupStore);
    }
    
    @Bean
    public CitationService citationService() {
        return new CitationService();
    }
    
    @Bean
    public MapServerDetailsService mapServerDetailsService() {
        return new MapServerDetailsService(baseUri);
    }
    
    @Bean
    public DataciteService dataciteService() throws TemplateModelException, IOException {
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
    public MessageConvertersHolder messageConverters() throws TemplateModelException, IOException {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(jacksonMapper);
        
        // EF Message Converters  
        converters.add(new Object2TemplatedMessageConverter(Activity.class,  freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(Facility.class,  freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(Network.class,   freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(Programme.class, freemarkerConfiguration()));
        converters.add(new UkeofXml2EFDocumentMessageConverter());
        
        // IMP Message Converters
        converters.add(new Object2TemplatedMessageConverter(Model.class,            freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(ModelApplication.class, freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(CaseStudy.class,        freemarkerConfiguration()));
        
        // CEH model catalogue
        converters.add(new Object2TemplatedMessageConverter(CehModel.class,             freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(CehModelApplication.class,  freemarkerConfiguration()));
        
        // Gemini Message Converters
        converters.add(new Object2TemplatedMessageConverter(GeminiDocument.class,       freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(LinkDocument.class,         freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(SearchResults.class,        freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(Citation.class,             freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(StateResource.class,        freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(PermissionResource.class,   freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(CatalogueResource.class,    freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(MaintenanceResponse.class,  freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(SparqlResponse.class,       freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(ValidationResponse.class,   freemarkerConfiguration()));
        converters.add(new Object2TemplatedMessageConverter(ErrorResponse.class,        freemarkerConfiguration()));
        converters.add(new TransparentProxyMessageConverter(httpClient()));
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        converters.add(new WmsFeatureInfo2XmlMessageConverter());
        converters.add(mappingJackson2HttpMessageConverter);
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
    public freemarker.template.Configuration freemarkerConfiguration() throws TemplateModelException, IOException {
        Map<String, Object> shared = new HashMap<>();
        shared.put("jena", jenaLookupService());
        shared.put("codes", codeLookupService);
        shared.put("downloadOrderDetails", downloadOrderDetailsService());
        shared.put("permission", permission());
        shared.put("mapServerDetails", mapServerDetailsService());
        shared.put("geminiHelper", geminiExtractorService());
        shared.put("catalogues", catalogueService());
        
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
    DocumentRepository documentRepository() throws XPathExpressionException, IOException, TemplateModelException {
        return new GitDocumentRepository(
            metadataRepresentationService(),
            documentReadingService(),
            documentIdentifierService(),
            documentWritingService(),
            bundledReaderService(),
            postProcessingService(),
            gitRepoWrapper()
        );
    }
    
    @Bean
    public DocumentReadingService documentReadingService() throws XPathExpressionException, IOException {
        return new MessageConverterReadingService()
                .addMessageConverter(new Xml2GeminiDocumentMessageConverter(codeLookupService))
                .addMessageConverter(new UkeofXml2EFDocumentMessageConverter())
                .addMessageConverter(new MappingJackson2HttpMessageConverter(jacksonMapper));
    }
    
    @Bean
    public DocumentWritingService documentWritingService() throws TemplateModelException, IOException {
        return new MessageConverterWritingService(messageConverters().getConverters());
    }
    
    @Bean
    public DocumentTypeLookupService metadataRepresentationService() {
        return new HashMapDocumentTypeLookupService()
                .register(GEMINI_DOCUMENT, GeminiDocument.class)
                .register(EF_DOCUMENT, BaseMonitoringType.class)
                .register(IMP_DOCUMENT, ImpDocument.class)
                .register(LINK_DOCUMENT, LinkDocument.class)
                .register(CEH_MODEL, CehModel.class)
                .register(CEH_MODEL_APPLICATION, CehModelApplication.class);
    }
    
    @Bean
    public GetCapabilitiesObtainerService getCapabilitiesObtainerService() throws XPathExpressionException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Arrays.asList(
                new Xml2WmsCapabilitiesMessageConverter()
        ));
        return new GetCapabilitiesObtainerService(restTemplate, mapServerDetailsService());
    }
    
    @Bean
    public MetadataListingService getWafListingService() throws XPathExpressionException, IOException, TemplateModelException {
        return new MetadataListingService(dataRepository, documentListingService(), bundledReaderService());
    }
    
    @Bean
    public TMSToWMSGetMapService tmsToWmsGetMapService() {
        return new TMSToWMSGetMapService();
    }
    
    @Bean
    public GeminiExtractorService geminiExtractorService() {
        return new GeminiExtractorService();
    }
    
    @Bean
    public DownloadOrderDetailsService downloadOrderDetailsService() {
        Pattern eidchub = Pattern.compile("http:\\/\\/eidc\\.ceh\\.ac\\.uk\\/metadata.*");
        Pattern orderMan = Pattern.compile("http(s?):\\/\\/catalogue.ceh.ac.uk\\/download\\?fileIdentifier=.*");
        return new DownloadOrderDetailsService(eidchub, orderMan);
    }
    
    @Bean
    public DocumentInfoMapper documentInfoMapper() {
        return new JacksonDocumentInfoMapper(jacksonMapper, MetadataInfo.class);
    }
    
    @Bean
    public MetadataInfoBundledReaderService bundledReaderService() throws XPathExpressionException, IOException, TemplateModelException {
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
    public SolrGeometryService solrGeometryService() {
        return new SolrGeometryService(new WKTReader());
    }
    
    @Bean
    public PostProcessingService postProcessingService() throws TemplateModelException, IOException {
        ClassMap<PostProcessingService> mappings = new PrioritisedClassMap<PostProcessingService>()
                .register(GeminiDocument.class, new GeminiDocumentPostProcessingService(citationService(), dataciteService(), jenaTdb, documentIdentifierService()))
                .register(BaseMonitoringType.class, new BaseMonitoringTypePostProcessingService(jenaTdb));
        return new ClassMapPostProcessingService(mappings);
    }
    
    @Bean @Qualifier("solr-index")
    public SolrIndexingService<MetadataDocument> documentIndexingService() throws XPathExpressionException, IOException, TemplateModelException {
        SolrIndexMetadataDocumentGenerator metadataDocument = new SolrIndexMetadataDocumentGenerator(codeLookupService, documentIdentifierService());
        SolrIndexBaseMonitoringTypeGenerator baseMonitoringType = new SolrIndexBaseMonitoringTypeGenerator(metadataDocument, solrGeometryService());
        SolrIndexLinkDocumentGenerator solrIndexLinkDocumentGenerator = new SolrIndexLinkDocumentGenerator(documentRepository());
        
        ClassMap<IndexGenerator<?, SolrIndex>> mappings = new PrioritisedClassMap<IndexGenerator<?, SolrIndex>>()
            .register(GeminiDocument.class,     new SolrIndexGeminiDocumentGenerator(new ExtractTopicFromDocument(), metadataDocument, solrGeometryService(), codeLookupService))
            .register(ImpDocument.class,        new SolrIndexImpDocumentGenerator(metadataDocument))
            .register(Facility.class,           new SolrIndexFacilityGenerator(baseMonitoringType, solrGeometryService()))
            .register(BaseMonitoringType.class, baseMonitoringType)
            .register(LinkDocument.class,     solrIndexLinkDocumentGenerator)
            .register(MetadataDocument.class,   metadataDocument);
        
        IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry = new IndexGeneratorRegistry(mappings);
        
        SolrIndexingService toReturn = new SolrIndexingService<>(
                bundledReaderService(),
                documentListingService(),
                dataRepository,
                indexGeneratorRegistry,
                solrServer,
                jenaLookupService(),
                documentIdentifierService()
        );
        
        solrIndexLinkDocumentGenerator.setIndexGeneratorRegistry(indexGeneratorRegistry);
        
        performReindexIfNothingIsIndexed(toReturn);
        return toReturn;
    }
    
    @Bean @Qualifier("jena-index")
    public JenaIndexingService documentLinkingService() throws XPathExpressionException, IOException, TemplateModelException {
        JenaIndexMetadataDocumentGenerator metadataDocument = new JenaIndexMetadataDocumentGenerator(documentIdentifierService());
        
        ClassMap<IndexGenerator<?, List<Statement>>> mappings = new PrioritisedClassMap<IndexGenerator<?, List<Statement>>>()
                .register(BaseMonitoringType.class, new JenaIndexBaseMonitoringTypeGenerator(metadataDocument))
                .register(GeminiDocument.class, new JenaIndexGeminiDocumentGenerator(metadataDocument))
                .register(LinkDocument.class, new JenaIndexLinkDocumentGenerator(metadataDocument))
                .register(MetadataDocument.class, metadataDocument);
        
        JenaIndexingService toReturn = new JenaIndexingService(
                bundledReaderService(),
                documentListingService(),
                dataRepository,
                new IndexGeneratorRegistry(mappings),
                documentIdentifierService(),
                jenaTdb
        );
        
        performReindexIfNothingIsIndexed(toReturn);
        return toReturn;
    }
    
    @Bean @Qualifier("datacite-index")
    public DocumentIndexingService dataciteIndexingService() throws XPathExpressionException, IOException, TemplateModelException {
        return new AsyncDocumentIndexingService(
                new DataciteIndexingService(bundledReaderService(), dataciteService())
        );
    }
    
    @Bean @Qualifier("validation-index")
    public DocumentIndexingService asyncValidationIndexingService() throws Exception {
        DocumentIndexingService toReturn = new AsyncDocumentIndexingService(validationIndexingService());
        
        performReindexIfNothingIsIndexed(toReturn);
        return toReturn;
    }
    
    @Bean @Qualifier("mapserver-index")
    public MapServerIndexingService mapServerIndexingService() throws Exception {
        MapServerIndexGenerator generator = new MapServerIndexGenerator(freemarkerConfiguration(), mapServerDetailsService());
        MapServerIndexingService toReturn = new MapServerIndexingService(
                bundledReaderService(),
                documentListingService(),
                dataRepository,
                generator,
                mapsLocation);
        
        performReindexIfNothingIsIndexed(toReturn);
        return toReturn;
    }
        
    @Bean 
    public ValidationIndexingService validationIndexingService() throws Exception {
        MediaTypeValidator html = new MediaTypeValidator("HTML Generation", MediaType.TEXT_HTML, documentWritingService());
        
        ClassMap<IndexGenerator<?, ValidationReport>> mappings = new PrioritisedClassMap<IndexGenerator<?, ValidationReport>>()
                .register(GeminiDocument.class, new ValidationIndexGenerator(Arrays.asList(
                        new XSDSchemaValidator("Gemini", MediaType.parseMediaType(GEMINI_XML_VALUE), documentWritingService(), geminiSchema),
                        html
                )))
                .register(MetadataDocument.class, new ValidationIndexGenerator(Arrays.asList(
                        html
                )));
        
        return new ValidationIndexingService(
                bundledReaderService(),
                documentListingService(),
                dataRepository,
                postProcessingService(),
                documentIdentifierService(),
                new IndexGeneratorRegistry(mappings)
        );
    }
    
    //Perform an initial index of solr if their is no content inside
    protected void performReindexIfNothingIsIndexed(DocumentIndexingService service) {
        try {
            if(service.isIndexEmpty()) {
                service.rebuildIndex();
            }
        }
        catch(DocumentIndexingException ex) {
            //Indexing or reading from solr failed... 
            bus.post(ex); //Silently hand over to the event bus
        }
    }
}
