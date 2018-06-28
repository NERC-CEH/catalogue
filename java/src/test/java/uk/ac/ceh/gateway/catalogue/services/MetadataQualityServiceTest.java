package uk.ac.ceh.gateway.catalogue.services;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.MetadataQualityService.MetadataCheck;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MetadataQualityServiceTest {
    private MetadataQualityService service;

    @Mock
    private DocumentRepository repository;

    @Before
    public void setup() {
        this.service = new MetadataQualityService(repository);
    }

    @Test
    @SneakyThrows
    public void successfullyCheckExistingDocument() {
        //given
        given(repository.read("test")).willReturn(new GeminiDocument());

        //when
        this.service.check("test");

        //then
        verify(repository).read("test");
    }

    @Test
    public void stringPassesIsMissing() {
        //given
        val expected = new MetadataCheck("Test is missing", "pass", 1);

        //when
        val actual = this.service.isMissing("Test", "title", 1);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void nullFailsIsMissing() {
        //given
        val expected = new MetadataCheck("Test is missing", "fail", 1);

        //when
        val actual = this.service.isMissing("Test", null, 1);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void emptyStringFailsIsMissing() {
        //given
        val expected = new MetadataCheck("Test is missing", "fail", 1);

        //when
        val actual = this.service.isMissing("Test", "", 1);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void successfullyHasLicence() {
        //given
        val gemini = new GeminiDocument()
            .setUseConstraints(Arrays.asList(
                new ResourceConstraint("Licence 1", "license", "http://example.com/licence1"),
                new ResourceConstraint("something else", "another", "http://example.com/another")
            ));
        val expected = new MetadataCheck("Licence is missing", "pass", 1);

        //when
        val actual = service.hasLicences(gemini);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void doesNotHaveLicence() {
        //given
        val gemini = new GeminiDocument()
            .setUseConstraints(Arrays.asList(
                new ResourceConstraint("Another", "another", "http://example.com/another1"),
                new ResourceConstraint("something else", "another", "http://example.com/another")
            ));
        val expected = new MetadataCheck("Licence is missing", "fail", 1);

        //when
        val actual = service.hasLicences(gemini);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void doesNotHaveUseConstraints() {
        //given
        val gemini = new GeminiDocument();
        val expected = new MetadataCheck("Licence is missing", "fail", 1);

        //when
        val actual = service.hasLicences(gemini);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasCorrectCount() {
        //given
        val target = Arrays.asList("one", "two");
        val expected = new MetadataCheck("Test count should be 2", "pass", 1);

        //when
        val actual = this.service.hasCount("Test", target, 1, 2);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasTooManyCount() {
        //given
        val target = Arrays.asList("one", "two", "three");
        val expected = new MetadataCheck("Test count should be 2", "fail", 1);

        //when
        val actual = this.service.hasCount("Test", target, 1, 2);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasNullCount() {
        //given
        val expected = new MetadataCheck("Test count should be 2", "fail", 1);

        //when
        val actual = this.service.hasCount("Test", null, 1, 2);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasEmptyListCount() {
        //given
        val target = Collections.emptyList();
        val expected = new MetadataCheck("Test count should be 2", "fail", 1);

        //when
        val actual = this.service.hasCount("Test", target, 1, 2);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasNoTemporalExtents() {
        //given
        val gemini = new GeminiDocument();
        val expected = new MetadataCheck("Temporal extents is missing", "fail", 1);

        //when
        val actual = this.service.emptyTemporalExtents(gemini);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasTemporalExtents() {
        //given
        val gemini = new GeminiDocument()
            .setTemporalExtents(Collections.singletonList(
                TimePeriod.builder()
                    .begin("2018-06-03")
                    .end("2018-06-23")
                    .build()
            ));
        val expected = new MetadataCheck("Temporal extents is empty", "pass", 1);

        //when
        val actual = this.service.emptyTemporalExtents(gemini);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasMissingTemporalExtents() {
        //given
        val gemini = new GeminiDocument()
            .setTemporalExtents(Collections.singletonList(
                TimePeriod.builder().build()
            ));
        val expected = new MetadataCheck("Temporal extents is empty", "fail", 1);

        //when
        val actual = this.service.emptyTemporalExtents(gemini);

        //then
        assertThat(actual, equalTo(expected));
    }
}