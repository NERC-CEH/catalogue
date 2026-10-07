package templates;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService;
import uk.ac.ceh.gateway.catalogue.quality.Results;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Renders the relationships section of the monitoring facility and network pages
 * ({@code _facilityRelationships.ftlh}, {@code _networkRelationships.ftlh}).
 * <p>
 * The "Used by" and "Data produced here" lists come from page-only getters
 * ({@code getUtilisingProgrammes}, {@code getProducedDataResources}), so a template that names
 * either one wrongly throws {@code InvalidReferenceException} and 500s every page of that type -
 * which the getters' own tests cannot see (dri-one #404).
 * <p>
 * The includes expect {@code monitoringBase.ftlh} imported as {@code m}, as the pages import it.
 * Importing it runs its top-level metadata quality check, hence the stub shared variable.
 */
@DisplayName("Monitoring facility and network relationships section")
class MonitoringRelationshipsTemplateTest {
    private static final String UTILISES = "https://digital.ceh.ac.uk/ontology/doo/utilises";

    private Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void init() {
        val strings = new StringTemplateLoader();
        for (val include : List.of("_facilityRelationships.ftlh", "_networkRelationships.ftlh")) {
            strings.putTemplate("test-" + include, """
                <#import "html/monitoring/monitoringBase.ftlh" as m>
                <#include "html/monitoring/%s">
                """.formatted(include));
        }

        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
            strings, new FileTemplateLoader(new File("../templates"))
        }));
        MetadataQualityService noProblems = id -> new Results(List.of(), id);
        configuration.setSharedVariable("metadataQuality", noProblems);
    }

    @SneakyThrows
    private String render(String include, MetadataDocument document) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate("test-" + include), document
        );
    }

    /** A Jena lookup in which a programme, a dataset and a draft dataset all utilise {@code uri}. */
    private static JenaLookupService utilisedBy(String uri) {
        val jena = mock(JenaLookupService.class);
        when(jena.inverseRelationships(uri, UTILISES)).thenReturn(List.of(
            link("https://example.com/id/programme", "COSMOS-UK programme", "monitoringProgramme", "published"),
            link("https://example.com/id/dataset", "Soil moisture data", "dataset", "published"),
            link("https://example.com/id/draft", "Unreleased soil data", "dataset", "draft")
        ));
        return jena;
    }

    private static Link link(String href, String title, String type, String status) {
        return Link.builder().href(href).title(title).associationType(type).publicationStatus(status).build();
    }

    /** The text from {@code heading} up to the next key-value heading, or the end. */
    private static String section(String html, String heading) {
        int start = html.indexOf(">" + heading + "</div>");
        assertThat(start).as("heading %s", heading).isNotNegative();
        int end = html.indexOf("class=\"key\"", start);
        return end < 0 ? html.substring(start) : html.substring(start, end);
    }

    @Test
    @DisplayName("a facility lists programmes under Used by and datasets under Data produced here")
    void facility() {
        //given
        val facility = new MonitoringFacility();
        facility.setId("morley");
        facility.setUri("https://example.com/id/morley");
        facility.populateFromJenaService(utilisedBy(facility.getUri()));

        //when
        val actual = render("_facilityRelationships.ftlh", facility);

        //then
        assertThat(section(actual, "Used by"))
            .contains("COSMOS-UK programme")
            .doesNotContain("Soil moisture data");
        assertThat(section(actual, "Data produced here"))
            .contains("href=\"https://example.com/id/dataset\"")
            .doesNotContain("COSMOS-UK programme");
        assertThat(actual).doesNotContain("Unreleased soil data");
    }

    @Test
    @DisplayName("a network lists programmes under Used by and datasets under Data produced by this network")
    void network() {
        //given
        val network = new MonitoringNetwork();
        network.setId("cosmos-uk");
        network.setUri("https://example.com/id/cosmos-uk");
        network.populateFromJenaService(utilisedBy(network.getUri()));

        //when
        val actual = render("_networkRelationships.ftlh", network);

        //then
        assertThat(section(actual, "Used by"))
            .contains("COSMOS-UK programme")
            .doesNotContain("Soil moisture data");
        assertThat(section(actual, "Data produced by this network"))
            .contains("href=\"https://example.com/id/dataset\"")
            .doesNotContain("COSMOS-UK programme");
        assertThat(actual).doesNotContain("Unreleased soil data");
    }

    @Test
    @DisplayName("renders with no relationships at all")
    void empty() {
        //given
        val facility = new MonitoringFacility();
        facility.setId("empty");
        val network = new MonitoringNetwork();
        network.setId("empty");

        //when / then
        assertThat(render("_facilityRelationships.ftlh", facility)).doesNotContain("Data produced");
        assertThat(render("_networkRelationships.ftlh", network)).doesNotContain("Data produced");
    }
}
