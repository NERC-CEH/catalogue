package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.NoSuchOnlineResourceException;
import uk.ac.ceh.gateway.catalogue.model.NotAGetCapabilitiesResourceException;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class OnlineResourceControllerTest {
    @Mock DataRepository<CatalogueUser> repo;
    @Mock CloseableHttpClient httpClient;
    @Mock BundledReaderService<GeminiDocument> documentBundleReader;
    @Mock RestTemplate rest;
    
    private OnlineResourceController controller;
    
    @Before
    public void createOnlineController() {
        MockitoAnnotations.initMocks(this);
        
        controller = spy(new OnlineResourceController(repo, httpClient, documentBundleReader, rest));
    }
    
    @Test(expected=NotAGetCapabilitiesResourceException.class)
    public void checkThatGetWMSCapabilitiesDoesntWorkWithIncorrectOnlineResource() {
        //Given
        OnlineResource resource = new OnlineResource(
                "http://not.a.wms", "nothing", "nothing");
        
        //When
        controller.getWmsCapabilities(resource);
        
        //Then
        fail("Expected to fail with invalid capabilites exception");
    }
    
    @Test
    public void checkThatRestTemplateIsCalledWithValidWMSCapabilitesType() {
        //Given
        OnlineResource resource = new OnlineResource(
                "https://www.google.com/wms?REQUEST=GetCapabilities", "test wms", "test wms");
        
        WmsCapabilities wmsCaps = mock(WmsCapabilities.class);
        
        when(rest.getForObject(eq("https://www.google.com/wms?REQUEST=GetCapabilities"), eq(WmsCapabilities.class))).thenReturn(wmsCaps);
        
        //When
        WmsCapabilities result = controller.getWmsCapabilities(resource);
        
        //Then
        assertThat("Expected my wms mock to come out", result, equalTo(wmsCaps));
    }
    
    @Test
    public void checkThatCanGetOnlineResourceWhichExists() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        List<OnlineResource> resources = Arrays.asList(new OnlineResource("a","b","c"));
        when(document.getOnlineResources()).thenReturn(resources);
        
        //When
        OnlineResource resource = controller.getOnlineResource(document, 0);
        
        //Then
        assertThat("the online resource url is a", resource.getUrl(), equalTo("a"));
    }
    
    @Test(expected=NoSuchOnlineResourceException.class)
    public void checkThatFailsWithExceptionIfResourceIsRequestedWhichIsNotPresent() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getOnlineResources()).thenReturn(Collections.EMPTY_LIST);
        
        //When
        OnlineResource resource = controller.getOnlineResource(document, 0);
        
        //Then
        fail("Expected to fail with execption");
    }
    
    @Test(expected=NoSuchOnlineResourceException.class)
    public void checkThatFailsWithExceptionIfResourceIsRequestedWhichIsNegative() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getOnlineResources()).thenReturn(Collections.EMPTY_LIST);
        
        //When
        OnlineResource resource = controller.getOnlineResource(document, -10);
        
        //Then
        fail("Expected to fail with execption");
    }
    
    @Test
    public void checkThatGettingOnlineResourceDelegatesToDocumentReader() throws IOException, UnknownContentTypeException {
        //Given
        String file = "bob";
        String revision = "bob";
        int index = 10;
        
        OnlineResource resource = new OnlineResource("a", "b", "c");
        doReturn(resource).when(controller).getOnlineResource(any(GeminiDocument.class), anyInt());
        
        //When
        controller.getOnlineResource(file, revision, index);
        
        //Then
        verify(documentBundleReader).readBundle(file, revision);
    }
    
    @Test
    public void checkThatProcessOrRedirectToOnlineResourceDelegates() throws DataRepositoryException, IOException, UnknownContentTypeException {
        //Given
        String file = "my filename";
        int index = 1;
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn("12");
        when(repo.getLatestRevision()).thenReturn(revision);
        
        doReturn(null).when(controller).processOrRedirectToOnlineResource("12", file, index);
        
        //When
        controller.processOrRedirectToOnlineResource(file, index);
        
        //Then
        verify(controller).processOrRedirectToOnlineResource("12", file, index);
    }
    
    @Test
    public void checkThatProxyDirectServiceRequestDelegates() throws DataRepositoryException, IOException, UnknownContentTypeException {
        //Given
        String file = "my filename", type="wms";
        int index = 1;
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn("12");
        when(repo.getLatestRevision()).thenReturn(revision);
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        doNothing().when(controller).proxyDirectServiceRequest("12", file, index, type, request, response);
        
        //When
        controller.proxyDirectServiceRequest(file, index, type, request, response);
        
        //Then
        verify(controller).proxyDirectServiceRequest("12", file, index, type, request, response);
    }
}
