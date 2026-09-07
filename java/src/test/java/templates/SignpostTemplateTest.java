package templates;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SignpostTemplateTest {

    Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
    }

    @SneakyThrows
    private String template(String templateFilename, GeminiDocument gemini) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(templateFilename),
            gemini
        );
    }

    @Test
    void nercSignpostFallsBackToThirdPartyWhenNoDistributor() {
        //given
        val gemini = new GeminiDocument();
        gemini.setType("nercSignpost");

        //when
        val actual = template("html/dataResource/_nercSignpost.ftlh", gemini);

        //then
        assertThat(actual).contains("a third party");
    }

    @Test
    void nercSignpostUsesDistributorOrganisationNameWhenPresent() {
        //given
        val gemini = new GeminiDocument();
        gemini.setType("nercSignpost");
        gemini.setDistributorContacts(List.of(
            ResponsibleParty.builder().organisationName("EIDC").build()
        ));

        //when
        val actual = template("html/dataResource/_nercSignpost.ftlh", gemini);

        //then
        assertThat(actual).contains("EIDC").doesNotContain("a third party");
    }

    @Test
    void signpostFallsBackToThirdPartyWhenNoDistributor() {
        //given
        val gemini = new GeminiDocument();
        gemini.setType("signpost");

        //when
        val actual = template("html/dataResource/_signpost.ftlh", gemini);

        //then
        assertThat(actual).contains("a third party");
    }

    @Test
    void signpostUsesDistributorOrganisationNameWhenPresent() {
        //given
        val gemini = new GeminiDocument();
        gemini.setType("signpost");
        gemini.setDistributorContacts(List.of(
            ResponsibleParty.builder().organisationName("EIDC").build()
        ));

        //when
        val actual = template("html/dataResource/_signpost.ftlh", gemini);

        //then
        assertThat(actual).contains("EIDC").doesNotContain("a third party");
    }
}
