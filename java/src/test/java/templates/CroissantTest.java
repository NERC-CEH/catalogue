package templates;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.citation.Citation;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FileDetailsService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CroissantTest {

    Configuration configuration;
    GeminiDocument gemini;
    @Mock
    FileDetailsService fileDetailsService;

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
        gemini.setOnlineResources(List.of(
            OnlineResource.builder().function("fileAccess").url("https://catalogue.ceh.ac.uk/datastore/eidchub/05047b98-26a0-4162-adaf-18f68f802d9f").name("Download the data").build()
        ));
        return gemini;
    }

    private GeminiDocument createGeminiDocumentFull(String fileId) {
        val gemini = createGeminiDocument(fileId);
        gemini.setDescription("Description of the dataset.");
        gemini.setDatasetReferenceDate(DatasetReferenceDate.builder().publicationDate(LocalDate.of(2024, 8, 22)).build());
        gemini.setUseConstraints(List.of(ResourceConstraint.builder().code("license").uri("https://eidc.ac.uk/licences/ogl3").build()));
        gemini.setDatacitable(true);
        gemini.setCitation(Citation.builder().doi("doi").publisher("EIDC").title("Title of dataset").year(2024).authors(List.of("Bob", "Helen")).build());
        gemini.setKeywordsDiscipline(List.of(
            Keyword.builder().value("Environmental survey").build()
        ));
        gemini.setResponsibleParties(List.of(
            ResponsibleParty.builder().role("author").givenName("L.").familyName("Hand").nameIdentifier("https://orcid.org/0000-0001-9570-7479").organisationName("University of Edinburgh").organisationIdentifier("https://ror.org/01nrxwf90").build(),
            ResponsibleParty.builder().role("publisher").email("info@eidc.ac.uk").organisationName("NERC EDS Environmental Information Data Centre").organisationIdentifier("https://ror.org/04xw4m193").build()
        ));
        return gemini;
    }

    private void givenFileDetailsServiceEmpty(String fileId) {
        given(fileDetailsService.getDetailsFor(fileId, false, "eidchub"))
            .willReturn(Collections.emptyList());
    }

    private void givenFileDetailsServiceFull(String fileId) {
        given(fileDetailsService.getDetailsFor(fileId, false, "eidchub"))
            .willReturn(List.of(
                new FileDetailsService.Part("data0.csv", "File", "data0.csv", "text/csv", "29cd35efe0af017002da20c0f3b5e7c30d7fb5abe6906a3d92967240c5c5b337", "https://catalogue.ceh.ac.uk/datastore/eidchub/123456789/data0.csv", 997L, LocalDateTime.of(2024,7,21,8, 7)),
                new FileDetailsService.Part("data1.parquet", "File", "data1.parquet", "application/parquet", "86e165efe0af017002da20c0f3b5e7c30d7fb5abe6906a3d92967240c5c98ac2", "https://catalogue.ceh.ac.uk/datastore/eidchub/123456789/data1.parquet", 638L, LocalDateTime.of(2020,5,6,23, 59))
            ));
    }

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        configuration.setSharedVariable("fileDetails", fileDetailsService);
    }

    @SneakyThrows
    @Test
    void croissantMinimal() {
        //given
        val expected = expected("croissant/minimal.json");
        val fileId = "123456789";
        gemini = createGeminiDocument(fileId);
        givenFileDetailsServiceEmpty(fileId);

        //when
        val actual = template("croissant/croissant.ftl");
        log.debug(actual);

        //then
        JSONAssert.assertEquals(expected, actual, true);
    }

    @SneakyThrows
    @Test
    void croissantFull() {
        //given
        val expected = expected("croissant/full.json");
        val fileId = "123456789";
        gemini = createGeminiDocumentFull(fileId);
        givenFileDetailsServiceFull(fileId);

        //when
        val actual = template("croissant/croissant.ftl");
        log.debug(actual);

        //then
        JSONAssert.assertEquals(expected, actual, true);
    }
}
