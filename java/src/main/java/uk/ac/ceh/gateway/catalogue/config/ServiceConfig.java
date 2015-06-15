package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
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
import uk.ac.ceh.gateway.catalogue.converters.Xml2UKEOFDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.MetadataDocumentSolrIndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.ExtractTopicFromDocument;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.linking.GitDocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.LinkDatabase;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
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
import uk.ac.ceh.gateway.catalogue.services.TerraCatalogImporterService;
import uk.ac.ceh.gateway.catalogue.ukeof.UKEOFDocument;
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
    @Autowired LinkDatabase linkDatabase;
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
    public DocumentReadingService documentReadingService() throws XPathExpressionException, IOException {
        return new MessageConverterReadingService()
                .addMessageConverter(new Xml2GeminiDocumentMessageConverter(codeLookupService))
                .addMessageConverter(new Xml2UKEOFDocumentMessageConverter())
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
                .register("UKEOF_DOCUMENT", UKEOFDocument.class);
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
    public SolrIndexingService<MetadataDocument> documentIndexingService() throws XPathExpressionException, IOException {
        SolrIndexingService toReturn = new SolrIndexingService<>(
                bundledReaderService(),
                documentListingService(),
                dataRepository,
                new MetadataDocumentSolrIndexGenerator(new ExtractTopicFromDocument(), codeLookupService),
                solrServer
        );
        
        performReindexIfNothingIsIndexed(toReturn);
        return toReturn;
    }
    
    @Bean
    public GitDocumentLinkService documentLinkingService() throws XPathExpressionException, IOException {
        GitDocumentLinkService toReturn = new GitDocumentLinkService(
                dataRepository,
                bundledReaderService(),
                documentListingService(),
                linkDatabase
        );
        
        performRelinkIfNothingIsLinked(toReturn);
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
                userFactory,
                documentReadingService(),
                documentInfoMapper(),
                infoFactory
        );
    }
    
    //Perform an initial index of solr if their is no content inside
    protected void performReindexIfNothingIsIndexed(SolrIndexingService<?> service) {
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
    
    //Perform an initial relink if there is no links already populated
    protected void performRelinkIfNothingIsLinked(DocumentLinkService service) {
        try {
            if(service.isEmpty()) {
                service.rebuildLinks();
            }
        }
        catch(DocumentLinkingException ex) {
            bus.post(ex);
        }
    }
}
