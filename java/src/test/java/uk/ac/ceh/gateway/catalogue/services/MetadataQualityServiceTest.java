package uk.ac.ceh.gateway.catalogue.services;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ResourceUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.services.MetadataQualityService.MetadataCheck;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.ac.ceh.gateway.catalogue.services.MetadataQualityService.Failure.ERROR;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class MetadataQualityServiceTest {
    private MetadataQualityService service;
    private Configuration config = Configuration.defaultConfiguration()
        .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

    @Mock
    private DocumentReader documentReader;

    @Before
    public void setup() {
        this.service = new MetadataQualityService(documentReader);
    }

    @Test
    @SneakyThrows
    public void successfullyCheckExistingDocument() {
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
    public void stringPassesIsMissing() {
        //given
        val expected = new MetadataCheck("Title is missing", "pass", ERROR);
        val parsed = JsonPath.parse("{\"title\": \"some title\"}", this.config);

        //when
        val actual = this.service.isMissing(parsed,"Title", "title");

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void multiLevelStringPassesIsMissing() {
        //given
        val expected = new MetadataCheck("Title is missing", "pass", ERROR);
        val parsed = JsonPath.parse("{\"container\": {\"title\": \"some title\"}}", this.config);

        //when
        val actual = this.service.isMissing(parsed,"Title", "container.title");

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void multiLevelEmptyStringIsMissing() {
        //given
        val expected = new MetadataCheck("Title is missing", "fail", ERROR);
        val parsed = JsonPath.parse("{\"container\": {\"title\": \"\"}}", this.config);

        //when
        val actual = this.service.isMissing(parsed,"Title", "container.title");

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void nullFailsIsMissing() {
        //given
        val expected = new MetadataCheck("Title is missing", "fail", ERROR);
        val parsed = JsonPath.parse("{\"unknown\": \"some title\"}", this.config);

        //when
        val actual = this.service.isMissing(parsed,"Title", "title");

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void emptyStringFailsIsMissing() {
        //given
        val expected = new MetadataCheck("Title is missing", "fail", ERROR);
        val parsed = JsonPath.parse("{\"title\": \"\"}", this.config);

        //when
        val actual = this.service.isMissing(parsed,"Title", "title");

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
        val expected = new MetadataCheck("Licence is missing", "pass", ERROR);

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
        val expected = new MetadataCheck("Licence is missing", "fail", ERROR);

        //when
        val actual = service.hasLicences(gemini);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void doesNotHaveUseConstraints() {
        //given
        val gemini = new GeminiDocument();
        val expected = new MetadataCheck("Licence is missing", "fail", ERROR);

        //when
        val actual = service.hasLicences(gemini);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasCorrectCount() {
        //given
        val target = Arrays.asList("one", "two");
        val expected = new MetadataCheck("Test count should be 2", "pass", ERROR);

        //when
        val actual = this.service.hasCount("Test", target, ERROR, 2);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasTooManyCount() {
        //given
        val target = Arrays.asList("one", "two", "three");
        val expected = new MetadataCheck("Test count should be 2", "fail", ERROR);

        //when
        val actual = this.service.hasCount("Test", target, ERROR, 2);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasNullCount() {
        //given
        val expected = new MetadataCheck("Test count should be 2", "fail", ERROR);

        //when
        val actual = this.service.hasCount("Test", null, ERROR, 2);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasEmptyListCount() {
        //given
        val target = Collections.emptyList();
        val expected = new MetadataCheck("Test count should be 2", "fail", ERROR);

        //when
        val actual = this.service.hasCount("Test", target, ERROR, 2);

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void hasNoTemporalExtents() {
        //given
        val gemini = new GeminiDocument();
        val expected = new MetadataCheck("Temporal extents is missing", "fail", ERROR);

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
        val expected = new MetadataCheck("Temporal extents is empty", "pass", ERROR);

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
        val expected = new MetadataCheck("Temporal extents is empty", "fail", ERROR);

        //when
        val actual = this.service.emptyTemporalExtents(gemini);

        //then
        assertThat(actual, equalTo(expected));
    }
}