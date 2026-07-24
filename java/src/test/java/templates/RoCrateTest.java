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
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.model.Fileset;
import uk.ac.ceh.gateway.catalogue.model.ObservedProperty;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
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

    private void givenFileDetailsServiceDetached(String fileId) {
        given(fileDetailsService.getDetailsFor(fileId, false, "eidchub"))
            .willReturn(List.of(
                new FileDetailsService.Part(fileId, "File", "name1", "text/csv", "testHash", "https://example.com/name1", 12L, LocalDateTime.of(2024,12,9,15, 34)),
                new FileDetailsService.Part(fileId, "File", "name2", "text/csv", "testHash", "https://example.com/name2", 9832L, LocalDateTime.of(2020,5,6,23, 59))
            ));
    }

    private void givenFileDetailsServiceAttached(String fileId) {
        given(fileDetailsService.getDetailsFor(fileId, true, "eidchub"))
            .willReturn(List.of(
                new FileDetailsService.Part(fileId, "File", "name1", "text/csv", "testHash", "data/name4", 542L, LocalDateTime.of(2024,12,9,15, 34)),
                new FileDetailsService.Part(fileId, "File", "name2", "text/csv", "testHash", "data/name5", 32L, LocalDateTime.of(2020,5,6,23, 59))
            ));
    }

    private GeminiDocument createGeminiDocumentAttached(String fileId) {
        val gemini = new GeminiDocument();
        gemini.setUri("https://example.org/id/" + fileId);
        gemini.setId(fileId);
        gemini.setTitle("Title");
        gemini.setType("dataset");
        gemini.setOnlineResources(List.of(
            OnlineResource.builder().function("download").url("https://data-package.ceh.ac.uk/data/05047b98-26a0-4162-adaf-18f68f802d9f").name("Download the data").build()
        ));
        return gemini;
    }

    private GeminiDocument createGeminiDocumentDetached(String fileId) {
        val gemini = new GeminiDocument();
        gemini.setUri("https://example.org/id/" + fileId);
        gemini.setId(fileId);
        gemini.setTitle("Title");
        gemini.setType("dataset");
        gemini.setOnlineResources(List.of(
            OnlineResource.builder().function("fileAccess").url("https://catalogue.ceh.ac.uk/datastore/eidchub/05047b98-26a0-4162-adaf-18f68f802d9f").name("Download the data").build()
        ));
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
        configuration = new Configuration(Configuration.VERSION_2_3_33);
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
            val expected = expected("rocrate/attached-minimal.json");
            val fileId = "123456789";
            gemini = createGeminiDocumentAttached(fileId);
            givenFileDetailsServiceAttached(fileId);

            //when
            val actual = template("rocrate/rocrate_attached.ftl");

            //then
            JSONAssert.assertEquals(expected, actual, true);
            verify(fileDetailsService).getDetailsFor(fileId, true, "eidchub");
        }
    }

    @Nested
    @DisplayName("Detached")
    class Detached {

        @SneakyThrows
        @Test
        void rocrateMinimal() {
            //given
            val expected = expected("rocrate/minimal.json");
            val fileId = "09837382";
            gemini = createGeminiDocumentDetached(fileId);
            givenFileDetailsServiceDetached(fileId);

            //when
            val actual = template("rocrate/rocrate.ftl");

            //then
            JSONAssert.assertEquals(expected, actual, true);
            verify(fileDetailsService).getDetailsFor(fileId, false, "eidchub");
        }

        @SneakyThrows
        @Test
        void rocrateFull() {
            //given
            val expected = expected("rocrate/full.json");

            val fileId = "882739943";
            gemini = createGeminiDocumentDetached(fileId);

            // partsList & partDetails
            givenFileDetailsServiceDetached(fileId);

            // datacite
            givenCodeLookupService();
            gemini.setDatacitable(true);
            gemini.setCitation(Citation.builder()
                    .doi("10.5285/" + fileId)
                    .authors(List.of("Able", "Bracken", "Charles"))
                    .year(2020)
                    .month(1)
                    .day(1)
                    .title("Title")
                    .publisher("Publisher")
                    .resourceTypeGeneral("resourceType")
                .build());

            // availability not Deleted
            gemini.setAccessLimitation(AccessLimitation.builder()
                .availability("Available")
                .build());
            gemini.setDescription("Description");
            gemini.setAlternateTitles(List.of("Alternate Title 1", "Alternate Title 2"));

            // creation & publication dates
            gemini.setDatasetReferenceDate(DatasetReferenceDate.builder()
                    .creationDate(LocalDate.of(2024, 1, 28))
                    .publicationDate(LocalDate.of(2024, 9, 7))
                .build());

            // observed properties
            gemini.setFileset(List.of(
                Fileset.builder()
                    .filesetName("name")
                    .encodingFormat("format")
                    .includes("*.*")
                    .observedProperty(List.of(
                        ObservedProperty.builder().title("observed property 1").uri("https://example.com/op/1").unitsUri("https://example.com/units/m").units("metre").build(),
                        ObservedProperty.builder().value("observed property 2 value").build()
                    ))
                    .build()
            ));

            // keywords
            gemini.setKeywordsPlace(List.of(
                Keyword.builder().value("Lancaster").URI("https://example.com/lacaster").build(),
                Keyword.builder().value("Bangor").URI("https://example.com/bangor").build()
            ));

            // authors
            gemini.setAuthors(List.of(
                ResponsibleParty.builder().givenName("Donald").familyName("Duck").email("donald@example.com").nameIdentifier("https://orcid.org/0000-1234-5678-9101").build()
            ));

            //  points of contact
            gemini.setContactPoints(List.of(
                ResponsibleParty.builder().organisationName("TMSP").email("pocs@example.com").organisationIdentifier("https://example.com/TMSP").build()
            ));

            // incoming citations
            gemini.setIncomingCitations(List.of(
                Supplemental.builder().url("https://example.com/citations/0").description("description").build(),
                Supplemental.builder().url("https://example.com/citations/1").build(),
                Supplemental.builder().name("something else").build()
            ));

            // temporal extents
            gemini.setTemporalExtents(List.of(
                TimePeriod.builder().begin("2024-02-01").end("2024-10-28").build(),
                TimePeriod.builder().begin("2019-08-22").build(),
                TimePeriod.builder().end("2018-11-30").build()
            ));

            // bounding boxes
            gemini.setBoundingBoxes(List.of(
                BoundingBox.builder().northBoundLatitude("46.2").eastBoundLongitude("2.3").southBoundLatitude("45.7").westBoundLongitude("0.4").build(),
                BoundingBox.builder().northBoundLatitude("36.8").eastBoundLongitude("48.7").southBoundLatitude("-11.6").westBoundLongitude("-120.5").build()
            ));

            gemini.setFunding(List.of(
                Funding.builder().funderName("UKRI").build(),
                Funding.builder().funderName("University of Oxford").build()
            ));

            // OGL licences
            gemini.setUseConstraints(List.of(
                ResourceConstraint.builder().code("license").uri("https://eidc.ac.uk/licences/ogl/plain").build()
            ));

            //when
            val actual = template("rocrate/rocrate.ftl");

            //then
            JSONAssert.assertEquals(expected, actual, true);
            verify(fileDetailsService).getDetailsFor(fileId, false, "eidchub");
        }
    }
}
