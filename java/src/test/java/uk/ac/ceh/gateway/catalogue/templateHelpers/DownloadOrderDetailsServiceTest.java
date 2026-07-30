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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
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
        when(downloadUrlProperties.getRegexCeda()).thenReturn("https://data\\.ceda\\.ac\\.uk/eidc/.*");
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
        assertThat(order.isDataAccessible(), is(true));
        assertThat(order.getDataAccessResources().contains(onlineResource), is(true));
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
        assertThat(order.isDataAccessible(), is(true));
        assertThat(order.getDataAccessResources().contains(onlineResource), is(true));
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
        assertThat(order.isDataAccessible(), is(true));
        assertThat(order.getDataAccessResources().contains(onlineResource), is(true));
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
        assertThat(order.isDataAccessible(), is(true));
        assertThat(order.getDataAccessResources().contains(onlineResource), is(true));
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
        assertThat(order.isDataAccessible(), is(true));
        assertThat(order.getDataAccessResources().contains(orderable), is(true));
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
        assertThat(order.isDataAccessible(), is(false));
        assertThat(order.getDataAccessResources().contains(onlineResource), is(true));
    }

    @Test
    public void offlineAccessIsIncludedAlongsideDownloadableResources() {
        //Given a record offering both a real download and an offline access message
        OnlineResource download = OnlineResource.builder()
                .function("download").url("https://data-package.ceh.ac.uk/data/ABCDEFGH.zip").build();
        OnlineResource offlineAccess = OnlineResource.builder()
                .function("offlineAccess").url("https://example.com/contact-us").build();
        List<OnlineResource> onlineResources = Arrays.asList(download, offlineAccess);

        //When
        DownloadOrder order = service.from(onlineResources);

        //Then both are offered, not just the download
        assertThat(order.isDataAccessible(), is(true));
        assertThat(order.getDataAccessResources(), hasItems(download, offlineAccess));
    }

    @Test
    public void offlineAccessAloneDoesNotMakeDataAccessible() {
        //Given an offline access message and an order that is not on the order manager
        OnlineResource offlineAccess = OnlineResource.builder()
                .function("offlineAccess").url("https://example.com/contact-us").build();
        OnlineResource notOrderable = OnlineResource.builder()
                .function("order").url("SomeOtherUrl").build();
        List<OnlineResource> onlineResources = Arrays.asList(offlineAccess, notOrderable);

        //When
        DownloadOrder order = service.from(onlineResources);

        //Then the offline message is still shown, but the data is not accessible
        assertThat(order.isDataAccessible(), is(false));
        assertThat(order.isDataAddressable(), is(false));
        assertThat(order.getDataAccessResources(), contains(offlineAccess));
    }

    @Test
    public void dataAccessResourcesAreOrderedFileAccessDownloadOrderThenOfflineAccess() {
        //Given one resource of each supported function
        OnlineResource fileAccess = OnlineResource.builder()
                .function("fileAccess").url("https://catalogue.ceh.ac.uk/datastore/eidchub/ABCDEFGH/").build();
        OnlineResource download = OnlineResource.builder()
                .function("download").url("https://data-package.ceh.ac.uk/data/ABCDEFGH.zip").build();
        OnlineResource order = OnlineResource.builder()
                .function("order").url("https://order-eidc.ceh.ac.uk/resources/ABCDEFGH/order").build();
        OnlineResource offlineAccess = OnlineResource.builder()
                .function("offlineAccess").url("https://example.com/contact-us").build();
        // deliberately supplied out of order
        List<OnlineResource> onlineResources = Arrays.asList(offlineAccess, order, download, fileAccess);

        //When
        DownloadOrder downloadOrder = service.from(onlineResources);

        //Then the access panel lists them in a stable, predictable order
        assertThat(downloadOrder.getDataAccessResources(),
            contains(fileAccess, download, order, offlineAccess));
        assertThat(downloadOrder.isDataAccessible(), is(true));
        assertThat(downloadOrder.isDataAddressable(), is(true));
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
