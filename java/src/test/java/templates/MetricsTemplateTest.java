package templates;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DownloadUrlProperties;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.templateHelpers.DownloadOrderDetailsService;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * Renders {@code _metrics.ftlh} the way a record page does. The counts it shows are decorative, so a
 * metrics outage must cost the counter and not the page - this pins that, and pins that each count is
 * queried once per render rather than twice.
 */
class MetricsTemplateTest {
    private static final String WRAPPER = "test-metrics.ftlh";
    private static final String WRAPPER_SOURCE = """
        <#assign
            catalogue = catalogues.retrieve(metadata.catalogue)
            recordType = "dataset">
        <#include "html/dataResource/_metrics.ftlh">
        """;
    private static final String RECORD_ID = "db1ea1f3-4101-4a52-b2b3-582473f724ea";

    @SneakyThrows
    private String render(MetricsService metrics) {
        val strings = new StringTemplateLoader();
        strings.putTemplate(WRAPPER, WRAPPER_SOURCE);

        val configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
            strings, new FileTemplateLoader(new File("../templates"))
        }));
        configuration.setSharedVariable("catalogues", catalogues());
        configuration.setSharedVariable("metrics", metrics);
        configuration.setSharedVariable("downloadOrderDetails", downloadOrderDetails());

        val gemini = (GeminiDocument) new GeminiDocument()
            .setType("dataset")
            .setOnlineResources(List.of())
            .setId(RECORD_ID)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").build());

        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(WRAPPER), gemini);
    }

    private DownloadOrderDetailsService downloadOrderDetails() {
        val properties = mock(DownloadUrlProperties.class);
        when(properties.getRegexSupportingDocs()).thenReturn("https://data-package\\.ceh\\.ac\\.uk/sd/.*");
        when(properties.getRegexOrderManDownload()).thenReturn("http(s?)://catalogue\\.ceh\\.ac\\.uk/download\\?fileIdentifier=.*");
        when(properties.getRegexOrder()).thenReturn("https://order-eidc\\.ceh\\.ac\\.uk/resources/.{8}/order\\?*.*");
        return new DownloadOrderDetailsService(properties);
    }

    private CatalogueService catalogues() {
        val catalogues = mock(CatalogueService.class);
        when(catalogues.retrieve(anyString())).thenReturn(Catalogue.builder()
            .id("eidc")
            .title("Environmental Information Data Centre")
            .url("https://catalogue.ceh.ac.uk")
            .contactUrl("https://catalogue.ceh.ac.uk/contact")
            .logo("UKCEH_EIDC_black.png")
            .build());
        return catalogues;
    }

    @Test
    void showsTheCountsWhenMetricsAreAvailable() {
        //given
        val metrics = mock(MetricsService.class);
        when(metrics.totalViews(RECORD_ID)).thenReturn(42);
        when(metrics.totalDownloads(RECORD_ID)).thenReturn(7);

        //when
        val actual = render(metrics);

        //then
        assertThat(actual).contains("42").contains("views").contains("7").contains("downloads");
    }

    /**
     * The counts come back null when the metrics database cannot be read. Previously the underlying
     * exception propagated out of the template and 500'd the record page.
     */
    @Test
    void omitsTheUsageBlockWhenMetricsAreUnavailable() {
        //given the metrics database cannot be read, so both counts report as absent
        val metrics = mock(MetricsService.class);
        when(metrics.totalViews(RECORD_ID)).thenReturn(null);
        when(metrics.totalDownloads(RECORD_ID)).thenReturn(null);

        //when
        val actual = render(metrics);

        //then the page renders, simply without a usage section
        assertThat(actual).doesNotContain("segment-usage").doesNotContain("views").doesNotContain("downloads");
    }

    /**
     * The counts were previously read twice each - once to test for null, once to assign - doubling the
     * queries against a SQLite database on a network share for no benefit.
     */
    @Test
    void queriesEachCountOnlyOncePerRender() {
        //given
        val metrics = mock(MetricsService.class);
        when(metrics.totalViews(RECORD_ID)).thenReturn(42);
        when(metrics.totalDownloads(RECORD_ID)).thenReturn(7);

        //when
        render(metrics);

        //then
        verify(metrics, times(1)).totalViews(RECORD_ID);
        verify(metrics, times(1)).totalDownloads(RECORD_ID);
    }
}
