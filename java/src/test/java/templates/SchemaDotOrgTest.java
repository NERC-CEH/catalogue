package templates;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class SchemaDotOrgTest {

    Configuration configuration;
    GeminiDocument gemini;

    @SneakyThrows
    private String expected(String filename) {
        val expected = Objects.requireNonNull(getClass().getResourceAsStream(filename));
        return IOUtils.toString(expected, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private String template() {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate("schema.org/schema.org.ftl"),
            gemini
        );
    }

    private GeminiDocument createGeminiDocument() {
        val gemini = new GeminiDocument();
        gemini.setUri("https://example.org/id/" + "123456789");
        gemini.setId("123456789");
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
        gemini = createGeminiDocument();

        //when
        val actual = template();

        //then
        JSONAssert.assertEquals(expected, actual, true);
    }

    @SneakyThrows
    @Test
    void schemaDotOrgWithFunding() {
        //given
        val expected = expected("schemaDotOrg/with-funding.json");
        gemini = createGeminiDocument();
        gemini.setFunding(List.of(
            Funding.builder()
                .funderName("Natural Environment Research Council")
                .funderIdentifier("https://ror.org/00h27bh59")
                .awardNumber("NE/S010351/1")
                .awardURI("https://gtr.ukri.org/projects?term=NE/S010351/1")
                .build(),
            Funding.builder()
                .funderName("European Union")
                .awardTitle("Horizon 2020 research and innovation programme")
                .awardNumber("776480")
                .build(),
            Funding.builder()
                .funderName("Scottish Government")
                .awardTitle("Hydro Nation Scholars Programme")
                .build()
        ));

        //when
        val actual = template();

        //then
        JSONAssert.assertEquals(expected, actual, true);
    }

    @SneakyThrows
    @Test
    void schemaDotOrgOglLicence() {
        //given
        val expected = expected("schemaDotOrg/ogl-licence.json");
        gemini = createGeminiDocument();
        gemini.setUseConstraints(List.of(
            ResourceConstraint.builder().code("license").uri("https://eidc.ac.uk/licences/ogl/plain").build()
        ));

        //when
        val actual = template();

        //then
        JSONAssert.assertEquals(expected, actual, true);
    }
}
