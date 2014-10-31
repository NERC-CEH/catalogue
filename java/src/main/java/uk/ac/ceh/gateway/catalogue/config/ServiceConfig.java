package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import javax.xml.xpath.XPathExpressionException;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.converters.Xml2GeminiDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.gemini.CrazyScienceAreaIndexer;
import uk.ac.ceh.gateway.catalogue.converters.Xml2UKEOFDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.MetadataDocumentSolrIndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.linking.GitDocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.LinkDatabase;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.services.HashMapDocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.ExtensionDocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.JacksonDocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.MessageConverterReadingService;
import uk.ac.ceh.gateway.catalogue.services.MetadataInfoBundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.services.TMSToWMSGetMapService;
import uk.ac.ceh.gateway.catalogue.ukeof.UKEOFDocument;

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
    
    @Bean
    public CitationService citationService() {
        return new CitationService();
    }
        
    @Bean
    public DocumentReadingService documentReadingService() throws XPathExpressionException {
        return new MessageConverterReadingService()
                .addMessageConverter(new Xml2GeminiDocumentMessageConverter())
                .addMessageConverter(new Xml2UKEOFDocumentMessageConverter());
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
    public TMSToWMSGetMapService tmsToWmsGetMapService() {
        return new TMSToWMSGetMapService();
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
    public MetadataInfoBundledReaderService bundledReaderService() throws XPathExpressionException {
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
    public SolrIndexingService<MetadataDocument> documentIndexingService() throws XPathExpressionException {
        SolrIndexingService toReturn = new SolrIndexingService<>(
                bundledReaderService(),
                documentListingService(),
                dataRepository,
                new MetadataDocumentSolrIndexGenerator(new CrazyScienceAreaIndexer()),
                solrServer
        );
        
        performReindexIfNothingIsIndexed(toReturn);
        return toReturn;
    }
    
    @Bean
    public GitDocumentLinkService documentLinkingService() throws XPathExpressionException {
        GitDocumentLinkService toReturn = new GitDocumentLinkService(
                dataRepository,
                bundledReaderService(),
                linkDatabase
        );
        
        performRelinkIfNothingIsLinked(toReturn);
        return toReturn;
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
