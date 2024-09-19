package uk.ac.ceh.gateway.catalogue.quality;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MonitoringQualityServiceTest {
    private MonitoringQualityService service;
    private ObjectMapper objectMapper = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .registerModule(new GuavaModule())
        .registerModule(new JaxbAnnotationModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Mock
    private DocumentReader documentReader;

    @BeforeEach
    public void setup() {
        this.service = new MonitoringQualityService(documentReader, objectMapper);
    }

    @Test
    @SneakyThrows
    public void successfullyCheckMonitoringNetwork() {
        given(documentReader.read("monitoringNetworkRight", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringNetworkRight.raw"))
            );
        given(documentReader.read("monitoringNetworkRight", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringNetworkRight.meta"))
            );

        //when
        val results = this.service.check("monitoringNetworkRight");

        //then
        verify(documentReader).read("monitoringNetworkRight", "raw");
        verify(documentReader).read("monitoringNetworkRight", "meta");
        assertThat(results.getProblems(), empty());
    }

    @Test
    @SneakyThrows
    public void successfullyCheckMonitoringProgramme() {
        given(documentReader.read("monitoringProgrammeRight", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringProgrammeRight.raw"))
            );
        given(documentReader.read("monitoringProgrammeRight", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringProgrammeRight.meta"))
            );

        //when
        val results = this.service.check("monitoringProgrammeRight");

        //then
        verify(documentReader).read("monitoringProgrammeRight", "raw");
        verify(documentReader).read("monitoringProgrammeRight", "meta");
        assertThat(results.getProblems(), empty());
    }

    @Test
    @SneakyThrows
    public void successfullyCheckMonitoringActivity() {
        given(documentReader.read("monitoringActivityRight", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringActivityRight.raw"))
            );
        given(documentReader.read("monitoringActivityRight", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringActivityRight.meta"))
            );

        //when
        val results = this.service.check("monitoringActivityRight");

        //then
        verify(documentReader).read("monitoringActivityRight", "raw");
        verify(documentReader).read("monitoringActivityRight", "meta");
        assertThat(results.getProblems(), empty());
    }

    @Test
    @SneakyThrows
    public void successfullyCheckMonitoringFacility() {
        given(documentReader.read("monitoringFacilityRight", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringFacilityRight.raw"))
            );
        given(documentReader.read("monitoringFacilityRight", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringFacilityRight.meta"))
            );

        //when
        val results = this.service.check("monitoringFacilityRight");

        //then
        verify(documentReader).read("monitoringFacilityRight", "raw");
        verify(documentReader).read("monitoringFacilityRight", "meta");
        assertThat(results.getProblems(), empty());
    }

    @Test
    @SneakyThrows
    public void checkNetworkNoKeywords() {
        given(documentReader.read("monitoringNetworkNoKeywords", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringNetworkNoKeywords.raw"))
            );
        given(documentReader.read("monitoringNetworkNoKeywords", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringNetworkRight.meta"))
            );

        //when
        val results = this.service.check("monitoringNetworkNoKeywords");

        //then
        verify(documentReader).read("monitoringNetworkNoKeywords", "raw");
        verify(documentReader).read("monitoringNetworkNoKeywords", "meta");
        val problems = results.getProblems().stream().map(check -> check.getTest()).toList();
        assertThat(problems, contains("Keywords is empty"));
    }

    @Test
    @SneakyThrows
    public void checkFacilityWrong() {
        given(documentReader.read("monitoringFacilityWrong", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringFacilityWrong.raw"))
            );
        given(documentReader.read("monitoringFacilityWrong", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringFacilityRight.meta"))
            );

        //when
        val results = this.service.check("monitoringFacilityWrong");

        //then
        verify(documentReader).read("monitoringFacilityWrong", "raw");
        verify(documentReader).read("monitoringFacilityWrong", "meta");
        val problems = results.getProblems().stream().map(check -> check.getTest()).toList();
        assertThat(problems, containsInAnyOrder("Operating period is empty", "Geometry is missing", "Facility type is missing"));
    }

    @Test
    @SneakyThrows
    public void checkProgrammeMissingFields() {
        given(documentReader.read("monitoringProgrammeMissingFields", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringProgrammeMissingFields.raw"))
            );
        given(documentReader.read("monitoringProgrammeMissingFields", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringProgrammeRight.meta"))
            );

        //when
        val results = this.service.check("monitoringProgrammeMissingFields");

        //then
        verify(documentReader).read("monitoringProgrammeMissingFields", "raw");
        verify(documentReader).read("monitoringProgrammeMissingFields", "meta");
        val problems = results.getProblems().stream().map(check -> check.getTest()).toList();
        assertThat(problems, contains("Bounding box is missing"));
    }

    @Test
    @SneakyThrows
    public void checkActivityBoundingBox() {
        given(documentReader.read("monitoringActivityBoundingBox", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringActivityBoundingBox.raw"))
            );
        given(documentReader.read("monitoringActivityBoundingBox", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("ukeof/monitoringProgrammeRight.meta"))
            );

        //when
        val results = this.service.check("monitoringActivityBoundingBox");

        //then
        verify(documentReader).read("monitoringActivityBoundingBox", "raw");
        verify(documentReader).read("monitoringActivityBoundingBox", "meta");
        val problems = results.getProblems().stream().map(check -> check.getTest()).toList();
        assertThat(problems, containsInAnyOrder(
            "westBoundLongitude is out of range",
            "eastBoundLongitude is missing from bounding box",
            "southBoundLatitude should be less than northBoundLatitude",
            "southBoundLatitude is out of range"
        ));
    }
}
