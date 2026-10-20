package templates;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.config.DownloadUrlProperties;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.templateHelpers.DataPreviewOffer;
import uk.ac.ceh.gateway.catalogue.templateHelpers.DownloadOrderDetailsService;
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.wms.LegendEntry;
import uk.ac.ceh.gateway.catalogue.wms.WmsMap;
import uk.ac.ceh.gateway.catalogue.wms.WmsMapService;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Renders the record-page Map section. {@code wmsMapList} is assigned once by
 * {@code _typeDataset.ftlh} so the table of contents and the section share one lookup;
 * the wrapper reproduces that assign.
 */
class MapPreviewTemplateTest {
    private static final String SECTION = "test-section.ftlh";
    private static final String TOC = "test-toc.ftlh";
    private static final String CAPABILITIES =
        "https://catalogue.ceh.ac.uk/maps/abc?service=WMS&request=GetCapabilities";

    private static final Link LINK = Link.builder().href("https://catalogue.ceh.ac.uk/id/svc1").build();

    Configuration configuration;
    WmsMapService wmsMaps;
    DataPreviewOffer dataPreview;

    private DownloadOrderDetailsService downloadOrderDetails() {
        val properties = mock(DownloadUrlProperties.class);
        when(properties.getRegexSupportingDocs()).thenReturn("https://data-package\\.ceh\\.ac\\.uk/sd/.*");
        when(properties.getRegexOrderManDownload()).thenReturn("http(s?)://catalogue\\.ceh\\.ac\\.uk/download\\?fileIdentifier=.*");
        when(properties.getRegexOrder()).thenReturn("https://order-eidc\\.ceh\\.ac\\.uk/resources/.{8}/order\\?*.*");
        return new DownloadOrderDetailsService(properties);
    }

    @SneakyThrows
    @BeforeEach
    void init() {
        val strings = new StringTemplateLoader();
        strings.putTemplate(SECTION, """
            <#assign wmsMapList = wmsMaps.forRecord(id, title, resourceType.value, onlineResources![], relIsRequiredBy![])>
            <#assign dataPreviewOffered = dataPreview.offered(id)>
            <#include "html/dataResource/_mapPreview.ftlh">
            """);
        strings.putTemplate(TOC, """
            <#assign wmsMapList = wmsMaps.forRecord(id, title, resourceType.value, onlineResources![], relIsRequiredBy![])>
            <#assign dataPreviewOffered = dataPreview.offered(id)>
            <#include "html/dataResource/_toc.ftlh">
            """);
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
            strings, new FileTemplateLoader(new File("../templates"))
        }));
        wmsMaps = mock(WmsMapService.class);
        configuration.setSharedVariable("wmsMaps", wmsMaps);
        configuration.setSharedVariable("permission", mock(PermissionService.class));
        dataPreview = mock(DataPreviewOffer.class);
        configuration.setSharedVariable("dataPreview", dataPreview);
        configuration.setSharedVariable("downloadOrderDetails", downloadOrderDetails());
    }

    @SneakyThrows
    private String render(String template, GeminiDocument gemini) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(template), gemini);
    }

    private GeminiDocument document() {
        GeminiDocument gemini = (GeminiDocument) new GeminiDocument().setType("dataset").setId("rec-1");
        gemini.setTitle("A dataset");
        gemini.setRelIsRequiredBy(List.of(LINK));
        return gemini;
    }

    private WmsMap map(String... layers) {
        return new WmsMap("abc", "Land cover", CAPABILITIES,
            "https://catalogue.ceh.ac.uk/maps/abc", "/maps/abc", List.of(layers), Map.of());
    }

    private void stub(WmsMap... maps) {
        when(wmsMaps.forRecord(any(), any(), any(), any(), any())).thenReturn(List.of(maps));
    }

    @Test
    void noMapsRendersNoSection() {
        stub();

        assertThat(render(SECTION, document()), not(containsString("segment-map")));
    }

    @Test
    void rendersServicePayload() {
        stub(map("LC.A"));

        val actual = render(SECTION, document());

        assertThat(actual, containsString("id=\"segment-map\""));
        assertThat(actual, containsString("<h2>Data Preview</h2>"));
        assertThat(actual, containsString("data-local-path=\"/maps/abc\""));
        assertThat(actual, containsString("data-public-url=\"https://catalogue.ceh.ac.uk/maps/abc\""));
        assertThat(actual, containsString(
            "data-capabilities-url=\"https://catalogue.ceh.ac.uk/maps/abc?service=WMS&amp;request=GetCapabilities\""));
        assertThat(actual, containsString("<span class=\"wms-layer-name\">LC.A</span>"));
        assertThat(actual, containsString("wms-map-unavailable d-none"));
    }

    @Test
    void rendersOneExtentPerBoundingBox() {
        stub(map());
        val gemini = document();
        gemini.setBoundingBoxes(List.of(
            BoundingBox.builder().westBoundLongitude("-8.5").eastBoundLongitude("1.5")
                .southBoundLatitude("49.9").northBoundLatitude("60.8").build()));

        val actual = render(SECTION, gemini);

        assertThat(actual, containsString(
            "<span class=\"wms-extent\" data-west=\"-8.5\" data-east=\"1.5\" data-south=\"49.9\" data-north=\"60.8\"></span>"));
    }

    @Test
    void gisHelpShowsCapabilitiesUrlWithoutJs() {
        stub(map("LC.A"));

        val actual = render(SECTION, document());

        assertThat(actual, containsString(
            "<details class=\"wms-gis-help\">\n        <summary><h3 class=\"h5\">Use this map in your GIS</h3></summary>"));
        assertThat(actual, containsString("wms-capabilities-url"));
        assertThat(actual, containsString("wms-getmap-url"));
        assertThat(actual, containsString(
            "value=\"https://catalogue.ceh.ac.uk/maps/abc?service=WMS&amp;request=GetCapabilities\""));
    }

    @Test
    void escapesAwkwardLayerNames() {
        stub(map("a&b \"c\""));

        val actual = render(SECTION, document());

        assertThat(actual, containsString("<span class=\"wms-layer-name\">a&amp;b &quot;c&quot;</span>"));
    }

    @Test
    void tocLinksToMapOnlyWhenThereIsOne() {
        stub(map());
        assertThat(render(TOC, document()), containsString("href=\"#segment-map\">Data Preview</a>"));

        stub();
        assertThat(render(TOC, document()), not(containsString("#segment-map")));
    }

    @Test
    void lookupPassesThePageValuesOnce() {
        stub(map());

        render(SECTION, document());

        verify(wmsMaps, times(1)).forRecord("rec-1", "A dataset", "dataset", List.of(), List.of(LINK));
    }

    @Test
    void mapWithoutTitleStillRenders() {
        stub(new WmsMap("abc", null, CAPABILITIES,
            "https://catalogue.ceh.ac.uk/maps/abc", "/maps/abc", List.of(), Map.of()));

        assertThat(render(SECTION, document()), containsString("data-title=\"\""));
    }

    @Test
    void rendersLegendEntriesByLayerEscaped() {
        stub(new WmsMap("abc", "Land cover", CAPABILITIES, "https://catalogue.ceh.ac.uk/maps/abc", "/maps/abc", List.of(),
            Map.of("LC.A", List.of(new LegendEntry("Broadleaved & <yew>", "#ff0000", "area")))));

        val actual = render(SECTION, document());

        assertThat(actual, containsString("<span class=\"wms-legend-layer\" data-layer=\"LC.A\">"));
        assertThat(actual, containsString(
            "<span class=\"wms-legend-class\" data-colour=\"#ff0000\" data-shape=\"area\">Broadleaved &amp; &lt;yew&gt;</span>"));
    }

    @Test
    void legendEntryWithoutColourRendersBlankColour() {
        stub(new WmsMap("abc", "Land cover", CAPABILITIES, "https://catalogue.ceh.ac.uk/maps/abc", "/maps/abc", List.of(),
            Map.of("LC.A", List.of(new LegendEntry("Unstyled", null, "line")))));

        assertThat(render(SECTION, document()), containsString("data-colour=\"\" data-shape=\"line\">Unstyled</span>"));
    }

    private GeminiDocument previewDocument() {
        val gemini = document();
        gemini.setId("abc");
        gemini.setOnlineResources(List.of(
            OnlineResource.builder().url("https://data-package.ceh.ac.uk/data/abc?a=1&b=2").function("download").build(),
            OnlineResource.builder().url("https://data-package.ceh.ac.uk/sd/abc.zip").function("information").build()));
        return gemini;
    }

    @Test
    void sectionRendersForDataPreviewWithoutMaps() {
        stub();
        when(dataPreview.offered("abc")).thenReturn(true);

        val actual = render(SECTION, previewDocument());

        assertThat(actual, containsString("id=\"segment-map\""));
        assertThat(actual, containsString("Data Preview"));
        assertThat(actual, containsString("class=\"data-preview\""));
        assertThat(actual, containsString("data-record-id=\"abc\""));
        assertThat(actual, containsString(
            "data-supporting-documents-url=\"https://data-package.ceh.ac.uk/sd/abc.zip\""));
        assertThat(actual, containsString("data-preview-data"));
        assertThat(actual, containsString("data-preview-documents"));
        assertThat(actual, not(containsString("wms-map-preview")));
    }

    @Test
    void sectionKeepsMapAndAddsDataPreview() {
        stub(map("LC.A"));
        when(dataPreview.offered("abc")).thenReturn(true);

        val actual = render(SECTION, previewDocument());

        assertThat(actual, containsString("wms-map-preview"));
        assertThat(actual, containsString("class=\"data-preview\""));
    }

    @Test
    void tocLinksDataPreviewWithoutMaps() {
        stub();
        when(dataPreview.offered("abc")).thenReturn(true);

        assertThat(render(TOC, previewDocument()), containsString("href=\"#segment-map\">Data Preview</a>"));
    }

    @Test
    void noSectionWhenNeitherOffered() {
        stub();
        when(dataPreview.offered("abc")).thenReturn(false);

        assertThat(render(SECTION, previewDocument()), not(containsString("segment-map")));
        assertThat(render(TOC, previewDocument()), not(containsString("#segment-map")));
    }

    @Test
    void packageUrlGoesThroughDownloadEndpoint() {
        stub();
        when(dataPreview.offered("abc")).thenReturn(true);

        assertThat(render(SECTION, previewDocument()), containsString(
            "data-package-url=\"/download/abc?url=https://data-package.ceh.ac.uk/data/abc?a=1&amp;b=2\""));
    }
}
