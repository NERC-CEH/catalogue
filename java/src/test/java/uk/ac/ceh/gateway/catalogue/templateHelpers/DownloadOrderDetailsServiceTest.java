package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.config.DownloadUrlProperties;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.templateHelpers.DownloadOrderDetailsService.DownloadOrder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DownloadOrderDetailsServiceTest {
    private DownloadOrderDetailsService service;

    @BeforeEach
    public void init() {
        DownloadUrlProperties downloadUrlProperties = mock(DownloadUrlProperties.class);
        when(downloadUrlProperties.getRegexOrder()).thenReturn("https://order-eidc\\.ceh\\.ac\\.uk/resources/.{8}/order\\?*.*");
        when(downloadUrlProperties.getRegexPackage()).thenReturn("https://data-package\\.ceh\\.ac\\.uk/.*");
        when(downloadUrlProperties.getRegexDatastore()).thenReturn("https://catalogue\\.ceh\\.ac\\.uk/datastore/eidchub/.*");
        when(downloadUrlProperties.getRegexSupportingDocs()).thenReturn("https://data-package\\.ceh\\.ac\\.uk/sd/.*");
        when(downloadUrlProperties.getRegexOrderManDownload()).thenReturn("http(s?)://catalogue\\.ceh\\.ac\\.uk/download\\?fileIdentifier=.*");
        service = new DownloadOrderDetailsService(downloadUrlProperties);
    }

    @Test
    public void canDownloadOrderWithOrderableResourceOldOrderManager() {
        //Given
        String orderUrl = "https://catalogue.ceh.ac.uk/download?fileIdentifier=downloadMe";
        String orderMessage = "Message";
        OnlineResource onlineResource = OnlineResource.builder()
                .function("order").url(orderUrl).description(orderMessage).build();
        List<OnlineResource> onlineResources = Collections.singletonList(onlineResource);

        //When
        DownloadOrder order = service.from(onlineResources);

        //Then
        assertThat(order.isOrderable(), is(true));
        assertThat(order.getOrderResources().contains(onlineResource), is(true));
    }

    @Test
    public void canDownloadOrderWithOrderableResourceNewOrderManager() {
        //Given
        String orderUrl = "https://order-eidc.ceh.ac.uk/resources/ABCDEFGH/order";
        String orderMessage = "Message";
        OnlineResource onlineResource = OnlineResource.builder()
                .function("order").url(orderUrl).description(orderMessage).build();
        List<OnlineResource> onlineResources = Collections.singletonList(onlineResource);

        //When
        DownloadOrder order = service.from(onlineResources);

        //Then
        assertThat(order.isOrderable(), is(true));
        assertThat(order.getOrderResources().contains(onlineResource), is(true));
    }

    @Test
    public void canDownloadOrderWithOrderableResourceNewOrderManagerWithQueryParams() {
        //Given
        String orderUrl = "https://order-eidc.ceh.ac.uk/resources/ABCDEFGH/order?test=true";
        String orderMessage = "Message";
        OnlineResource onlineResource = OnlineResource.builder()
                .function("order").url(orderUrl).description(orderMessage).build();
        List<OnlineResource> onlineResources = Collections.singletonList(onlineResource);

        //When
        DownloadOrder order = service.from(onlineResources);

        //Then
        assertThat(order.isOrderable(), is(true));
        assertThat(order.getOrderResources().contains(onlineResource), is(true));
    }

    @Test
    public void canDownloadOrderWithDownloadResource() {
        //Given
        String orderUrl = "https://distrubtion.server.com";
        String orderMessage = "Message";
        OnlineResource onlineResource = OnlineResource.builder()
                .function("download").url(orderUrl).description(orderMessage).build();
        List<OnlineResource> onlineResources = Collections.singletonList(onlineResource);

        //When
        DownloadOrder order = service.from(onlineResources);

        //Then
        assertThat(order.isOrderable(), is(true));
        assertThat(order.getOrderResources().contains(onlineResource), is(true));
    }

    @Test
    public void checkThatOrderableResourceTakesPrecedentOverNonOrderable() {
        //Given
        String orderUrl = "https://catalogue.ceh.ac.uk/download?fileIdentifier=downloadMe";
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
        assertThat(order.getOrderResources().contains(orderable), is(true));
    }

    @Test
    public void readNonOrderableMessage() {
        //Given
        String orderUrl = "SomewhereElse";
        String orderMessage = "Message";
        OnlineResource onlineResource = OnlineResource.builder()
                .function("offlineAccess").url(orderUrl).description(orderMessage).build();
        List<OnlineResource> onlineResources = Collections.singletonList(onlineResource);

        //When
        DownloadOrder order = service.from(onlineResources);

        //Then
        assertThat(order.isOrderable(), is(false));
        assertThat(order.getOrderResources().contains(onlineResource), is(true));
    }

    @Test
    public void canReadSupportingDocumentation() {
        //Given
        String orderUrl = "https://data-package.ceh.ac.uk/sd/docs";
        OnlineResource onlineResource = OnlineResource.builder()
                .function("information").url(orderUrl).build();
        List<OnlineResource> onlineResources = Collections.singletonList(onlineResource);

        //When
        DownloadOrder order = service.from(onlineResources);

        //Then
        assertThat(order.getSupportingDocumentsUrl(), is(equalTo(orderUrl)));
    }
}
