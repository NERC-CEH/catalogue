package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SampleArchiveIndexGeneratorTest {

    @Mock
    private SolrIndexMetadataDocumentGenerator metadataDocumentGenerator;
    private SampleArchiveIndexGenerator generator;

    @BeforeEach
    void setup() {
        generator = new SampleArchiveIndexGenerator(metadataDocumentGenerator);
    }

    @Test
    @SneakyThrows
    void indexMetadataDocumentFields() {
        //given
        val document = new SampleArchive();

        given(metadataDocumentGenerator.generateIndex(document))
            .willReturn(new SolrIndex());

        //when
        generator.generateIndex(document);

        //then
    }

    @Test
    @SneakyThrows
    void indexSampleArchiveFields() {
        //given
        val document = new SampleArchive();
        document
            .setPhysicalStates(List.of(
                Keyword.builder()
                    .value("Air dried")
                    .URI("http://vocab.ceh.ac.uk.org/esb#airdry")
                    .build()
            ))
            .setSpecimenTypes(List.of(
                Keyword.builder()
                    .value("Air")
                    .URI("http://vocab.ceh.ac.uk.org/esb#air")
                    .build()
            ))
            .setTaxa(List.of(
                Keyword.builder()
                    .value("Algae")
                    .URI("http://vocab.ceh.ac.uk.org/esb#algae'")
                    .build()
            ))
            .setTissues(List.of(
                Keyword.builder()
                    .value("Bone")
                    .URI("http://vocab.ceh.ac.uk.org/esb#bone")
                    .build()
            ));

        given(metadataDocumentGenerator.generateIndex(document))
            .willReturn(new SolrIndex());

        //when
        val index = generator.generateIndex(document);

        //then
        assertThat(index.getSaPhysicalState(), hasItem("Air dried"));
        assertThat(index.getSaSpecimenType(), hasItem("Air"));
        assertThat(index.getSaTaxon(), hasItem("Algae"));
        assertThat(index.getSaTissue(), hasItem("Bone"));
    }

}
