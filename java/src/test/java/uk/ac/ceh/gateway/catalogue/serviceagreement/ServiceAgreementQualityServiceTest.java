package uk.ac.ceh.gateway.catalogue.serviceagreement;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReader;
import uk.ac.ceh.gateway.catalogue.quality.CatalogueResults;
import uk.ac.ceh.gateway.catalogue.quality.MetadataCheck;
import uk.ac.ceh.gateway.catalogue.quality.Results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.ERROR;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.WARNING;
import static uk.ac.ceh.gateway.catalogue.serviceagreement.GitRepoServiceAgreementService.FOLDER;

@Slf4j
@ActiveProfiles({"service-agreement"})
@ExtendWith(MockitoExtension.class)
public class ServiceAgreementQualityServiceTest {
    private ServiceAgreementQualityService service;
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

    @BeforeEach
    public void setup() {
        this.service = new ServiceAgreementQualityService(documentReader, objectMapper, "EIDCHELP-");
    }


    @Test
    public void checkBasicsRight() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("basicsRight.json"), this.config);

        //when
        val actual = this.service.checkBasics(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkBasicsWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("basicsWrong.json"), this.config);

        //when
        val actual = this.service.checkBasics(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    @SneakyThrows
    public void successfullyCheckExistingDocument() {
        //given
        given(documentReader.read(FOLDER + "test1", "raw"))
                .willReturn(
                        ResourceUtils.getFile(getClass().getResource("test1.raw"))
                );
        given(documentReader.read(FOLDER + "test1", "meta"))
                .willReturn(
                        ResourceUtils.getFile(getClass().getResource("test1.meta"))
                );

        //when
        this.service.check("test1");

        //then
        verify(documentReader).read(FOLDER + "test1", "raw");
        verify(documentReader).read(FOLDER + "test1", "meta"); //meta not working
    }

    @Test
    @SneakyThrows
    public void successfullyCheckEmptyDocument() {
        //given
        given(documentReader.read(FOLDER + "test0", "raw"))
                .willReturn(
                        ResourceUtils.getFile(getClass().getResource("test0.raw"))
                );
        given(documentReader.read(FOLDER + "test0", "meta"))
                .willReturn(
                        ResourceUtils.getFile(getClass().getResource("test0.meta"))
                );

        //when
        this.service.check("test0");

        //then
        verify(documentReader).read(FOLDER + "test0", "raw"); // raw not working
        verify(documentReader).read(FOLDER + "test0", "meta");
    }

    @Test
    public void checkAuthorRight() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("authorsRight.json"), this.config);

        //when
        val actual = this.service.checkAuthors(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkAuthorWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("authorsWrong.json"), this.config);

        //when
        val actual = this.service.checkAuthors(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkOwnersRight() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("ownersRight.json"), this.config);

        //when
        val actual = this.service.checkOwnerOfIpr(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkOwnersWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("ownersWrong.json"), this.config);

        //when
        val actual = this.service.checkOwnerOfIpr(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkDepositorContactDetailsRight() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("contactDetailsRight.json"), this.config);

        //when
        val actual = this.service.checkDepositorContactDetails(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkDepositorContactDetailsWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("contactDetailsWrong.json"), this.config);

        //when
        val actual = this.service.checkDepositorContactDetails(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkFilesRight() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("checkFilesRight.json"), this.config);

        //when
        val actual = this.service.checkFiles(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkFilesWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("checkFilesWrong.json"), this.config);

        //when
        val actual = this.service.checkFiles(parsed);

        //then
        assertThat(actual, not(empty()));
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
        val actual = new Results(checks, "test").getErrors();

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
        val actual = new Results(checks, "test").getWarnings();

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
        val actual = new Results(checks, "test")
                .getProblems()
                .stream()
                .map(MetadataCheck::getSeverity)
                .toList();

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

        val results = Arrays.asList(
                new Results(checks, "test0"),
                new Results(checks, "test1")
        );

        //when
        val actual = new CatalogueResults(results);

        //then
        assertThat(actual.getTotalErrors(), equalTo(expectedTotalErrors));
        assertThat(actual.getTotalWarnings(), equalTo(expectedTotalWarnings));
    }

    @Test
    public void checkSupportingDocsRight() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("supportingDocsRight.json"), this.config);

        //when
        val actual = this.service.checkSupportingDocs(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkSupportingDocsWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("supportingDocsWrong.json"), this.config);

        //when
        val actual = this.service.checkSupportingDocs(parsed);

        //then
        assertThat(actual, not(empty()));
    }
}
