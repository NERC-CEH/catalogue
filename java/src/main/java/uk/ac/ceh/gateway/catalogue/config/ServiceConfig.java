package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
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
import uk.ac.ceh.gateway.catalogue.converters.*;
import uk.ac.ceh.gateway.catalogue.ef.*;
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
import uk.ac.ceh.gateway.catalogue.postprocess.GeminiDocumentPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.services.*;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;
import uk.ac.ceh.gateway.catalogue.util.PrioritisedClassMap;
import uk.ac.ceh.gateway.catalogue.validation.MediaTypeValidator;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.XSDSchemaValidator;

import javax.xml.validation.Schema;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Configuration
public class ServiceConfig {
    @Value("${documents.baseUri}") private String baseUri;
    @Value("${template.location}") private File templates;
    @Value("${maps.location}") private File mapsLocation;
    @Value("${doi.prefix}") private String doiPrefix;
    @Value("${doi.username}") private String doiUsername;
    @Value("${doi.password}") private String doiPassword;
    @Value("#{systemEnvironment['JIRA_USERNAME']}") private String jiraUsername;
    @Value("#{systemEnvironment['JIRA_PASSWORD']}") private String jiraPassword;
    @Value("${jira.address}") private String jiraAddress;
    @Value("#{systemEnvironment['PLONE_USERNAME']}") private String ploneUsername;
    @Value("#{systemEnvironment['PLONE_PASSWORD']}") private String plonePassword;
    @Value("${plone.address}") private String ploneAddress;
    
    @Autowired private ObjectMapper jacksonMapper;
    @Autowired private DataRepository<CatalogueUser> dataRepository;
    @Autowired private Dataset jenaTdb;
    @Autowired private SolrServer solrServer;
    @Autowired private EventBus bus;
    @Autowired @Qualifier("gemini") private Schema geminiSchema;
    @Autowired private JenaLookupService jenaLookupService;
    @Autowired private CodeLookupService codeLookupService;
    @Autowired private DownloadOrderDetailsService downloadOrderDetailsService;
    @Autowired private PermissionService permissionService;
    @Autowired private MapServerDetailsService mapServerDetailsService;
    @Autowired private GeminiExtractorService geminiExtractorService;
    @Autowired private CatalogueService catalogueService;
    @Autowired private CitationService citationService;
    @Autowired private DataciteService dataciteService;
    @Autowired private DocumentIdentifierService documentIdentifierService;
    @Autowired private BundledReaderService<MetadataDocument> bundledReaderService;
    @Autowired private DocumentListingService documentListingService;
    @Autowired private SolrGeometryService solrGeometryService;
    @Autowired private SolrIndexMetadataDocumentGenerator solrIndexMetadataDocumentGenerator;
    @Autowired private SolrIndexBaseMonitoringTypeGenerator solrIndexBaseMonitoringTypeGenerator;
    @Autowired private SolrIndexLinkDocumentGenerator solrIndexLinkDocumentGenerator;
    @Autowired private JenaIndexMetadataDocumentGenerator jenaIndexMetadataDocumentGenerator;
    @Autowired private PostProcessingService<MetadataDocument> postProcessingService;
    @Autowired private MapServerIndexGenerator mapServerIndexGenerator;
    
    public static final String GEMINI_DOCUMENT = "GEMINI_DOCUMENT";
    public static final String EF_DOCUMENT = "EF_DOCUMENT";
    public static final String IMP_DOCUMENT = "IMP_DOCUMENT";
    public static final String CEH_MODEL = "CEH_MODEL";
    public static final String CEH_MODEL_APPLICATION = "CEH_MODEL_APPLICATION";
    public static final String LINK_DOCUMENT = "LINK_DOCUMENT";

    private final File dropbox = new File("/var/ceh-catalogue/dropbox");
    private final char replacement = '-';
    private final String eidcPattern = "http:\\/\\/eidc\\.ceh\\.ac\\.uk\\/metadata.*";
    private final String orderManagerPattern = "http(s?):\\/\\/catalogue.ceh.ac.uk\\/download\\?fileIdentifier=.*";
    
    @Bean
    public WebResource jira() {
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(jiraUsername, jiraPassword));
        return client.resource(jiraAddress);
    }

    @Bean
    public WebResource plone() {
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(ploneUsername, plonePassword));
        return client.resource(ploneAddress);
    }
    
    @Bean
    public MessageConvertersHolder messageConverters() throws TemplateModelException, IOException {
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
        shared.put("jena", jenaLookupService);
        shared.put("codes", codeLookupService);
        shared.put("downloadOrderDetails", downloadOrderDetailsService);
        shared.put("permission", permissionService);
        shared.put("mapServerDetails", mapServerDetailsService);
        shared.put("geminiHelper", geminiExtractorService);
        shared.put("catalogues", catalogueService);
        
        freemarker.template.Configuration config = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_22);
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setSharedVaribles(shared);
        config.setDefaultEncoding("UTF-8");
        config.setTemplateLoader(new FileTemplateLoader(templates));
        return config;
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
                .register(CEH_MODEL_APPLICATION, CehModelApplication.class)
                .register(OSDP_AGENT_SHORT, Agent.class)
                .register(OSDP_DATASET_SHORT, uk.ac.ceh.gateway.catalogue.osdp.Dataset.class)
                .register(OSDP_MODEL_SHORT, uk.ac.ceh.gateway.catalogue.osdp.Model.class)
                .register(OSDP_MONITORING_ACTIVITY_SHORT, MonitoringActivity.class)
                .register(OSDP_MONITORING_FACILITY_SHORT, MonitoringFacility.class)
                .register(OSDP_MONITORING_PROGRAMME_SHORT, MonitoringProgramme.class)
                .register(OSDP_PUBLICATION_SHORT, Publication.class)
                .register(OSDP_SAMPLE_SHORT, Sample.class);
    }
    
    @Bean
    public RestTemplate capabilitiesObtainer() throws XPathExpressionException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Arrays.asList(
                new Xml2WmsCapabilitiesMessageConverter()
        ));
        return restTemplate;
    }
    
    @Bean
    public DocumentInfoMapper<MetadataInfo> documentInfoMapper() {
        return new JacksonDocumentInfoMapper<>(jacksonMapper, MetadataInfo.class);
    }
    
    @Bean
    public ClassMap<PostProcessingService> postProcessingServiceClassMap() {
        return new PrioritisedClassMap<PostProcessingService>()
            .register(GeminiDocument.class,
                new GeminiDocumentPostProcessingService(
                    citationService,
                    dataciteService,
                    jenaTdb,
                    documentIdentifierService
                )
            )
            .register(BaseMonitoringType.class,
                new BaseMonitoringTypePostProcessingService(jenaTdb));
    }

    @Bean
    public IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry() {
        return new IndexGeneratorRegistry<>(solrIndexGeneratorClassMap());
    }

    @Bean
    ClassMap<IndexGenerator<?, SolrIndex>> solrIndexGeneratorClassMap() {
        return new PrioritisedClassMap<IndexGenerator<?, SolrIndex>>()
            .register(GeminiDocument.class, new SolrIndexGeminiDocumentGenerator(new ExtractTopicFromDocument(), solrIndexMetadataDocumentGenerator, solrGeometryService, codeLookupService))
            .register(Facility.class, new SolrIndexFacilityGenerator(solrIndexBaseMonitoringTypeGenerator, solrGeometryService))
            .register(BaseMonitoringType.class, solrIndexBaseMonitoringTypeGenerator)
            .register(LinkDocument.class, solrIndexLinkDocumentGenerator)
            .register(MonitoringFacility.class, new SolrIndexOsdpMonitoringFacilityGenerator(solrIndexMetadataDocumentGenerator, solrGeometryService))
            .register(MetadataDocument.class, solrIndexMetadataDocumentGenerator);
    }

    @Bean
    ClassMap<IndexGenerator<?, List<Statement>>> jenaIndexGeneratorClassMap() {
        return new PrioritisedClassMap<IndexGenerator<?, List<Statement>>>()
            .register(BaseMonitoringType.class, new JenaIndexBaseMonitoringTypeGenerator(jenaIndexMetadataDocumentGenerator))
            .register(GeminiDocument.class, new JenaIndexGeminiDocumentGenerator(jenaIndexMetadataDocumentGenerator))
            .register(LinkDocument.class, new JenaIndexLinkDocumentGenerator(jenaIndexMetadataDocumentGenerator))
            .register(MetadataDocument.class, jenaIndexMetadataDocumentGenerator);
    }
    
    @Bean @Qualifier("datacite-index")
    public DocumentIndexingService dataciteIndexingService() throws XPathExpressionException, IOException, TemplateModelException {
        return new AsyncDocumentIndexingService(
            new DataciteIndexingService(bundledReaderService, dataciteService)
        );
    }
    
    @Bean @Qualifier("validation-index")
    public DocumentIndexingService asyncValidationIndexingService() throws Exception {
        return new AsyncDocumentIndexingService(validationIndexingService());
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
        
        return new ValidationIndexingService<MetadataDocument>(
                bundledReaderService,
                documentListingService,
                dataRepository,
                postProcessingService,
                documentIdentifierService,
                new IndexGeneratorRegistry(mappings)
        );
    }
}
