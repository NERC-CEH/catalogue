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
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.citation.Citation;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@DisplayName("Citation template")
public class CitationTemplateTest {
    Configuration configuration;
    Citation citationWithSingleValue = createCitationWithSngleValue();
    Citation citationWithListValue = createCitationWithListValue();

    @SneakyThrows
    private String expected(String filename) {
        val expected = Objects.requireNonNull(getClass().getResourceAsStream(filename));
        return IOUtils.toString(expected, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private String template(String templateFilename, Object model) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(templateFilename),
            model
        );
    }

    private void compare(String expected, String actual) {
        assertThat(actual.trim(), equalTo(expected.trim()));
    }

    private Citation createCitationWithListValue() {
        return Citation.builder()
            .doi("10.1234/12345678-abcd-abcd-abcd-123456789abc")
            .authors(List.of("Tester A", "Tester B"))
            .publisher("NERC Environmental Information Data Centre")
            .title("This is a test")
            .year(2025)
            .build();
    }

    private Citation createCitationWithSngleValue() {
        return Citation.builder()
            .doi("10.1234/12345678-abcd-abcd-abcd-123456789abc")
            .authors(List.of("Tester A"))
            .publisher("NERC Environmental Information Data Centre")
            .title("This is a test")
            .year(2025)
            .build();
    }

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
    }

    @Nested
    @DisplayName("Bib")
    class Bib {
        @Test
        @DisplayName("Bib with single value in field")
        void bibWithSibgleValue() {
            // given
            val expected = expected("citation/bib-single-value.txt");

            // when
            val actual = template("citation/bib.ftlh", citationWithSingleValue);

            //then
            compare(expected, actual);
        }

        @Test
        @DisplayName("Bib with list of values in field")
        void bibWithListValue() {
            // given
            val expected = expected("citation/bib-list-value.txt");

            // when
            val actual = template("citation/bib.ftlh", citationWithListValue);

            //then
            compare(expected, actual);
        }
    }

    @Nested
    @DisplayName("Research info system")
    class Ris {
        @Test
        @DisplayName("Ris with single value in field")
        void risWithSibgleValue() {
            // given
            val expected = expected("citation/ris-single-value.txt");

            // when
            val actual = template("citation/ris.ftlh", citationWithSingleValue);

            //then
            compare(expected, actual);
        }

        @Test
        @DisplayName("Ris with list of values in field")
        void risWithListValue() {
            // given
            val expected = expected("citation/ris-list-value.txt");

            // when
            val actual = template("citation/ris.ftlh", citationWithListValue);

            //then
            compare(expected, actual);
        }
    }
}
