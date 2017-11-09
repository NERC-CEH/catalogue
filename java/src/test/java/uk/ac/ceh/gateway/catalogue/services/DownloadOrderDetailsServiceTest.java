package uk.ac.ceh.gateway.catalogue.services;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.services.DownloadOrderDetailsService.DownloadOrder;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author cjohn
 */
public class DownloadOrderDetailsServiceTest {
    private final String eidchub = "http:\\/\\/eidc\\.ceh\\.ac\\.uk\\/metadata.*";
    private final String orderMan = "http(s?):\\/\\/catalogue.ceh.ac.uk\\/download\\?fileIdentifier=.*";
    private DownloadOrderDetailsService service;
    
    @Before
    public void init() {
        service = new DownloadOrderDetailsService(eidchub, orderMan);
    }
    
    @Test
    public void canDownloadOrderWithOrderableResource() {
        //Given
        String orderUrl = "http://catalogue.ceh.ac.uk/download?fileIdentifier=downloadMe";
        String orderMessage = "Message";
        OnlineResource onlineResource = OnlineResource.builder()
                .function("order").url(orderUrl).description(orderMessage).build();
        List<OnlineResource> onlineResources = Arrays.asList(onlineResource);
        
        //When
        DownloadOrder order = service.from(onlineResources);
        
        //Then
        assertThat(order.isOrderable(), is(true));
        assertThat(order.getOrderResources(), contains(onlineResource));
    }
    
    @Test
    public void canDownloadOrderWithDownloadResource() {
        //Given
        String orderUrl = "http://distrubtion.server.com";
        String orderMessage = "Message";
        OnlineResource onlineResource = OnlineResource.builder()
                .function("download").url(orderUrl).description(orderMessage).build();
        List<OnlineResource> onlineResources = Arrays.asList(onlineResource);
        
        //When
        DownloadOrder order = service.from(onlineResources);
        
        //Then
        assertThat(order.isOrderable(), is(true));
        assertThat(order.getOrderResources(), contains(onlineResource));
    }
    
    @Test
    public void checkThatOrderableResourceTakesPresedentOverNonOrderable() {
        //Given
        String orderUrl = "http://catalogue.ceh.ac.uk/download?fileIdentifier=downloadMe";
        String orderMessage = "Message";
        OnlineResource orderable = OnlineResource.builder()
                .function("order").url(orderUrl).description(orderMessage).build();
        OnlineResource notOrderable = OnlineResource.builder()
                .function("order").url("SomeOtherUrl").description("notOrderable").build();
        List<OnlineResource> onlineResources = Arrays.asList(orderable, notOrderable);
        
        //When
        DownloadOrder order = service.from(onlineResources);
        
        //Then
        assertThat(order.isOrderable(), is(true));
        assertThat(order.getOrderResources(), contains(orderable));
    }
    
    @Test
    public void readNonOrderableMessage() {
        //Given
        String orderUrl = "SomewhereElse";
        String orderMessage = "Message";
        OnlineResource onlineResource = OnlineResource.builder()
                .function("order").url(orderUrl).description(orderMessage).build();
        List<OnlineResource> onlineResources = Arrays.asList(onlineResource);
        
        //When
        DownloadOrder order = service.from(onlineResources);
        
        //Then
        assertThat(order.isOrderable(), is(false));
        assertThat(order.getOrderResources(), contains(onlineResource));
    }
    
    @Test
    public void canReadSupportingDocumentation() {
        //Given
        String orderUrl = "http://eidc.ceh.ac.uk/metadata/docs";
        OnlineResource onlineResource = OnlineResource.builder()
                .function("information").url(orderUrl).build();
        List<OnlineResource> onlineResources = Arrays.asList(onlineResource);
        
        //When
        DownloadOrder order = service.from(onlineResources);
        
        //Then
        assertThat(order.getSupportingDocumentsUrl(), equalTo(orderUrl));
    }
}
