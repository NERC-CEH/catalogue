package templates;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@DisplayName("RO-Crate")
public class RoCrateTest {

    Configuration configuration;
    GeminiDocument gemini;

    @SneakyThrows
    private String expected(String filename) {
        val expected = Objects.requireNonNull(getClass().getResourceAsStream(filename));
        return IOUtils.toString(expected, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private String template(String templateFilename) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(templateFilename),
            gemini
        );
    }

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        gemini = new GeminiDocument();
    }

    @Nested
    @DisplayName("Attached")
    class Attached {

        @SneakyThrows
        @Test
        void rocrateAttachedMinimal() {
            //given
            val uri = "https://example.org/id/123456789";
            gemini.setUri(uri);
            gemini.setTitle("Title");
            gemini.setType("dataset");
            val expected = expected("rocrate/attached-minimal.json");

            //when
            val actual = template("rocrate/rocrate_attached.ftl");

            //then
            JSONAssert.assertEquals(expected, actual, true);
        }
    }

    @Nested
    @DisplayName("Detached")
    class Detached {

        @SneakyThrows
        @Test
        void rocrateMinimal() {
            //given
            val uri = "https://example.org/id/123456789";
            gemini.setUri(uri);
            gemini.setTitle("Title");
            gemini.setType("dataset");
            val expected = expected("rocrate/minimal.json");

            //when
            val actual = template("rocrate/rocrate.ftl");

            //then
            JSONAssert.assertEquals(expected, actual, true);
        }
    }
}
