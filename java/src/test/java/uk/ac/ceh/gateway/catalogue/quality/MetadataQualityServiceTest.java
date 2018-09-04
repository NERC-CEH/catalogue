package uk.ac.ceh.gateway.catalogue.quality;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ResourceUtils;
import uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService.MetadataCheck;
import uk.ac.ceh.gateway.catalogue.services.DocumentReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService.Severity.ERROR;
import static uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService.Severity.WARNING;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class MetadataQualityServiceTest {
    private MetadataQualityService service;
    // Keep ObjectMapper options same as ObjectMapper in config/ApplicationConfig.java
    private ObjectMapper objectMapper = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .registerModule(new GuavaModule())
        .registerModule(new JaxbAnnotationModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private Configuration config = Configuration.defaultConfiguration()
        .jsonProvider(new JacksonJsonProvider(objectMapper))
        .mappingProvider(new JacksonMappingProvider(objectMapper))
        .addOptions(
            Option.DEFAULT_PATH_LEAF_TO_NULL,
            Option.SUPPRESS_EXCEPTIONS
        );

    @Mock
    private DocumentReader documentReader;

    @Before
    public void setup() {
        this.service = new MetadataQualityService(documentReader, objectMapper);
    }

    @Test
    @SneakyThrows
    public void successfullyCheckExistingDocument() {
        //given
        given(documentReader.read("test1", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("test1.raw"))
            );
        given(documentReader.read("test1", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("test1.meta"))
            );

        //when
        this.service.check("test1");

        //then
        verify(documentReader).read("test1", "raw");
        verify(documentReader).read("test1", "meta");
    }

    @Test
    @SneakyThrows
    public void successfullyCheckEmptyDocument() {
        //given
        given(documentReader.read("test0", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("test0.raw"))
            );
        given(documentReader.read("test0", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("test0.meta"))
            );

        //when
        this.service.check("test0");

        //then
        verify(documentReader).read("test0", "raw");
        verify(documentReader).read("test0", "meta");
    }

    @Test
    public void checkAddressOrganisationName() {
        //given
        val addresses = new ArrayList<Map<String, String>>(Arrays.asList(
            ImmutableMap.of("organisationName", "Test organisation 0"),
            ImmutableMap.of("organisationName", "Test organisation 1"),
            ImmutableMap.of("organisationName", "Test organisation 2")
        ));

        //when
        val actual = this.service.checkAddress(addresses, "Test").isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkWrongAddressOrganisationName() {
        //given
        val addresses = new ArrayList<Map<String, String>>(Arrays.asList(
            ImmutableMap.of("organisationName", "Test organisation 0"),
            ImmutableMap.of("organisationName", "Test organisation 1"),
            ImmutableMap.of("individualName", "Test individual 0")
        ));

        //when
        val actual = this.service.checkAddress(addresses, "Test").isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkNonGeographicWithRightElements() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("nonGeographicRight.json"), this.config);

        //when
        val actual = this.service.checkNonGeographicDatasets(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkNonGeographicWithWrongElements() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("nonGeographicWrong.json"), this.config);

        //when
        val actual = this.service.checkNonGeographicDatasets(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkSignpostHasCorrectOnlineResource() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("signpostRight.json"), this.config);

        //when
        val actual = this.service.checkSignpost(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkSignpostHasIncorrectOnlineResource() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("signpostWrong.json"), this.config);

        //when
        val actual = this.service.checkSignpost(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkSignpostHasMissingOnlineResource() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("signpostMissing.json"), this.config);

        //when
        val actual = this.service.checkSignpost(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkSignpostNotCorrectResourceType() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("signpostNotResourceType.json"), this.config);

        //when
        val actual = this.service.checkSignpost(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkDatasetCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("datasetCorrect.json"), this.config);

        //when
        val actual = this.service.checkDataset(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkDatasetWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("datasetWrong.json"), this.config);

        //when
        val actual = this.service.checkDataset(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkAuthorCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("authorsRight.json"), this.config);

        //when
        val actual = this.service.checkAuthors(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkAuthorWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("authorsWrong.json"), this.config);

        //when
        val actual = this.service.checkAuthors(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkTopicCategoriesCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("topicCategoriesRight.json"), this.config);

        //when
        val actual = this.service.checkTopicCategories(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkTopicCategoriesWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("topicCategoriesWrong.json"), this.config);

        //when
        val actual = this.service.checkTopicCategories(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkCustodianCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("custodiansRight.json"), this.config);

        //when
        val actual = this.service.checkCustodian(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkCustodianWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("custodiansWrong.json"), this.config);

        //when
        val actual = this.service.checkCustodian(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkPublisherCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("publishersRight.json"), this.config);

        //when
        val actual = this.service.checkPublisher(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkPublisherWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("publishersWrong.json"), this.config);

        //when
        val actual = this.service.checkPublisher(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkDistributorCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("distributorsRight.json"), this.config);

        //when
        val actual = this.service.checkDistributor(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkDistributorWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("distributorsWrong.json"), this.config);

        //when
        val actual = this.service.checkDistributor(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkDownloadOrdersCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("downloadOrdersRight.json"), this.config);

        //when
        val actual = this.service.checkDownloadAndOrderLinks(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkDownloadOrdersNotAvailable() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("downloadOrdersNotAvailable.json"), this.config);

        //when
        val actual = this.service.checkDownloadAndOrderLinks(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkDownloadOrdersWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("downloadOrdersWrong.json"), this.config);

        //when
        val actual = this.service.checkDownloadAndOrderLinks(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkDataFormatsCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("dataFormatRight.json"), this.config);

        //when
        val actual = this.service.checkDataFormat(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkDataFormatsWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("dataFormatWrong.json"), this.config);

        //when
        val actual = this.service.checkDataFormat(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkBasicsCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("basicsCorrect.json"), this.config);

        //when
        val actual = this.service.checkBasics(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkBasicsWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("basicsWrong.json"), this.config);

        //when
        val actual = this.service.checkBasics(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkResourceStatusAvailable() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("resourceStatusAvailable.json"), this.config);

        //when
        val actual = this.service.resourceStatusIsAvailable(parsed);

        //then
        assertTrue(actual);
    }

    @Test
    public void checkResourceStatusNotAvailable() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("resourceStatusNotAvailable.json"), this.config);

        //when
        val actual = this.service.resourceStatusIsAvailable(parsed);

        //then
        assertFalse(actual);
    }

    @Test
    public void checkInspireThemeCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("inspireCorrect.json"), this.config);

        //when
        val actual = this.service.checkInspireTheme(parsed).isPresent();

        //then
        assertFalse(actual);
    }

    @Test
    public void checkInspireThemeWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("inspireWrong.json"), this.config);

        //when
        val actual = this.service.checkInspireTheme(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkInspireThemeKeywordWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("inspireKeywordWrong.json"), this.config);

        //when
        val actual = this.service.checkInspireTheme(parsed).isPresent();

        //then
        assertTrue(actual);
    }

    @Test
    public void checkBoundingBoxesWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("boundingBoxWrong.json"), this.config);

        //when
        val actual = this.service.checkBoundingBoxes(parsed).isPresent();

        //then
        assertTrue(actual);
    }


    @Test
    public void resultsErrors() {
        //given
        val checks = Arrays.asList(
            new MetadataCheck("check 1", ERROR),
            new MetadataCheck("check 2", ERROR),
            new MetadataCheck("check 3", WARNING),
            new MetadataCheck("check 4", ERROR),
            new MetadataCheck("check 5", ERROR)
        );

        //when
        val actual = new MetadataQualityService.Results(checks, "test").getErrors();

        //then
        assertThat(actual, equalTo(4L));
    }

    @Test
    public void resultsWarnings() {
        //given
        val checks = Arrays.asList(
            new MetadataCheck("check 1", WARNING),
            new MetadataCheck("check 2", ERROR),
            new MetadataCheck("check 3", WARNING),
            new MetadataCheck("check 4", ERROR),
            new MetadataCheck("check 5", WARNING)
        );

        //when
        val actual = new MetadataQualityService.Results(checks, "test").getWarnings();

        //then
        assertThat(actual, equalTo(3L));
    }

    @Test
    public void problemsSortedBySeverity() {
        //given
        val expected = Arrays.asList(ERROR, ERROR, ERROR, ERROR, WARNING, WARNING, WARNING, WARNING, WARNING, WARNING);
        val checks = Arrays.asList(
            new MetadataCheck("check 1", WARNING),
            new MetadataCheck("check 2", ERROR),
            new MetadataCheck("check 3", WARNING),
            new MetadataCheck("check 4", ERROR),
            new MetadataCheck("check 5", WARNING),
            new MetadataCheck("check 6", WARNING),
            new MetadataCheck("check 7", ERROR),
            new MetadataCheck("check 8", WARNING),
            new MetadataCheck("check 9", ERROR),
            new MetadataCheck("check 10", WARNING)
        );

        //when
        val actual = new MetadataQualityService.Results(checks, "test")
            .getProblems()
            .stream()
            .map(MetadataCheck::getSeverity)
            .collect(Collectors.toList());

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void totalErrorsAndWarnings() {
        //given
        val expectedTotalErrors = 8L;
        val expectedTotalWarnings = 12L;
        val checks = Arrays.asList(
            new MetadataCheck("check 1", WARNING),
            new MetadataCheck("check 2", ERROR),
            new MetadataCheck("check 3", WARNING),
            new MetadataCheck("check 4", ERROR),
            new MetadataCheck("check 5", WARNING),
            new MetadataCheck("check 6", WARNING),
            new MetadataCheck("check 7", ERROR),
            new MetadataCheck("check 8", WARNING),
            new MetadataCheck("check 9", ERROR),
            new MetadataCheck("check 10", WARNING)
        );

        val results =Arrays.asList(
            new MetadataQualityService.Results(checks, "test0"),
            new MetadataQualityService.Results(checks, "test1")
        );

        //when
        val actual = new MetadataQualityService.CatalogueResults(results);

        //then
        assertThat(actual.getTotalErrors(), equalTo(expectedTotalErrors));
        assertThat(actual.getTotalWarnings(), equalTo(expectedTotalWarnings));
    }
}