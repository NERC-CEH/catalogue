package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Statement;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.converters.UkeofXml2EFDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2GeminiDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2WmsCapabilitiesMessageConverter;
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
import uk.ac.ceh.gateway.catalogue.postprocess.BaseMonitoringTypePostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.ClassMapPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.GeminiDocumentPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.services.*;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyRetriever;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyService;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;
import uk.ac.ceh.gateway.catalogue.upload.HubbubService;
import uk.ac.ceh.gateway.catalogue.upload.UploadDocumentService;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;
import uk.ac.ceh.gateway.catalogue.util.PrioritisedClassMap;
import uk.ac.ceh.gateway.catalogue.validation.MediaTypeValidator;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.XSDSchemaValidator;

import javax.xml.validation.Schema;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueServiceConfig.*;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Slf4j
@Configuration
public class ServiceConfig {
    @Value("${documents.baseUri}") private String baseUri;
    @Value("${maps.location}") private File mapsLocation;
    @Value("${jira.username}") private String jiraUsername;
    @Value("${jira.password}") private String jiraPassword;
    @Value("${jira.address}") private String jiraAddress;
    @Value("${sparql.endpoint}") private String sparqlEndpoint;
    @Value("${sparql.graph}") private String sparqlGraph;

    @Autowired private CitationService citationService;
    @Autowired private CodeLookupService codeLookupService;
    @Autowired private DataciteService dataciteService;
    @Autowired private DataRepository<CatalogueUser> dataRepository;
    @Autowired private DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private freemarker.template.Configuration freemarkerConfiguration;
    @Autowired @Qualifier("gemini") private Schema geminiSchema;
    @Autowired private HubbubService hubbubService;
    @Autowired private ObjectMapper jacksonMapper;
    @Autowired private JenaLookupService jenaLookupService;
    @Autowired private Dataset jenaTdb;
    @Autowired private MapServerDetailsService mapServerDetailsService;
    @Autowired private SolrClient solrClient;

    @Bean
    public UploadDocumentService uploadDocumentService() {
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
    public DocumentReadingService documentReadingService() {
        return new MessageConverterReadingService()
                .addMessageConverter(new Xml2GeminiDocumentMessageConverter(codeLookupService))
                .addMessageConverter(new UkeofXml2EFDocumentMessageConverter())
                .addMessageConverter(new MappingJackson2HttpMessageConverter(jacksonMapper));
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
        return new GetCapabilitiesObtainerService(restTemplate, mapServerDetailsService);
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
    public MetadataInfoBundledReaderService bundledReaderService() {
        return new MetadataInfoBundledReaderService(
            dataRepository,
            documentReadingService(),
            documentInfoMapper,
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
    public PostProcessingService postProcessingService() {
        ClassMap<PostProcessingService> mappings = new PrioritisedClassMap<PostProcessingService>()
                .register(GeminiDocument.class, new GeminiDocumentPostProcessingService(citationService, dataciteService, jenaTdb, documentIdentifierService()))
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
        solrIndexLinkDocumentGenerator.setRepository(documentRepository);
        
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
            jenaLookupService,
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
                new DataciteIndexingService(bundledReaderService(), dataciteService)
        );
    }
    
    @Bean @Qualifier("validation-index")
    public DocumentIndexingService asyncValidationIndexingService() {
        return new AsyncDocumentIndexingService(validationIndexingService());
    }
    
    @Bean(initMethod = "initialIndex") @Qualifier("mapserver-index")
    public MapServerIndexingService mapServerIndexingService() {
        MapServerIndexGenerator generator = new MapServerIndexGenerator(freemarkerConfiguration, mapServerDetailsService);
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
