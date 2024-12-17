package templates;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class SchemaDotOrgTest {

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

    private GeminiDocument createGeminiDocument(String fileId) {
        val gemini = new GeminiDocument();
        gemini.setUri("https://example.org/id/" + fileId);
        gemini.setId(fileId);
        gemini.setTitle("Title");
        gemini.setType("dataset");
        return gemini;
    }

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
    }

    @SneakyThrows
    @Test
    void schemaDotOrgMinimal() {
        //given
        val expected = expected("schemaDotOrg/minimal.json");
        val fileId = "123456789";
        gemini = createGeminiDocument(fileId);

        //when
        val actual = template("schema.org/schema.org.ftl");
        log.info(actual);

        //then
        JSONAssert.assertEquals(expected, actual, true);
    }
}
