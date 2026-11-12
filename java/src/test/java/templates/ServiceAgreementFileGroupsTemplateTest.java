package templates;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.serviceagreement.FileGroup;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Renders {@code _fileGroups.ftlh}, the part of the service agreement page that lists the file groups
 * and any naming convention an older agreement still carries. It is an include so it can be rendered
 * without the page skeleton.
 */
@DisplayName("Service agreement file groups")
class ServiceAgreementFileGroupsTemplateTest {

    private Configuration configuration;

    @BeforeEach
    void init() throws Exception {
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new FileTemplateLoader(new File("../templates")));
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    @SneakyThrows
    private String render(ServiceAgreement agreement) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate("html/service_agreement/_fileGroups.ftlh"),
            agreement
        );
    }

    private FileGroup group(String pattern, Integer count, String description) {
        val group = new FileGroup();
        group.setPattern(pattern);
        group.setCount(count);
        group.setDescription(description);
        return group;
    }

    @Test
    @DisplayName("shows the pattern, count and description of a group")
    void showsGroup() {
        val agreement = new ServiceAgreement();
        agreement.setFileGroups(List.of(group("CP_*_MInv_*Stats_*.csv", 1200, "Chemistry statistics")));

        val actual = render(agreement);

        assertThat(actual, containsString("CP_*_MInv_*Stats_*.csv"));
        assertThat(actual, containsString("1200"));
        assertThat(actual, containsString("Chemistry statistics"));
        assertThat(actual, not(containsString("Previous naming convention")));
    }

    @Test
    @DisplayName("escapes markup in a pattern")
    void escapesPattern() {
        val agreement = new ServiceAgreement();
        agreement.setFileGroups(List.of(group("<img src=x>", 1, null)));

        val actual = render(agreement);

        assertThat(actual, containsString("&lt;img src=x&gt;"));
        assertThat(actual, not(containsString("<img")));
    }

    @Test
    @DisplayName("shows an older agreement's naming convention as the previous one")
    void showsPreviousNamingConvention() {
        val agreement = new ServiceAgreement();
        agreement.setFileNamingConvention("200 files CP_<REGION>.csv");

        val actual = render(agreement);

        assertThat(actual, containsString("Previous naming convention"));
        assertThat(actual, containsString("200 files CP_&lt;REGION&gt;.csv"));
        assertThat(actual, not(containsString("File groups to be deposited")));
    }

    @Test
    @DisplayName("shows nothing when there are no groups and no naming convention")
    void showsNothing() {
        val actual = render(new ServiceAgreement());

        assertThat(actual, not(containsString("Previous naming convention")));
        assertThat(actual, not(containsString("File groups to be deposited")));
    }
}
