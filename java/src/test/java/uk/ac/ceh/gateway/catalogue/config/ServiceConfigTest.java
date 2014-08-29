package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import javax.xml.xpath.XPathExpressionException;
import org.apache.solr.client.solrj.SolrServer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.linking.GitDocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.LinkDatabase;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.ExtensionDocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.MetadataInfoBundledReaderService;

/**
 *
 * @author cjohn
 */
public class ServiceConfigTest {
    @Mock EventBus bus;
    @Mock ObjectMapper jacksonMapper;
    @Mock DataRepository dataRepository;
    @Mock SolrServer solrServer;
    @Mock LinkDatabase linkDatabase;
    
    private ServiceConfig services;
    
    @Before
    public void createServiceConfig() {
        MockitoAnnotations.initMocks(this);
        services = spy(new ServiceConfig());
        services.jacksonMapper = jacksonMapper;
        services.dataRepository = dataRepository;
        services.solrServer = solrServer;
        services.bus = bus;
        services.linkDatabase = linkDatabase;
    }
    
    @Test
    public void checkDocumentInfoFactorySetsMediaType() {
        //Given
        DocumentInfoFactory<MetadataDocument, MetadataInfo> documentInfoFactory = services.documentInfoFactory();
        GeminiDocument document = mock(GeminiDocument.class);
        MediaType type = MediaType.TEXT_HTML;
        
        //When
        MetadataInfo info = documentInfoFactory.createInfo(document, type);
        
        //Then
        assertEquals("Expected to find the text/html on metadata info", "text/html", info.getRawType());
    }
    
    @Test
    public void checkThatAProvidedMediaTypeIsOverwritten() {
        //Given
        DocumentInfoFactory<MetadataDocument, MetadataInfo> documentInfoFactory = services.documentInfoFactory();
        GeminiDocument documentWithMetadataInfo = mock(GeminiDocument.class);
        MetadataInfo providedInfo = new MetadataInfo();
        //set the value of the raw type. This needs to be hidden
        providedInfo.setRawType(MediaType.IMAGE_PNG_VALUE);
        when(documentWithMetadataInfo.getMetadata()).thenReturn(providedInfo);
        
        //When
        MetadataInfo metadataInfo = documentInfoFactory.createInfo(documentWithMetadataInfo, MediaType.TEXT_HTML);
        
        //Then
        assertEquals("Expected to find the set media type", MediaType.TEXT_HTML, metadataInfo.getRawMediaType());
    }
    
    @Test
    public void checkIfNoMetadataDocumentIsPresentInGeminiANewOneIsCreated() {
        //Given
        DocumentInfoFactory<MetadataDocument, MetadataInfo> documentInfoFactory = services.documentInfoFactory();
        GeminiDocument documentWithMetadataInfo = mock(GeminiDocument.class);
        when(documentWithMetadataInfo.getMetadata()).thenReturn(null);
        
        //When
        MetadataInfo metadataInfo = documentInfoFactory.createInfo(documentWithMetadataInfo, MediaType.IMAGE_PNG);
        
        //Then
        assertNotNull("Didn't expect the metadata info to be null", metadataInfo);
        assertEquals("Expected to find image png media type", MediaType.IMAGE_PNG, metadataInfo.getRawMediaType());
    }
    
    @Test
    public void checkThatIndexingServiceIsRequestedToBeIndexedAfterCreation() throws XPathExpressionException {
        //Given
        doNothing().when(services).performReindexIfNothingIsIndexed(any(SolrIndexingService.class));
        
        //When
        SolrIndexingService<MetadataDocument> documentIndexingService = services.documentIndexingService();
        
        //Then
        verify(services).performReindexIfNothingIsIndexed(documentIndexingService);
    }
    
    @Test
    public void checkThatIndexingServiceIsReindexedIfEmpty() throws DocumentIndexingException {
        //Given
        SolrIndexingService<GeminiDocument> documentIndexingService = mock(SolrIndexingService.class);
        when(documentIndexingService.isIndexEmpty()).thenReturn(true);
        
        //When
        services.performReindexIfNothingIsIndexed(documentIndexingService);
        
        //Then
        verify(documentIndexingService).rebuildIndex();
    }
    
    @Test
    public void checkThatIndexingServiceIsNotReindexIfPopulated() throws DocumentIndexingException {
        //Given
        SolrIndexingService<GeminiDocument> documentIndexingService = mock(SolrIndexingService.class);
        when(documentIndexingService.isIndexEmpty()).thenReturn(false);
        
        //When
        services.performReindexIfNothingIsIndexed(documentIndexingService);
        
        //Then
        verify(documentIndexingService, never()).rebuildIndex();
    }
    
    @Test
    public void checkThatDocumentExceptionWhenReindexingIsPostedToEventBus() throws DocumentIndexingException {
        //Given
        DocumentIndexingException documentIndexingException = new DocumentIndexingException("Failed to check if index is empty");
        SolrIndexingService<GeminiDocument> documentIndexingService = mock(SolrIndexingService.class);
        when(documentIndexingService.isIndexEmpty()).thenThrow(documentIndexingException);
        
        //When
        services.performReindexIfNothingIsIndexed(documentIndexingService);
        
        //Then
        verify(bus).post(documentIndexingException);
    }
    
    @Test
    public void checkThatDocumentInfoMapperCanBeCreated() {
        //Given
        //Nothing
        
        //When
        DocumentInfoMapper docMapper = services.documentInfoMapper();
        
        //Then
        assertNotNull("Didn't expect the mapper to be null", docMapper);
    }
    
    @Test
    public void checkThatBundledReaderServiceIsComposedCorrectly() throws XPathExpressionException {
        //Given
        DocumentReadingService readingService = mock(DocumentReadingService.class);
        DocumentInfoMapper infoMapper = mock(DocumentInfoMapper.class);
        
        doReturn(readingService).when(services).documentReadingService();
        doReturn(infoMapper).when(services).documentInfoMapper();
        
        //When
        MetadataInfoBundledReaderService reader = services.bundledReaderService();
        
        //Then
        assertEquals("Expected to find the dataRepository", dataRepository, reader.getRepo());
        assertEquals("Expected to find the readingService", readingService, reader.getDocumentReader());
        assertEquals("Expected to find the infoMapper", infoMapper, reader.getDocumentInfoMapper());
    }
    
    @Test
    public void checkThatDocumentIndexingServiceIsComposedCorrectly() throws XPathExpressionException {
        //Given
        MetadataInfoBundledReaderService reader = mock(MetadataInfoBundledReaderService.class);
        ExtensionDocumentListingService listingService = mock(ExtensionDocumentListingService.class);
        
        doReturn(reader).when(services).bundledReaderService();
        doReturn(listingService).when(services).documentListingService();
        doNothing().when(services).performReindexIfNothingIsIndexed(any(SolrIndexingService.class));
        
        //When
        SolrIndexingService<MetadataDocument> documentIndexingService = services.documentIndexingService();
        
        //Then
        assertEquals("Expected to find the reader", reader, documentIndexingService.getReader());
        assertEquals("Expected to find the listingService", listingService, documentIndexingService.getListingService());
        assertEquals("Expected to find the dataRepository", dataRepository, documentIndexingService.getRepo());
        assertEquals("Expected to find the solrServer", solrServer, documentIndexingService.getSolrServer());
    }
    
    @Test
    public void checkThatDocumentLinkingServiceIsComposedCorrectly() throws XPathExpressionException {
        //Given
        MetadataInfoBundledReaderService reader = mock(MetadataInfoBundledReaderService.class);
        
        doReturn(reader).when(services).bundledReaderService();
        doNothing().when(services).performRelinkIfNothingIsLinked(any(DocumentLinkService.class));
        
        //When
        GitDocumentLinkService documentLinkingService = services.documentLinkingService();
        
        //Then
        assertEquals("Expected to find the reader", reader, documentLinkingService.getDocumentBundleReader());
        assertEquals("Expected to find the linking database", linkDatabase, documentLinkingService.getLinkDatabase());
        assertEquals("Expected to find the dataRepository", dataRepository, documentLinkingService.getRepo());
    }

    @Test
    public void checkThatLinkingServiceIsRequestedToBeLinkedAfterCreation() throws XPathExpressionException {
        //Given
        doNothing().when(services).performRelinkIfNothingIsLinked(any(DocumentLinkService.class));
        
        //When
        GitDocumentLinkService documentLinkingService = services.documentLinkingService();
        
        //Then
        verify(services).performRelinkIfNothingIsLinked(documentLinkingService);
    }
    
    @Test
    public void checkThatLinkingServiceIsReLinkedIfEmpty() throws DocumentLinkingException {
        //Given
        DocumentLinkService documentLinkingService = mock(DocumentLinkService.class);
        when(documentLinkingService.isEmpty()).thenReturn(true);
        
        //When
        services.performRelinkIfNothingIsLinked(documentLinkingService);
        
        //Then
        verify(documentLinkingService).rebuildLinks();
    }
    
    @Test
    public void checkThatLinkingServiceIsNotRelinkedIfPopulated() throws DocumentLinkingException {
        //Given
        DocumentLinkService documentLinkingService = mock(DocumentLinkService.class);
        when(documentLinkingService.isEmpty()).thenReturn(false);
        
        //When
        services.performRelinkIfNothingIsLinked(documentLinkingService);
        
        //Then
        verify(documentLinkingService, never()).rebuildLinks();
    }
    
    @Test
    public void checkThatLinkExceptionWhenRelinkingIsPostedToEventBus() throws DocumentLinkingException {
        //Given
        DocumentLinkingException documentLinkingException = new DocumentLinkingException("Failed to check if index is empty");
        DocumentLinkService documentLinkingService = mock(DocumentLinkService.class);
        when(documentLinkingService.isEmpty()).thenReturn(true);
        doThrow(documentLinkingException).when(documentLinkingService).rebuildLinks();
        
        //When
        services.performRelinkIfNothingIsLinked(documentLinkingService);
        
        //Then
        verify(bus).post(documentLinkingException);
    }
}
