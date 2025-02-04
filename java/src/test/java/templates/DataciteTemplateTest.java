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
import org.xmlunit.builder.DiffBuilder;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        gemini = new GeminiDocument();
        model = new HashMap<>();
        model.put("doc", gemini);
    }

    @Nested
    @DisplayName("rights")
    class Rights {

        @Test
        @DisplayName("with OGL licence")
        void withOglLicence() {
            // given
            val expected = expected("datacite/rights-ogl.xml");
            gemini.setUseConstraints(List.of(
                ResourceConstraint.builder().code("license").uri("https://eidc.ceh.ac.uk/licences/OGL/plain").build()
            ));

            // when
            val actual = template("datacite/_rights.ftlx");

            //then
            compare(expected, actual);
        }

        @Test
        @DisplayName("with other licence")
        void withOtherLicence() {
            // given
            val expected = expected("datacite/rights-other.xml");
            gemini.setUseConstraints(List.of(
                ResourceConstraint.builder()
                    .code("license")
                    .uri("https://example.com/licences/1")
                    .value("license")
                    .build()
            ));

            // when
            val actual = template("datacite/_rights.ftlx");

            //then
            compare(expected, actual);
        }
    }

    @Nested
    @DisplayName("related")
    class Related {

        @Test
        @SneakyThrows
        @DisplayName("with related records")
        void related() {
            //given
            val uri = "https://example.org/id/123456789";
            gemini
                .setOnlineResources(List.of(
                    OnlineResource.builder()
                        .function("information")
                        .url("https://data-package.ceh.ac.uk/sd/123456789")
                        .build()
                ))
                .setUri(uri);

            val jena = mock(JenaLookupService.class);
            configuration.setSharedVariable("jena", jena);
            val expected = expected("datacite/related-full.xml");

            given(jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#supersedes")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/847463839").build()
            ));
            given(jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#supersedes")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/28319461").build()
            ));

            //when
            val actual = template("datacite/_related.ftlx");

            //then
            compare(expected, actual);
        }
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
            val actual = template("datacite/_subjects.ftlx");

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

            val expected = expected("datacite/subjects-full.xml");

            //when
            val actual = template("datacite/_subjects.ftlx");

            //then
            compare(expected, actual);
        }
    }

    @Nested
    @DisplayName("base")
    class Base {

        @SneakyThrows
        @Test
        @DisplayName("with everything else")
        void full() {
            //given
            val uri = "https://example.org/id/123456789";
            val jena = mock(JenaLookupService.class);
            configuration.setSharedVariable("jena", jena);

            given(jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#supersedes")).willReturn(Collections.emptyList());
            given(jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#supersedes")).willReturn(Collections.emptyList());

            model.put("doi", "doi:123123");
            gemini
                .setResponsibleParties(List.of(
                    ResponsibleParty.builder().role("publisher").organisationIdentifier("https://ror.org/1234542").organisationName("EIDC").build(),
                    ResponsibleParty.builder().role("publisher").organisationName("OTHER").build()
                ))
                .setDatasetReferenceDate(DatasetReferenceDate.builder().publicationDate(LocalDate.of(2025, 2, 3)).build())
                .setTitle("Test")
                .setDescription("Description")
                .setUri(uri);

            val expected = expected("datacite/datacite-full.xml");

            //when
            val actual = template("datacite/datacite.ftlx");

            //then
            compare(expected, actual);
        }

    }

    @Nested
    @DisplayName("formats")
    class Formats {

        @Test
        @DisplayName("with creators")
        void full() {
            //given
            gemini.setDistributionFormats(List.of(
                DistributionInfo.builder().name("csv").type("text").build(),
                DistributionInfo.builder().name("nc").type("binary").build(),
                DistributionInfo.builder().name("sdf").version("2.0").build()
            ));

            val expected = expected("datacite/formats-full.xml");

            //when
            val actual = template("datacite/_formats.ftlx");

            //then
            compare(expected, actual);
        }
    }

    @Nested
    @DisplayName("creators")
    class Creators {
        @Test
        @DisplayName("with creators")
        void full() {
            //given
            val author1 = ResponsibleParty
                .builder()
                .role("author")
                .individualName("Bob")
                .organisationName("Example Inc.")
                .build();
            val author2 = ResponsibleParty
                .builder()
                .role("author")
                .individualName("George")
                .organisationName("Science Inc.")
                .organisationIdentifier("https://ror.org/00pggkr55")
                .build();
            val author3 = ResponsibleParty
                .builder()
                .role("author")
                .individualName("Helen")
                .organisationName("EIDC")
                .organisationIdentifier("https://ror.org/04xw4m193")
                .build();
            gemini.setResponsibleParties(List.of(
                author1,
                author2,
                author3
            ));

            val expected = expected("datacite/creators-full.xml");

            //when
            val actual = template("datacite/_creators.ftlx");

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
            val actual = template("datacite/_contributors.ftlx");

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

            val expected = expected("datacite/contributors-full.xml");

            //when
            val actual = template("datacite/_contributors.ftlx");

            //then
            compare(expected, actual);
        }
    }
}
