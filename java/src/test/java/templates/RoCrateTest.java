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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.citation.Citation;
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.ObservedProperty;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FileDetailsService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@Slf4j
@DisplayName("RO-Crate")
@ExtendWith(MockitoExtension.class)
public class RoCrateTest {

    Configuration configuration;
    GeminiDocument gemini;
    @Mock
    CodeLookupService codeLookupService;
    @Mock
    FileDetailsService fileDetailsService;

    private void givenCodeLookupService() {
        given(codeLookupService.lookup("datacite.resourceTypeGeneral", "resourceType"))
            .willReturn("Resource Type");
    }

    private void givenFileDetailsService(String fileId) {
        given(fileDetailsService.getDetailsFor(fileId, false))
            .willReturn(List.of(
                new FileDetailsService.Part(fileId, "File", "name1", "text/csv", "https://example.com/name1", 12L, LocalDateTime.of(2024,12,9,15, 34)),
                new FileDetailsService.Part(fileId, "File", "name2", "text/csv", "https://example.com/name2", 9832L, LocalDateTime.of(2020,5,6,23, 59))
            ));
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
        configuration.setSharedVariable("codes", codeLookupService);
        configuration.setSharedVariable("fileDetails", fileDetailsService);
    }

    @Nested
    @DisplayName("Attached")
    class Attached {

        @SneakyThrows
        @Test
        void rocrateAttachedMinimal() {
            //given
            gemini = createGeminiDocument("123456789");
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
            gemini = createGeminiDocument("09837382");
            val expected = expected("rocrate/minimal.json");

            //when
            val actual = template("rocrate/rocrate.ftl");

            //then
            JSONAssert.assertEquals(expected, actual, true);
        }

        @SneakyThrows
        @Test
        void rocrateFull() {
            //given
            val expected = expected("rocrate/full.json");

            val fileId = "882739943";
            gemini = createGeminiDocument(fileId);

            // partsList & partDetails
            givenFileDetailsService(fileId);

            // datacite
            givenCodeLookupService();
            gemini.setDatacitable(true);
            gemini.setCitation(Citation.builder()
                    .doi("10.5285/" + fileId)
                    .authors(List.of("Able", "Bracken", "Charles"))
                    .year(2020)
                    .title("Title")
                    .publisher("Publisher")
                    .resourceTypeGeneral("resourceType")
                .build());

            // resourceStatus not Deleted
            gemini.setAccessLimitation(AccessLimitation.builder()
                    .code("Available")
                .build());
            gemini.setDescription("Description");
            gemini.setAlternateTitles(List.of("Alternate Title 1", "Alternate Title 2"));

            // creation & publication dates
            gemini.setDatasetReferenceDate(DatasetReferenceDate.builder()
                    .creationDate(LocalDate.of(2024, 1, 28))
                    .publicationDate(LocalDate.of(2024, 9, 7))
                .build());

            // observed properties
            gemini.setObservedProperty(List.of(
                ObservedProperty.builder().title("observed property 1").uri("https://example.com/op/1").unitsUri("https://example.com/units/m").units("metre").build(),
                ObservedProperty.builder().value("observed property 2 value").build()
            ));

            // keywords
            gemini.setKeywordsPlace(List.of(
                Keyword.builder().value("Lancaster").URI("https://example.com/lacaster").build(),
                Keyword.builder().value("Bangor").URI("https://example.com/bangor").build()
            ));

            // authors and points of contact
            gemini.setResponsibleParties(List.of(
                ResponsibleParty.builder().role("author").individualName("Donald").nameIdentifier("https://orcid.org/0000-1234-5678-9101").build(),
                ResponsibleParty.builder().role("pointOfContact").organisationName("TMSP").organisationIdentifier("https://example.com/TMSP").build()
            ));

            //when
            val actual = template("rocrate/rocrate.ftl");
            log.info(actual);

            //then
            JSONAssert.assertEquals(expected, actual, true);
            verify(fileDetailsService).getDetailsFor(fileId, false);
        }
    }
}
