package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import org.apache.jena.query.Dataset;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.solr.client.solrj.SolrServer;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
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
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.JenaIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
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
    @Mock Dataset jenaTdb;
    
    private ServiceConfig services;
    
    @Before
    public void createServiceConfig() {
        MockitoAnnotations.initMocks(this);
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.baseUri = "https://example.com/";
        services = spy(serviceConfig);
        services.jacksonMapper = jacksonMapper;
        services.dataRepository = dataRepository;
        services.solrServer = solrServer;
        services.bus = bus;
        services.jenaTdb = jenaTdb;
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
    public void checkThatIndexingServiceIsRequestedToBeIndexedAfterCreation() throws XPathExpressionException, IOException, TemplateModelException {
        //Given
        freemarker.template.Configuration freemarkerConfiguration = mock(freemarker.template.Configuration.class);
        
        doNothing().when(services).performReindexIfNothingIsIndexed(any(SolrIndexingService.class));
        doReturn(freemarkerConfiguration).when(services).freemarkerConfiguration();
        
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
        DocumentIndexingService documentIndexingService = mock(DocumentIndexingService.class);
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
    public void checkThatBundledReaderServiceIsComposedCorrectly() throws XPathExpressionException, IOException, TemplateModelException {
        //Given
        DocumentReadingService readingService = mock(DocumentReadingService.class);
        DocumentInfoMapper infoMapper = mock(DocumentInfoMapper.class);
        Configuration freemarkerConfiguration = mock(Configuration.class);
        
        doReturn(readingService).when(services).documentReadingService();
        doReturn(infoMapper).when(services).documentInfoMapper();
        doReturn(freemarkerConfiguration).when(services).freemarkerConfiguration();
        
        //When
        MetadataInfoBundledReaderService reader = services.bundledReaderService();
        
        //Then
        assertThat("BundledReaderService should not be null", reader, is(not(nullValue())));
    }
    
    @Test
    public void checkThatLinkingServiceIsRequestedToBeLinkedAfterCreation() throws XPathExpressionException, IOException, TemplateModelException {
        //Given
        Configuration freemarkerConfiguration = mock(Configuration.class);
        
        doReturn(freemarkerConfiguration).when(services).freemarkerConfiguration();
        doNothing().when(services).performReindexIfNothingIsIndexed(any(DocumentIndexingService.class));
        
        //When
        JenaIndexingService documentLinkingService = services.documentLinkingService();
        
        //Then
        verify(services).performReindexIfNothingIsIndexed(documentLinkingService);
    }
}
