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
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DownloadUrlProperties;
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.templateHelpers.DownloadOrderDetailsService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Renders {@code _accessButtons.ftlh} the way a record page does, to guard the
 * {@code accessOption} macro's argument list. The macro takes six positional
 * parameters, so a call site that omits one shifts every later argument into the
 * wrong slot - and FreeMarker only reports that at render time, on live records.
 * <p>
 * {@code catalogue} and {@code recordType} reach the sub-template as namespace
 * variables assigned by {@code dataResource.ftlh}, which outrank the data model -
 * a shared variable would be shadowed by {@code MetadataDocument.getCatalogue()}.
 * The wrapper below reproduces those assigns so the include behaves as in production.
 */
class AccessButtonsTemplateTest {
    private static final String WRAPPER = "test-dataResource.ftlh";
    private static final String WRAPPER_SOURCE = """
        <#assign
            catalogue = catalogues.retrieve(metadata.catalogue)
            recordType = "dataset">
        <#include "html/dataResource/_accessButtons.ftlh">
        """;

    private static final String RECORD_ID = "db1ea1f3-4101-4a52-b2b3-582473f724ea";
    private static final String WMS_URL =
        "https://catalogue.ceh.ac.uk/maps/" + RECORD_ID + "?service=WMS&request=GetCapabilities";
    private static final String DOWNLOAD_URL =
        "https://catalogue.ceh.ac.uk/datastore/eidchub/" + RECORD_ID;
    private static final String FILE_ACCESS_URL =
        "https://catalogue.ceh.ac.uk/datastore/eidchub/" + RECORD_ID + "/";

    Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void init() {
        val strings = new StringTemplateLoader();
        strings.putTemplate(WRAPPER, WRAPPER_SOURCE);

        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
            strings, new FileTemplateLoader(new File("../templates"))
        }));
        configuration.setSharedVariable("downloadOrderDetails", downloadOrderDetails());
        configuration.setSharedVariable("jena", jena());
        configuration.setSharedVariable("catalogues", catalogues());
    }

    private DownloadOrderDetailsService downloadOrderDetails() {
        val properties = mock(DownloadUrlProperties.class);
        when(properties.getRegexSupportingDocs()).thenReturn("https://data-package\\.ceh\\.ac\\.uk/sd/.*");
        when(properties.getRegexOrderManDownload()).thenReturn("http(s?)://catalogue\\.ceh\\.ac\\.uk/download\\?fileIdentifier=.*");
        when(properties.getRegexOrder()).thenReturn("https://order-eidc\\.ceh\\.ac\\.uk/resources/.{8}/order\\?*.*");
        return new DownloadOrderDetailsService(properties);
    }

    private JenaLookupService jena() {
        val jena = mock(JenaLookupService.class);
        when(jena.latestVersion(anyString())).thenReturn(Collections.emptyList());
        return jena;
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

    @SneakyThrows
    private String render(GeminiDocument gemini) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(WRAPPER),
            gemini
        );
    }

    private GeminiDocument availableDataset(OnlineResource... onlineResources) {
        return (GeminiDocument) new GeminiDocument()
            .setType("dataset")
            .setOnlineResources(List.of(onlineResources))
            .setAccessLimitation(AccessLimitation.builder().availability("Available").build())
            .setId(RECORD_ID)
            .setUri("https://catalogue.ceh.ac.uk/id/" + RECORD_ID)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").build());
    }

    private OnlineResource wms() {
        return OnlineResource.builder().url(WMS_URL).name("WMS").function("search").build();
    }

    private OnlineResource download() {
        return OnlineResource.builder().url(DOWNLOAD_URL).name("Zip package").function("download").build();
    }

    private OnlineResource fileAccess() {
        return OnlineResource.builder().url(FILE_ACCESS_URL).name("Files").function("fileAccess").build();
    }

    /**
     * A WMS GetCapabilities online resource makes the record map viewable, which
     * reaches {@code wmsButton} -> {@code accessOption}. Passing too few positional
     * arguments there put a string in the boolean {@code newWindow} slot and threw
     * {@code NonBooleanException}, 500ing every map viewable record page.
     */
    @Test
    void wmsAccessOptionRendersWithoutThrowing() {
        //given
        val gemini = availableDataset(wms());

        //when
        val actual = render(gemini);

        //then
        assertThat(actual).contains("Web map service");
    }

    @Test
    void wmsAccessOptionOpensInNewWindowAndIsLabelledAccess() {
        //given
        val gemini = availableDataset(wms());

        //when
        val actual = render(gemini);

        //then
        assertThat(actual)
            .contains("target=\"_blank\"")
            .contains(">Access</span>")
            .doesNotContain(">Download</span>");
    }

    /**
     * A download-only EIDC distribution never assigns {@code optionIcon}, so the
     * positional call at line 168 passed an undefined variable.
     */
    @Test
    void downloadOnlyDatasetRendersWithoutThrowing() {
        //given
        val gemini = availableDataset(download());

        //when
        val actual = render(gemini);

        //then
        assertThat(actual)
            .contains("Download the whole dataset")
            .contains(">Download</span>")
            .doesNotContain("target=\"_blank\"");
    }

    /**
     * A nameless external resource must not inherit the label of an earlier resource -
     * it should fall back to the {@code accessOption} default.
     */
    @Test
    void namelessResourceDoesNotInheritAnEarlierResourceLabel() {
        //given
        // both external, so neither label is replaced by the eidcDistribution wording
        val gemini = availableDataset(
            OnlineResource.builder().url("https://example.org/first.csv").name("First distribution").function("download").build(),
            OnlineResource.builder().url("https://example.org/second.csv").function("download").build()
        );

        //when
        val actual = render(gemini);

        //then
        assertThat(actual)
            .containsOnlyOnce("First distribution")
            .contains("Order/download");
    }

    /**
     * {@code optionIcon} is assigned at namespace scope inside the {@code #list}, so
     * a value set for an earlier resource leaks into later iterations. fileAccess is
     * ordered before download in {@code dataAccessResources}, so the download option
     * must not inherit the folder icon.
     */
    @Test
    void downloadOptionKeepsItsOwnIconWhenPrecededByFileAccess() {
        //given
        val gemini = availableDataset(fileAccess(), download());

        //when
        val actual = render(gemini);

        //then
        assertThat(actual)
            .contains("Directly access files")
            .contains("Download the whole dataset")
            .contains("fa-folder-open")
            .contains("fa-download");
    }
}
