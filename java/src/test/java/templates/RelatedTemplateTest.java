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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
    private static final String TOC = "html/dataResource/_toc.ftlh";
    private static final String TOC_LINK = "href=\"#segment-associations\"";

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
        // _toc.ftlh's last entry asks whether the viewer can edit the record
        configuration.setSharedVariable("permission", mock(PermissionService.class));
    }

    @SneakyThrows
    private String render(GeminiDocument document) {
        return render(WRAPPER, document);
    }

    @SneakyThrows
    private String render(String template, GeminiDocument document) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(template), document);
    }

    /**
     * Each relationship the "Related" section can show, set on its own. {@code _toc.ftlh} keeps its
     * own copy of the section's guard, and relUtilises was added to one and not the other
     * (dri-one #404): a dataset whose only relation was "Produced at" got the section with no
     * contents link to it.
     */
    static Stream<Arguments> relationships() {
        List<Link> one = List.of(Link.builder().href(FACILITY).title("Linked record").build());
        return Stream.of(
            Arguments.of("relPartOf", (Consumer<GeminiDocument>) d -> d.setRelPartOf(one)),
            Arguments.of("relIsRequiredBy", (Consumer<GeminiDocument>) d -> d.setRelIsRequiredBy(one)),
            Arguments.of("relRequires", (Consumer<GeminiDocument>) d -> d.setRelRequires(one)),
            Arguments.of("relRelation", (Consumer<GeminiDocument>) d -> d.setRelRelation(one)),
            Arguments.of("relUtilises", (Consumer<GeminiDocument>) d -> d.setRelUtilises(one))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("relationships")
    @DisplayName("the contents links to the Related section whenever the section is shown")
    void tocAgreesWithSection(String field, Consumer<GeminiDocument> relate) {
        //given
        val document = new GeminiDocument();
        document.setId("cosmos");
        relate.accept(document);

        //when
        val section = render(document);
        val toc = render(TOC, document);

        //then
        assertThat(section).as("Related section").contains("<h2>Related</h2>");
        assertThat(toc).as("contents link").contains(TOC_LINK);
    }

    @Test
    @DisplayName("the contents has no Related link for a dataset with no related records")
    void tocWithoutRelations() {
        //given
        val document = new GeminiDocument();
        document.setId("cosmos");

        //when / then
        assertThat(render(TOC, document)).doesNotContain(TOC_LINK);
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
