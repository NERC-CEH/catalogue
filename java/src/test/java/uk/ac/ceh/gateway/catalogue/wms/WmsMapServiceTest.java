package uk.ac.ceh.gateway.catalogue.wms;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.Service;
import uk.ac.ceh.gateway.catalogue.gemini.Service.CoupledResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WmsMapServiceTest {
    private static final String CAPABILITIES =
        "https://catalogue.ceh.ac.uk/maps/abc?request=GetCapabilities&service=WMS";

    @Mock private BundledReaderService<MetadataDocument> reader;
    @Mock private PermissionService permissionService;
    private final CatalogueUser user = new CatalogueUser("someone", "someone@example.com");
    private WmsMapService service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken(user, "n/a"));
        service = new WmsMapService(
            reader,
            permissionService,
            new MapServerDetailsService("http://localhost:8080", "http://mapserver/{id}"),
            "http://localhost:8080"
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static OnlineResource wms(String url) {
        return OnlineResource.builder().url(url).function("wms").build();
    }

    private static GeminiDocument serviceRecord(String id, String url, CoupledResource... coupled) {
        GeminiDocument document = new GeminiDocument();
        document.setId(id);
        document.setTitle("Service " + id);
        document.setType("service");
        document.setOnlineResources(List.of(wms(url)));
        document.setService(Service.builder().coupledResources(List.of(coupled)).build());
        return document;
    }

    private static CoupledResource coupled(String identifier, String layerName) {
        return CoupledResource.builder().identifier(identifier).layerName(layerName).build();
    }

    private static GeminiDocument dataset() {
        GeminiDocument document = new GeminiDocument();
        document.setId("ds1");
        document.setUri("https://catalogue.ceh.ac.uk/id/ds1");
        document.setType("dataset");
        document.setRelIsRequiredBy(List.of(
            Link.builder().href("https://catalogue.ceh.ac.uk/id/svc1").build()
        ));
        return document;
    }

    private void servicePermitted(GeminiDocument svc) throws Exception {
        when(permissionService.toAccess(user, "svc1", "VIEW")).thenReturn(true);
        when(reader.readBundle("svc1")).thenReturn(svc);
    }

    @Test
    void serviceRecordGetsAllLayers() {
        List<WmsMap> maps = service.forRecord(serviceRecord("svc1", CAPABILITIES));

        assertThat(maps, hasSize(1));
        WmsMap map = maps.get(0);
        assertThat(map.getServiceId(), is("svc1"));
        assertThat(map.getTitle(), is("Service svc1"));
        assertThat(map.getCapabilitiesUrl(), is(CAPABILITIES));
        assertThat(map.getPublicMapUrl(), is("https://catalogue.ceh.ac.uk/maps/abc"));
        assertThat(map.getLocalPath(), is("/maps/abc"));
        assertThat(map.getLayerNames(), is(empty()));
    }

    @Test
    void serviceRecordWithoutLocalWmsGetsNone() {
        assertThat(
            service.forRecord(serviceRecord("svc1", "https://example.com/maps/abc?request=GetCapabilities&service=WMS")),
            is(empty())
        );
    }

    @Test
    void serviceRecordWithTwoLocalWmsGetsTwoMaps() {
        GeminiDocument document = serviceRecord("svc1", CAPABILITIES);
        document.setOnlineResources(List.of(
            wms(CAPABILITIES),
            wms("https://catalogue.ceh.ac.uk/maps/def?request=GetCapabilities&service=WMS")
        ));

        assertThat(service.forRecord(document), hasSize(2));
    }

    @Test
    void datasetGetsLayersNamedForIt() throws Exception {
        servicePermitted(serviceRecord("svc1", CAPABILITIES,
            coupled("http://catalogue.ceh.ac.uk/id/ds1", "LC.A"),
            coupled("http://catalogue.ceh.ac.uk/id/other", "LC.B")));

        List<WmsMap> maps = service.forRecord(dataset());

        assertThat(maps, hasSize(1));
        assertThat(maps.get(0).getLayerNames(), contains("LC.A"));
        assertThat(maps.get(0).getTitle(), is("Service svc1"));
        assertThat(maps.get(0).getServiceId(), is("svc1"));
    }

    @Test
    void datasetFallsBackToAllLayersWhenNoneNamed() throws Exception {
        servicePermitted(serviceRecord("svc1", CAPABILITIES,
            coupled("https://catalogue.ceh.ac.uk/id/ds1", "  ")));

        assertThat(service.forRecord(dataset()).get(0).getLayerNames(), is(empty()));
    }

    @Test
    void placeholderLayerNameIsTreatedAsBlank() throws Exception {
        servicePermitted(serviceRecord("svc1", CAPABILITIES, coupled("ds1", "-")));

        assertThat(service.forRecord(dataset()).get(0).getLayerNames(), is(empty()));
    }

    @Test
    void coupledResourceMatchesBareId() throws Exception {
        servicePermitted(serviceRecord("svc1", CAPABILITIES, coupled("ds1", "LC.A")));

        assertThat(service.forRecord(dataset()).get(0).getLayerNames(), contains("LC.A"));
    }

    @Test
    void forbiddenServiceIsSkipped() throws Exception {
        when(permissionService.toAccess(user, "svc1", "VIEW")).thenReturn(false);

        assertThat(service.forRecord(dataset()), is(empty()));
        verify(reader, never()).readBundle(anyString());
    }

    @Test
    void unreadableServiceIsSkipped() throws Exception {
        when(permissionService.toAccess(user, "svc1", "VIEW")).thenReturn(true);
        when(reader.readBundle("svc1")).thenThrow(new IllegalStateException("gone"));

        assertThat(service.forRecord(dataset()), is(empty()));
    }

    @Test
    void anonymousViewerIsCheckedAsPublicUser() throws Exception {
        SecurityContextHolder.clearContext();
        when(permissionService.toAccess(CatalogueUser.PUBLIC_USER, "svc1", "VIEW")).thenReturn(false);

        assertThat(service.forRecord(dataset()), is(empty()));
        verify(reader, never()).readBundle(anyString());
    }

    @Test
    void datasetWithNoServicesDoesNoReads() {
        GeminiDocument document = dataset();
        document.setRelIsRequiredBy(List.of());

        assertThat(service.forRecord(document), is(empty()));
        verifyNoInteractions(reader, permissionService);
    }
}
