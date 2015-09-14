package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Statement;
import com.vividsolutions.jts.io.WKTReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpressionException;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.converters.UkeofXml2EFDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2GeminiDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.ExtractTopicFromDocument;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGeneratorRegistry;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexBaseMonitoringTypeGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexGeminiDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexMetadataDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndex;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexBaseMonitoringTypeGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexFacilityGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexLakeDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.lake.LakeDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.postprocess.BaseMonitoringTypePostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.ClassMapPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.GeminiDocumentPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DataRepositoryOptimizingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.services.DownloadOrderDetailsService;
import uk.ac.ceh.gateway.catalogue.services.ExtensionDocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.services.HashMapDocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.services.JacksonDocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.services.JsonDocumentWritingService;
import uk.ac.ceh.gateway.catalogue.services.MessageConverterReadingService;
import uk.ac.ceh.gateway.catalogue.services.MetadataInfoBundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;
import uk.ac.ceh.gateway.catalogue.services.TMSToWMSGetMapService;
import uk.ac.ceh.gateway.catalogue.services.TerraCatalogImporterService;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;
import uk.ac.ceh.gateway.catalogue.util.PrioritisedClassMap;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.OfflineTerraCatalogUserFactory;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.StateTranslatingMetadataInfoFactory;

/**
 * The following spring configuration will populate service beans
 * @author cjohn
 */
@Configuration
public class ServiceConfig {
    @Value("${documents.baseUri}") String baseUri;
    @Autowired RestTemplate restTemplate;
    @Autowired ObjectMapper jacksonMapper;
    @Autowired DataRepository<CatalogueUser> dataRepository;
    @Autowired Dataset jenaTdb;
    @Autowired SolrServer solrServer;
    @Autowired EventBus bus;
    @Autowired CodeLookupService codeLookupService;
    @Autowired AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory;
    @Autowired GroupStore<CatalogueUser> groupStore;
    
    @Bean
    public PermissionService permission() {
        return new PermissionService(dataRepository, documentInfoMapper(), groupStore);
    }
    
    @Bean
    public CitationService citationService() {
        return new CitationService();
    }
    
    @Bean
    public DataRepositoryOptimizingService dataRepositoryOptimizingService() {
        return new DataRepositoryOptimizingService(dataRepository);
    }
    
    @Bean
    public DocumentReadingService documentReadingService() throws XPathExpressionException, IOException {
        return new MessageConverterReadingService()
                .addMessageConverter(new Xml2GeminiDocumentMessageConverter(codeLookupService))
                .addMessageConverter(new UkeofXml2EFDocumentMessageConverter())
                .addMessageConverter(new MappingJackson2HttpMessageConverter(jacksonMapper));
    }
    
    @Bean
    public DocumentWritingService<MetadataDocument> documentWritingService() {
        return new JsonDocumentWritingService(jacksonMapper);
    }
    
    @Bean
    public DocumentTypeLookupService metadataRepresentationService() {
        return new HashMapDocumentTypeLookupService()
                .register("GEMINI_DOCUMENT", GeminiDocument.class)
                .register("EF_DOCUMENT", BaseMonitoringType.class)
                .register("IMP_DOCUMENT", ImpDocument.class)
                .register("LAKE_DOCUMENT", LakeDocument.class);
    }
    
    @Bean
    public GetCapabilitiesObtainerService getCapabilitiesObtainerService() {
        return new GetCapabilitiesObtainerService(restTemplate);
    }
    
    @Bean
    public MetadataListingService getWafListingService() throws XPathExpressionException, IOException {
        return new MetadataListingService(dataRepository, documentListingService(), bundledReaderService());
    }
    
    @Bean
    public TMSToWMSGetMapService tmsToWmsGetMapService() {
        return new TMSToWMSGetMapService();
    }
    
    @Bean
    public DownloadOrderDetailsService downloadOrderDetailsService() {
        Pattern eidchub = Pattern.compile("http:\\/\\/eidc\\.ceh\\.ac\\.uk\\/metadata.*");
        Pattern orderMan = Pattern.compile("http(s?):\\/\\/catalogue.ceh.ac.uk\\/download\\?fileIdentifier=.*");
        return new DownloadOrderDetailsService(eidchub, orderMan);
    }
    
    @Bean
    public DocumentInfoFactory<MetadataDocument, MetadataInfo> documentInfoFactory() {
        return (MetadataDocument document, MediaType contentType) -> {
            MetadataInfo toReturn = document.getMetadata();
            
            //If no MetadataInfo is attached to the document, we need to create 
            //a new one. 
            if(toReturn == null) {
                toReturn = new MetadataInfo();
            }
            
            toReturn.setRawType(contentType.toString()); //set the raw type
            toReturn.setDocumentType(metadataRepresentationService() //set the document class
                                        .getName(document.getClass()));
            return toReturn;
        };
    }
    
    @Bean
    public DocumentInfoMapper documentInfoMapper() {
        return new JacksonDocumentInfoMapper(jacksonMapper, MetadataInfo.class);
    }
    
    @Bean
    public MetadataInfoBundledReaderService bundledReaderService() throws XPathExpressionException, IOException {
        return new MetadataInfoBundledReaderService(
                dataRepository,
                documentReadingService(),
                documentInfoMapper(),
                metadataRepresentationService()
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
    public PostProcessingService postProcessingService() {
        ClassMap<PostProcessingService> mappings = new PrioritisedClassMap<PostProcessingService>()
                .register(GeminiDocument.class, new GeminiDocumentPostProcessingService(citationService(), jacksonMapper, jenaTdb))
                .register(BaseMonitoringType.class, new BaseMonitoringTypePostProcessingService(jenaTdb));
        return new ClassMapPostProcessingService(mappings);
    }
    
    @Bean
    public SolrIndexingService<MetadataDocument> documentIndexingService() throws XPathExpressionException, IOException {
        SolrIndexMetadataDocumentGenerator metadataDocument = new SolrIndexMetadataDocumentGenerator(codeLookupService, documentIdentifierService());
        SolrIndexBaseMonitoringTypeGenerator baseMonitoringType = new SolrIndexBaseMonitoringTypeGenerator(metadataDocument, solrGeometryService());
        
        ClassMap<IndexGenerator<?, SolrIndex>> mappings = new PrioritisedClassMap<IndexGenerator<?, SolrIndex>>()
            .register(GeminiDocument.class,     new SolrIndexGeminiDocumentGenerator(new ExtractTopicFromDocument(), metadataDocument, solrGeometryService(), codeLookupService))
            .register(Facility.class,           new SolrIndexFacilityGenerator(baseMonitoringType, solrGeometryService()))
            .register(LakeDocument.class,       new SolrIndexLakeDocumentGenerator(metadataDocument, solrGeometryService()))
            .register(BaseMonitoringType.class, baseMonitoringType)
            .register(MetadataDocument.class,   metadataDocument);
        
        SolrIndexingService toReturn = new SolrIndexingService<>(
                bundledReaderService(),
                documentListingService(),
                dataRepository,
                new IndexGeneratorRegistry(mappings),
                solrServer
        );
        
        performReindexIfNothingIsIndexed(toReturn);
        return toReturn;
    }
    
    @Bean
    public JenaIndexingService documentLinkingService() throws XPathExpressionException, IOException {
        JenaIndexMetadataDocumentGenerator metadataDocument = new JenaIndexMetadataDocumentGenerator(documentIdentifierService());
        
        ClassMap<IndexGenerator<?, List<Statement>>> mappings = new PrioritisedClassMap<IndexGenerator<?, List<Statement>>>()
                .register(BaseMonitoringType.class, new JenaIndexBaseMonitoringTypeGenerator(metadataDocument))
                .register(GeminiDocument.class, new JenaIndexGeminiDocumentGenerator(metadataDocument))
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
    
    @Bean
    public TerraCatalogImporterService terraCatalogImporterService() throws XPathExpressionException, IOException {
        OfflineTerraCatalogUserFactory<CatalogueUser> userFactory = new OfflineTerraCatalogUserFactory<>(phantomUserBuilderFactory);
        userFactory.put("ceh", "@ceh.ac.uk");

        StateTranslatingMetadataInfoFactory infoFactory = new StateTranslatingMetadataInfoFactory();
        infoFactory.put("private", "draft");
        infoFactory.put("public", "published");
        
        return new TerraCatalogImporterService(
                dataRepository,
                documentListingService(),
                documentIdentifierService(),
                userFactory,
                documentReadingService(),
                documentInfoMapper(),
                infoFactory
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
