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

class LinkedTemplateTest {

    Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
    }

    @SneakyThrows
    private String template(GeminiDocument gemini) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate("html/dataResource/_linked.ftlh"),
            gemini
        );
    }

    @Test
    void rendersWithoutThrowingWhenNoPublisher() {
        //given
        val gemini = new GeminiDocument();

        //when
        val actual = template(gemini);

        //then
        assertThat(actual).doesNotContain("UKCEH_EIDC_black.png");
    }

    @Test
    void showsEidcLogoWhenPublisherIsEidc() {
        //given
        val gemini = new GeminiDocument();
        gemini.setPublishers(List.of(
            ResponsibleParty.builder().organisationName("NERC EDS Environmental Information Data Centre").build()
        ));

        //when
        val actual = template(gemini);

        //then
        assertThat(actual).contains("UKCEH_EIDC_black.png");
    }

    @Test
    void doesNotShowEidcLogoWhenPublisherIsNotEidc() {
        //given
        val gemini = new GeminiDocument();
        gemini.setPublishers(List.of(
            ResponsibleParty.builder().organisationName("Some Other Publisher").build()
        ));

        //when
        val actual = template(gemini);

        //then
        assertThat(actual).doesNotContain("UKCEH_EIDC_black.png");
    }
}
