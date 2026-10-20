package uk.ac.ceh.gateway.catalogue.wms;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ceh.gateway.catalogue.document.CatalogueRecordIds;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource.Attribute;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource.Attribute.Value;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.Style;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.Service;
import uk.ac.ceh.gateway.catalogue.gemini.Service.CoupledResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.PermissionDeniedException;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private final CatalogueRecordIds recordIds =
        new CatalogueRecordIds("http://localhost:8080", "https://catalogue.ceh.ac.uk");
    private WmsMapService service;
    private final Logger logger = (Logger) LoggerFactory.getLogger(WmsMapService.class);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private Level loggerLevel;

    @BeforeEach
    void setUp() {
        loggerLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        logs.start();
        logger.addAppender(logs);
        SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken(user, "n/a"));
        service = new WmsMapService(
            reader,
            permissionService,
            new MapServerDetailsService("http://localhost:8080", "http://mapserver/{id}", recordIds),
            recordIds
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        logger.detachAppender(logs);
        logger.setLevel(loggerLevel);
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

    // A raster source whose attribute LC.A has one class, as the mapfile template would build it
    private static MapDataDefinition definition() {
        Style style = new Style();
        style.setColour("#ff0000");
        Value value = new Value();
        value.setLabel("Woodland");
        value.setStyle(style);
        Attribute attribute = new Attribute();
        attribute.setName("LC.A");
        attribute.setValues(List.of(value));
        DataSource source = new DataSource();
        source.setType("RASTER");
        source.setAttributes(List.of(attribute));
        MapDataDefinition definition = new MapDataDefinition();
        definition.setData(List.of(source));
        return definition;
    }

    private static final LegendEntry WOODLAND = new LegendEntry("Woodland", "#ff0000", "area");

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
    void serviceRecordGetsLegendsFromItsOwnStoredDefinition() throws Exception {
        GeminiDocument stored = serviceRecord("svc1", CAPABILITIES);
        stored.setMapDataDefinition(definition());
        when(reader.readBundle("svc1")).thenReturn(stored);

        List<WmsMap> maps = service.forRecord(serviceRecord("svc1", CAPABILITIES));

        assertThat(maps.get(0).getLegends().get("LC.A"), contains(WOODLAND));
    }

    @Test
    void serviceRecordWithTwoLocalWmsReadsItsDefinitionOnce() throws Exception {
        GeminiDocument document = serviceRecord("svc1", CAPABILITIES);
        document.setOnlineResources(List.of(
            wms(CAPABILITIES),
            wms("https://catalogue.ceh.ac.uk/maps/def?request=GetCapabilities&service=WMS")
        ));

        service.forRecord(document);

        verify(reader, times(1)).readBundle("svc1");
    }

    @Test
    void serviceRecordWithoutLocalWmsReadsNothing() throws Exception {
        service.forRecord(serviceRecord("svc1", "https://example.com/maps/abc?request=GetCapabilities&service=WMS"));

        verify(reader, never()).readBundle(anyString());
    }

    @Test
    void serviceRecordStillGetsItsMapWhenTheDefinitionCannotBeRead() throws Exception {
        when(reader.readBundle("svc1")).thenThrow(new IllegalStateException("datastore down"));

        List<WmsMap> maps = service.forRecord(serviceRecord("svc1", CAPABILITIES));

        assertThat(maps, hasSize(1));
        assertThat(maps.get(0).getLegends(), is(anEmptyMap()));
    }

    @Test
    void datasetGetsLegendsFromTheLinkedService() throws Exception {
        GeminiDocument svc = serviceRecord("svc1", CAPABILITIES);
        svc.setMapDataDefinition(definition());
        servicePermitted(svc);

        List<WmsMap> maps = service.forRecord(dataset());

        assertThat(maps.get(0).getLegends().get("LC.A"), contains(WOODLAND));
        verify(reader, times(1)).readBundle("svc1");
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

    @Test
    void serviceRecordWithoutTitleIsTitledByItsId() {
        GeminiDocument document = serviceRecord("svc1", CAPABILITIES);
        document.setTitle(null);

        assertThat(service.forRecord(document).get(0).getTitle(), is("svc1"));
    }

    @Test
    void linkedServiceWithoutTitleIsTitledByItsId() throws Exception {
        GeminiDocument svc = serviceRecord("svc1", CAPABILITIES);
        svc.setTitle(null);
        servicePermitted(svc);

        assertThat(service.forRecord(dataset()).get(0).getTitle(), is("svc1"));
    }

    @Test
    void linkedServiceWithBlankTitleIsTitledByItsId() throws Exception {
        GeminiDocument svc = serviceRecord("svc1", CAPABILITIES);
        svc.setTitle("  ");
        servicePermitted(svc);

        assertThat(service.forRecord(dataset()).get(0).getTitle(), is("svc1"));
    }

    @Test
    void deletedServiceIsSkippedBeforeAnyCheckOrRead() {
        GeminiDocument document = dataset();
        document.setRelIsRequiredBy(List.of(
            Link.builder().href("https://catalogue.ceh.ac.uk/id/svc1").availability("deleted").build()
        ));

        assertThat(service.forRecord(document), is(empty()));
        verifyNoInteractions(reader, permissionService);
    }

    @Test
    void forbiddenReadIsLoggedAtDebug() throws Exception {
        when(permissionService.toAccess(user, "svc1", "VIEW")).thenReturn(true);
        when(reader.readBundle("svc1")).thenThrow(new PermissionDeniedException("no"));

        assertThat(service.forRecord(dataset()), is(empty()));
        assertThat(logs.list, hasSize(1));
        assertThat(logs.list.get(0).getLevel(), is(Level.DEBUG));
    }

    @Test
    void unreadableServiceIsLoggedAtWarnWithoutStackTrace() throws Exception {
        when(permissionService.toAccess(user, "svc1", "VIEW")).thenReturn(true);
        when(reader.readBundle("svc1")).thenThrow(new IllegalStateException("gone"));

        assertThat(service.forRecord(dataset()), is(empty()));
        assertThat(logs.list, hasSize(1));
        ILoggingEvent event = logs.list.get(0);
        assertThat(event.getLevel(), is(Level.WARN));
        assertThat(event.getThrowableProxy(), is(nullValue()));
        assertThat(event.getFormattedMessage(), is("Skipping WMS preview of service svc1: java.lang.IllegalStateException: gone"));
    }
}
