package uk.ac.ceh.gateway.catalogue.templateHelpers;

import com.vividsolutions.jts.geom.Envelope;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.*;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeminiExtractorTest {

    private GeminiExtractor service;

    @BeforeEach
    public void init() {
        String localDateStr = "2021-12-20";
        service = new GeminiExtractor(localDateStr);
    }

    @Test
    public void getKeywords() {
        //Given
        val document = new GeminiDocument();
        document.setDescriptiveKeywords(Arrays.asList(
            DescriptiveKeywords.builder()
                .keywords(Arrays.asList(
                    Keyword.builder().URI("http://example.com/0").value("example 1").build(),
                    Keyword.builder().URI("http://example.com/1").value("example 2").build()
                ))
                .type("place")
                .build()
        ));

        //When
        val keywords = service.getKeywords(document);

        //Then
        assertThat(keywords.size(), equalTo(2));
    }

    @Test
    public void getKeywordsWhenNone() {
        //Given
        val document = new GeminiDocument();

        //When
        val keywords = service.getKeywords(document);

        //Then
        assertThat(keywords.size(), equalTo(0));
    }

    @Test
    @SneakyThrows
    public void getWhenNoExtent() {
        //Given
        val document = new GeminiDocument();

        //When
        val env = service.getExtent(document);

        //Then
        assertThat(env.getMinX(), is(-180d));
        assertThat(env.getMinY(), is(-90d));
        assertThat(env.getMaxX(), is(180d));
        assertThat(env.getMaxY(), is(90d));
    }

    @Test
    @SneakyThrows
    public void checkThatCanGetContainedExtent() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        BoundingBox bbox1 = BoundingBox.builder()
                .westBoundLongitude("1")
                .southBoundLatitude("2")
                .eastBoundLongitude("3")
                .northBoundLatitude("4")
                .build();
        when(document.getBoundingBoxes()).thenReturn(Arrays.asList(bbox1));

        //When
        Envelope env = service.getExtent(document);

        //Then
        assertThat(env.getMinX(), is(1d));
        assertThat(env.getMinY(), is(2d));
        assertThat(env.getMaxX(), is(3d));
        assertThat(env.getMaxY(), is(4d));
    }

    @Test
    @SneakyThrows
    public void checkThatCanGetContainedExtentOf2Bbox() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        BoundingBox bbox1 = BoundingBox.builder()
                .westBoundLongitude("1")
                .southBoundLatitude("2")
                .eastBoundLongitude("3")
                .northBoundLatitude("4")
                .build();

        BoundingBox bbox2 = BoundingBox.builder()
                .westBoundLongitude("5")
                .southBoundLatitude("6")
                .eastBoundLongitude("7")
                .northBoundLatitude("8")
                .build();
        when(document.getBoundingBoxes()).thenReturn(Arrays.asList(bbox1, bbox2));

        //When
        Envelope env = service.getExtent(document);

        //Then
        assertThat(env.getMinX(), is(1d));
        assertThat(env.getMinY(), is(2d));
        assertThat(env.getMaxX(), is(7d));
        assertThat(env.getMaxY(), is(8d));
    }

    @Test
    public void isNewServiceAgreementTest() {
        //When
        Boolean isNewServiceAgreement = service.isNewServiceAgreement(LocalDate.parse("2022-12-20"));

        //Then
        assertThat(isNewServiceAgreement, is(true));
    }

    @Test
    public void isNotNewServiceAgreementTest() {
        //When
        Boolean isNewServiceAgreement = service.isNewServiceAgreement(LocalDate.parse("2020-12-20"));

        //Then
        assertThat(isNewServiceAgreement, is(false));
    }
}
