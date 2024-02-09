package uk.ac.ceh.gateway.catalogue.datacite.templates;

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
import org.xmlunit.builder.DiffBuilder;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
@DisplayName("Datacite template")
public class DataciteTemplateTest {
    Configuration configuration;
    GeminiDocument gemini;
    Map<String, Object> model;

    @SneakyThrows
    private String expected(String filename) {
        val expected = Objects.requireNonNull(getClass().getResourceAsStream(filename));
        return IOUtils.toString(expected, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private String template(String templateFilename) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(templateFilename),
            model
        );
    }

    private void compare(String expected, String actual) {
        val diff = DiffBuilder
            .compare(expected)
            .withTest(actual)
            .normalizeWhitespace()
            .checkForIdentical()
            .build();
        if(diff.hasDifferences()) {
            log.debug(actual);
        }
        assertFalse(diff.hasDifferences());
    }

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDirectoryForTemplateLoading(new File("../templates/datacite"));
        gemini = new GeminiDocument();
        model = new HashMap<>();
        model.put("doc", gemini);
    }

    @Nested
    @DisplayName("subjects")
    class Subjects {

        @Test
        @DisplayName("without subjects")
        void empty() {
            //given
            val expected = "";

            //when
            val actual = template("_subjects.ftlx");

            //then
            assertThat(actual, equalTo(expected));
        }

        @Test
        @DisplayName("with subjects")
        void full() {
            //given
            gemini.setKeywordsPlace(List.of(
                Keyword
                    .builder()
                    .value("Edinburgh")
                    .URI("https://sws.geonames.org/2650225")
                    .build()
            ));
            gemini.setKeywordsInstrument(List.of(
                Keyword
                    .builder()
                    .value("Trumpet")
                    .URI("https://example.com/trumpet")
                    .build()
            ));

            val expected = expected("subjects-full.xml");

            //when
            val actual = template("_subjects.ftlx");

            //then
            compare(expected, actual);
        }
    }

    @Nested
    @DisplayName("contributors")
    class Contributors {

        @Test
        @DisplayName("without contributors")
        void empty() {
            //given
            val expected = "";

            //when
            val actual = template("_contributors.ftlx");

            //then
            assertThat(actual, equalTo(expected));
        }

        @Test
        @DisplayName("with contributors")
        void full() {
            //given
            val pointOfContact = ResponsibleParty
                .builder()
                .role("pointOfContact")
                .individualName("Bob")
                .organisationName("Example Inc.")
                .build();
            val rightsHolder = ResponsibleParty
                .builder()
                .role("rightsHolder")
                .organisationName("Science Inc.")
                .organisationIdentifier("https://ror.org/00pggkr55")
                .build();
            val custodian = ResponsibleParty
                .builder()
                .role("custodian")
                .organisationName("EIDC")
                .organisationIdentifier("https://ror.org/04xw4m193")
                .build();
            gemini.setResponsibleParties(List.of(
                pointOfContact,
                rightsHolder,
                custodian
            ));

            val expected = expected("contributors-full.xml");

            //when
            val actual = template("_contributors.ftlx");

            //then
            compare(expected, actual);
        }
    }
}
