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

    @Test
    void thirdPartyFallsBackToThirdPartyWhenNoDistributor() {
        //given
        val gemini = new GeminiDocument();
        gemini.setType("thirdPartyDataset");

        //when
        val actual = template("html/dataResource/_third_party.ftlh", gemini);

        //then
        assertThat(actual).contains("This is a dataset managed by a third party");
    }

    @Test
    void thirdPartyFallsBackToThirdPartyWhenDistributorContactsNull() {
        //given
        val gemini = new GeminiDocument();
        gemini.setType("thirdPartyDataset");
        gemini.setDistributorContacts(null);

        //when
        val actual = template("html/dataResource/_third_party.ftlh", gemini);

        //then
        assertThat(actual).contains("This is a dataset managed by a third party");
    }

    @Test
    void thirdPartyUsesDistributorOrganisationNameAndEmailWhenPresent() {
        //given
        val gemini = new GeminiDocument();
        gemini.setType("thirdPartyDataset");
        gemini.setDistributorContacts(List.of(
            ResponsibleParty.builder().organisationName("INMS").email("data@example.org").build()
        ));

        //when
        val actual = template("html/dataResource/_third_party.ftlh", gemini);

        //then
        assertThat(actual)
            .contains("<a href=\"mailto:data@example.org\" title=\"data@example.org\">INMS</a>")
            .doesNotContain("a third party");
    }

    @Test
    void thirdPartyEscapesDistributorFields() {
        //given
        val gemini = new GeminiDocument();
        gemini.setType("thirdPartyDataset");
        gemini.setDistributorContacts(List.of(
            ResponsibleParty.builder().organisationName("<script>x</script>").email("a\"onmouseover=\"x@example.org").build()
        ));

        //when
        val actual = template("html/dataResource/_third_party.ftlh", gemini);

        //then
        assertThat(actual)
            .contains("&lt;script&gt;x&lt;/script&gt;")
            .doesNotContain("<script>")
            .doesNotContain("\"onmouseover");
    }
}
