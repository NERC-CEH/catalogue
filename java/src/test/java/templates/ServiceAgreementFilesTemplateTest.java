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
import uk.ac.ceh.gateway.catalogue.serviceagreement.File;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FileSizes;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Renders {@code _files.ftlh}, the part of the service agreement page that lists the named files.
 */
@DisplayName("Service agreement files")
class ServiceAgreementFilesTemplateTest {

    private Configuration configuration;

    @BeforeEach
    void init() throws Exception {
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new FileTemplateLoader(new java.io.File("../templates")));
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setSharedVariable("fileSizes", new FileSizes());
    }

    @SneakyThrows
    private String render(File file) {
        val agreement = new ServiceAgreement();
        agreement.setFiles(List.of(file));
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate("html/service_agreement/_files.ftlh"),
            agreement
        );
    }

    private File file(String name) {
        val file = new File();
        file.setName(name);
        return file;
    }

    @Test
    @DisplayName("shows a size in bytes formatted")
    void showsBytesFormatted() {
        val file = file("a.csv");
        file.setBytes(33554432L);

        assertThat(render(file), containsString("32 MB"));
    }

    @Test
    @DisplayName("prefers the bytes to the legacy size text")
    void prefersBytes() {
        val file = file("a.csv");
        file.setBytes(2621440L);
        file.setSize("big");

        val actual = render(file);

        assertThat(actual, containsString("2.5 MB"));
        assertThat(actual, not(containsString("big")));
    }

    @Test
    @DisplayName("shows a legacy size as typed")
    void showsLegacySize() {
        val file = file("a.csv");
        file.setSize("32Mb");

        assertThat(render(file), containsString("32Mb"));
    }

    @Test
    @DisplayName("marks a missing size")
    void marksMissingSize() {
        assertThat(render(file("a.csv")), containsString("<span class=\"text-danger\">missing</span>"));
    }

    @Test
    @DisplayName("shows the media type after the format, muted")
    void showsFormatType() {
        val file = file("a.csv");
        file.setFormat("Comma-separated values (CSV)");
        file.setFormatType("text/csv");

        assertThat(render(file), containsString("Comma-separated values (CSV) <span class=\"text-muted\">text/csv</span>"));
    }
}
