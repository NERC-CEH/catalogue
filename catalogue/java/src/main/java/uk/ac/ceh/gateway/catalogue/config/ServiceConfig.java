package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.hp.hpl.jena.rdf.model.Model;
import com.vividsolutions.jts.io.WKTReader;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpressionException;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.converters.Xml2GeminiDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.UkeofXml2EFDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGeneratorRegistry;
import uk.ac.ceh.gateway.catalogue.indexing.ExtractTopicFromDocument;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.MetadataDocumentJenaIndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexBaseMonitoringTypeGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexFacilityGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DataRepositoryOptimizingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.HashMapDocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.ExtensionDocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.JacksonDocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.MessageConverterReadingService;
import uk.ac.ceh.gateway.catalogue.services.MetadataInfoBundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.services.DownloadOrderDetailsService;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;
import uk.ac.ceh.gateway.catalogue.services.JsonDocumentWritingService;
import uk.ac.ceh.gateway.catalogue.services.TMSToWMSGetMapService;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;
import uk.ac.ceh.gateway.catalogue.services.TerraCatalogImporterService;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;
import uk.ac.ceh.gateway.catalogue.util.MostSpecificClassMap;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.OfflineTerraCatalogUserFactory;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.StateTranslatingMetadataInfoFactory;

/**
 * The following spring configuration will populate service beans
 * @author cjohn
 */
@Configuration
public class ServiceConfig {
    @Autowired RestTemplate restTemplate;
    @Autowired ObjectMapper jacksonMapper;
    @Autowired DataRepository<CatalogueUser> dataRepository;
    @Autowired Model jenaTdb;
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
                .register("EF_DOCUMENT", BaseMonitoringType.class);
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
        return new DocumentIdentifierService('-');
    }
    
    @Bean
    public SolrGeometryService solrGeometryService() {
        return new SolrGeometryService(new WKTReader());
    }
    
    @Bean
    public SolrIndexingService<MetadataDocument> documentIndexingService() throws XPathExpressionException, IOException {
        SolrIndexMetadataDocumentGenerator metadataDocument = new SolrIndexMetadataDocumentGenerator(new ExtractTopicFromDocument(), codeLookupService, documentIdentifierService());
        SolrIndexBaseMonitoringTypeGenerator baseMonitoringType = new SolrIndexBaseMonitoringTypeGenerator(metadataDocument, solrGeometryService());
        
        ClassMap<IndexGenerator> mappings = new MostSpecificClassMap<IndexGenerator>()
            .register(GeminiDocument.class,     new SolrIndexGeminiDocumentGenerator(metadataDocument, solrGeometryService(), codeLookupService))
            .register(Facility.class,           new SolrIndexFacilityGenerator(baseMonitoringType, solrGeometryService()))
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
        JenaIndexingService toReturn = new JenaIndexingService(
                bundledReaderService(),
                documentListingService(),
                dataRepository,
                new MetadataDocumentJenaIndexGenerator(documentIdentifierService()),
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
