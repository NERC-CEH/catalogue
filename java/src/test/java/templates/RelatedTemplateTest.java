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
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders {@code _related.ftlh}, the "Related" section of a data resource page.
 * <p>
 * {@code recordType} reaches the include as a namespace variable assigned by
 * {@code dataResource.ftlh}, so the wrapper below reproduces that assign.
 */
@DisplayName("Data resource related records section")
class RelatedTemplateTest {
    private static final String WRAPPER = "test-related.ftlh";
    private static final String WRAPPER_SOURCE = """
        <#assign recordType = "dataset">
        <#include "html/dataResource/_related.ftlh">
        """;

    private static final String FACILITY = "https://catalogue.ceh.ac.uk/id/morley-cosmos";

    private Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void init() {
        val strings = new StringTemplateLoader();
        strings.putTemplate(WRAPPER, WRAPPER_SOURCE);

        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
            strings, new FileTemplateLoader(new File("../templates"))
        }));
    }

    @SneakyThrows
    private String render(GeminiDocument document) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(WRAPPER), document);
    }

    @Test
    @DisplayName("lists the monitoring facilities a dataset was produced at")
    void showsUtilisedFacilities() {
        //given
        val document = new GeminiDocument();
        document.setRelUtilises(List.of(Link.builder()
            .href(FACILITY)
            .title("Morley COSMOS-UK site")
            .publicationStatus("Published")
            .build()));

        //when
        val actual = render(document);

        //then
        assertThat(actual)
            .contains("<h2>Related</h2>")
            .contains("associations-monitoring")
            .contains("href=\"" + FACILITY + "\"")
            .contains("Morley COSMOS-UK site");
    }

    @Test
    @DisplayName("renders no section for a dataset with no related records")
    void noRelations() {
        //when
        val actual = render(new GeminiDocument());

        //then
        assertThat(actual).doesNotContain("<h2>Related</h2>");
    }
}
