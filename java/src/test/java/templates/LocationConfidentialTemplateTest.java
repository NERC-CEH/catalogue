package templates;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.geometry.Geometry;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders {@code _locationConfidential.ftlh}, the banner shown on a monitoring
 * facility whose location is withheld.
 * <p>
 * The guard has to survive three states, and only one of them is the interesting
 * one: {@code geometry} itself is null. FreeMarker's {@code ??} and {@code !}
 * operators cover only the <em>last</em> step of an expression, so
 * {@code geometry.locationConfidential??} still throws
 * {@code InvalidReferenceException} when {@code geometry} is missing - the whole
 * chain needs parenthesising. A geometry-less facility is an ordinary record:
 * {@code MonitoringFacility.getWKTs()} null-guards the field, nothing validates
 * it on save, and {@code monitoringFacilityWrong.raw} is one.
 * <p>
 * The fragment is an include rather than inline markup in {@code facility.ftlh}
 * precisely so it can be rendered on its own - the surrounding {@code base} macro
 * would otherwise drag in the skeleton, metadata quality checks and the code
 * lookup service to test one conditional.
 */
@DisplayName("Monitoring facility location confidentiality banner")
class LocationConfidentialTemplateTest {

    private static final String BANNER = "The location of this facility is confidential";

    private static final String POINT =
        "{\"type\":\"Feature\",\"properties\":{},\"geometry\":"
            + "{\"type\":\"Point\",\"coordinates\":[-2.645,54.526]}}";

    private Configuration configuration;

    @BeforeEach
    void init() throws Exception {
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new FileTemplateLoader(new File("../templates")));
    }

    @SneakyThrows
    private String render(MonitoringFacility facility) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate("html/monitoring/_locationConfidential.ftlh"),
            facility
        );
    }

    private MonitoringFacility facilityWith(Geometry geometry) {
        val facility = new MonitoringFacility();
        facility.setGeometry(geometry);
        return facility;
    }

    /**
     * The regression. Before the guard was parenthesised this threw, 500ing the page
     * of every facility that has no geometry.
     */
    @Test
    @DisplayName("renders nothing for a facility that has no geometry at all")
    void noGeometry() {
        //given
        val facility = facilityWith(null);

        //when
        val actual = render(facility);

        //then
        assertThat(actual).doesNotContain(BANNER);
    }

    @Test
    @DisplayName("renders nothing when the geometry carries no flag")
    void geometryWithoutFlag() {
        //given
        val facility = facilityWith(Geometry.builder().geometryString(POINT).build());

        //when
        val actual = render(facility);

        //then
        assertThat(actual).doesNotContain(BANNER);
    }

    @Test
    @DisplayName("renders nothing when the flag is explicitly false")
    void geometryWithFalseFlag() {
        //given
        val facility = facilityWith(
            Geometry.builder().geometryString(POINT).locationConfidential(false).build()
        );

        //when
        val actual = render(facility);

        //then
        assertThat(actual).doesNotContain(BANNER);
    }

    /**
     * The converse, so the guard above cannot be satisfied by never rendering the
     * banner at all.
     */
    @Test
    @DisplayName("renders the banner when the flag is set")
    void geometryWithTrueFlag() {
        //given
        val facility = facilityWith(
            Geometry.builder().geometryString(POINT).locationConfidential(true).build()
        );

        //when
        val actual = render(facility);

        //then
        assertThat(actual).contains(BANNER);
    }
}
