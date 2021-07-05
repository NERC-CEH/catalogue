package uk.ac.ceh.gateway.catalogue.indexing.solr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;

import java.util.Arrays;
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
                            .uri("https://www.eidc.ac.uk/licences/OGLnonceh/plain")
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
}
