package uk.ac.ceh.gateway.catalogue.indexing.solr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Indexing Gemini documents into Solr")
@ExtendWith(MockitoExtension.class)
class SolrIndexGeminiDocumentGeneratorTest {
    @Mock
    SolrIndexMetadataDocumentGenerator documentIndexer;
    @Mock CodeLookupService codeLookupService;
    private SolrIndexGeminiDocumentGenerator generator;

    @BeforeEach
    void init() {
        when(documentIndexer.generateIndex(any(MetadataDocument.class))).thenReturn(new SolrIndex());
        generator = new SolrIndexGeminiDocumentGenerator(new ExtractTopicFromDocument(), documentIndexer, codeLookupService);
    }

    @Test
    @DisplayName("Topic is transferred")
    void checkThatTopicIsTransferredToIndex() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getTopics()).thenReturn(Arrays.asList("http://onto.nerc.ac.uk/CEHMD/topic/2", "http://onto.nerc.ac.uk/CEHMD/topic/3"));
        List<String> expected = Arrays.asList("0/Biodiversity/", "0/Phenology/");

        //When
        SolrIndex index = generator.generateIndex(document);
        List<String> actual = index.getTopic();

        //Then
        assertThat("Actual topic should have required items", actual, equalTo(expected));
    }

    @Nested
    @DisplayName("checking isOgl")
    class Check {

        @Test
        @DisplayName("is transferred")
        void checkThatIsOglTrueIsTransferredToIndex() {
            //Given
            GeminiDocument document = mock(GeminiDocument.class);
            when(document.getUseConstraints()).thenReturn(Arrays.asList(
                    ResourceConstraint.builder()
                            .uri("https://www.eidc.ac.uk/licences/ceh-open-government-licence/plain")
                            .build(),
                    ResourceConstraint.builder()
                            .uri("https://www.eidc.ac.uk/licences/open-government-licence-non-ceh-data/plain")
                            .build(),
                    ResourceConstraint.builder()
                            .value("More use limitations")
                            .build()
            ));
            when(codeLookupService.lookup("licence.isOgl", true)).thenReturn("IS OGL");

            //When
            SolrIndex index = generator.generateIndex(document);

            //Then
            assertEquals("IS OGL", index.getLicence());
        }

        @Test
        @DisplayName("is transferred for other urls")
        void checkThatIsOglTrueIsTransferredToIndexForOtherFormatOfUrl() {
            //Given
            GeminiDocument document = mock(GeminiDocument.class);
            when(document.getUseConstraints()).thenReturn(Arrays.asList(
                    ResourceConstraint.builder()
                            .uri("https://www.eidc.ac.uk/licences/open-government-licence-non-ceh-data/plain")
                            .build(),
                    ResourceConstraint.builder()
                            .value("More use limitations")
                            .build()
            ));
            when(codeLookupService.lookup("licence.isOgl", true)).thenReturn("IS OGL");

            //When
            SolrIndex index = generator.generateIndex(document);

            //Then
            assertEquals("IS OGL", index.getLicence());
        }

        @Test
        @DisplayName("is transferred for other formats")
        void checkThatIsOglTrueIsTransferredToIndexForOtherFormatOfUrlForAnother() {
            //Given
            GeminiDocument document = mock(GeminiDocument.class);
            when(document.getUseConstraints()).thenReturn(Arrays.asList(
                    ResourceConstraint.builder()
                            .uri("https://www.eidc.ac.uk/licences/oglnonceh/plain")
                            .build(),
                    ResourceConstraint.builder()
                            .value("More use limitations")
                            .build()
            ));
            when(codeLookupService.lookup("licence.isOgl", true)).thenReturn("IS OGL");

            //When
            SolrIndex index = generator.generateIndex(document);

            //Then
            assertEquals("IS OGL", index.getLicence());
        }

        @Test
        @DisplayName("is false")
        void checkThatIsOglFalseIsTransferredToIndex() {
            //Given
            GeminiDocument document = mock(GeminiDocument.class);
            when(document.getUseConstraints()).thenReturn(Arrays.asList(
                    ResourceConstraint.builder()
                            .uri("https://data-package.ceh.ac.uk/sd/eb7599f4-35f8-4365-bd4a-4056ee6c6083.zip")
                            .build(),
                    ResourceConstraint.builder()
                            .value("More use limitations")
                            .build()
            ));
            when(codeLookupService.lookup("licence.isOgl", false)).thenReturn("ISNT OGL");

            //When
            SolrIndex index = generator.generateIndex(document);

            //Then
            assertEquals("ISNT OGL", index.getLicence());
        }
    }

    @Nested
    @DisplayName("Temporal extent formatting")
    @MockitoSettings(strictness = Strictness.LENIENT)
    class TemporalExtentFormatting {

        @Test
        @DisplayName("Both begin and end")
        void bothBeginAndEnd() {
            var period = TimePeriod.builder().begin("2000-01-01").end("2015-12-31").build();
            String result = generator.formatTemporalExtents(List.of(period));
            assertEquals("Data collected from 2000 to 2015", result);
        }

        @Test
        @DisplayName("Begin only")
        void beginOnly() {
            var period = TimePeriod.builder().begin("2005-06-15").build();
            String result = generator.formatTemporalExtents(List.of(period));
            assertEquals("Data collection started in 2005", result);
        }

        @Test
        @DisplayName("End only")
        void endOnly() {
            var period = TimePeriod.builder().end("2010-03-01").build();
            String result = generator.formatTemporalExtents(List.of(period));
            assertEquals("Data available up to 2010", result);
        }

        @Test
        @DisplayName("Null list returns null")
        void nullListReturnsNull() {
            assertThat(generator.formatTemporalExtents(null), equalTo(null));
        }

        @Test
        @DisplayName("Empty list returns null")
        void emptyListReturnsNull() {
            assertThat(generator.formatTemporalExtents(Collections.emptyList()), equalTo(null));
        }

        @Test
        @DisplayName("Multiple periods joined with semicolon")
        void multiplePeriods() {
            var p1 = TimePeriod.builder().begin("1990-01-01").end("1995-12-31").build();
            var p2 = TimePeriod.builder().begin("2000-01-01").end("2010-12-31").build();
            String result = generator.formatTemporalExtents(List.of(p1, p2));
            assertEquals("Data collected from 1990 to 1995; Data collected from 2000 to 2010", result);
        }
    }
}
