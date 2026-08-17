package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.geometry.Geometry;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolrIndexMetadataDocumentGeneratorTest {

    private static final String FDRI_VOCAB = "https://digital.ceh.ac.uk/vocab/fdri/";

    @Mock CodeLookupService codeLookupService;
    @Mock DocumentIdentifierService documentIdentifierService;
    @Mock VocabularyService vocabularyService;
    private SolrIndexMetadataDocumentGenerator generator;

    @BeforeEach
    void createGeminiDocumentSolrIndexGenerator() {
        generator = new SolrIndexMetadataDocumentGenerator(
            codeLookupService,
            documentIdentifierService,
            vocabularyService
        );
    }

    @Test
    void serviceRecordTypeForEidc() {
        //given
        val document = new GeminiDocument();
        document.setType("service");
        document.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(codeLookupService.lookup("metadata.resourceType", "service"))
            .willReturn("Service");
        given(codeLookupService.lookup("metadata.recordType", "service"))
            .willReturn("Map (web service)");

        //when
        val actual = generator.generateIndex(document);

        //then
        assertThat(actual.getRecordType(), equalTo("Map (web service)"));
    }

    @Test
    void serviceRecordTypeForNonEidc() {
        //given
        val document = new GeminiDocument();
        document.setType("service");
        document.setMetadata(MetadataInfo.builder().catalogue("ukscape").build());
        given(codeLookupService.lookup("metadata.resourceType", "service"))
            .willReturn("Service");
        given(codeLookupService.lookup("metadata.recordType", "service"))
            .willReturn("Map (web service)");

        //when
        val actual = generator.generateIndex(document);

        //then
        assertThat(actual.getRecordType(), equalTo("Service"));
    }

    @Test
    @SneakyThrows
    void geometryPolygonLocationsAddedToIndex() {
        //Given
        MonitoringFacility document = new MonitoringFacility();
        String geojsonPolygon = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[-2.526855,53.956086],[-0.241699,52.802761],[-4.020996,52.802761],[-2.526855,53.956086]]]}}";
        document.setGeometry(Geometry.builder()
                .geometryString(geojsonPolygon)
                .build()
        );

        //When
        SolrIndex actual = generator.generateIndex(document);

        //Then
        assertThat(
                actual.getLocations(),
                hasItems("POLYGON((-2.526855 53.956086, -0.241699 52.802761, -4.020996 52.802761, -2.526855 53.956086))")
        );
    }

    @Test
    @SneakyThrows
    void geometryPointLocationsAddedToIndex() {
        //Given
        MonitoringFacility document = new MonitoringFacility();
        String geojsonPolygon = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-1.875916,53.891391]}}";
        document.setGeometry(Geometry.builder()
                .geometryString(geojsonPolygon)
                .build()
        );

        //When
        SolrIndex actual = generator.generateIndex(document);

        //Then
        assertThat(
                actual.getLocations(),
                hasItems("POINT(-1.875916 53.891391)")
        );
    }

    @Test
    void scaleAddedFromModel() {
        //Given
        CehModel model = new CehModel();
        model.setKeywords(Arrays.asList(
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/global").value("global").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/catchment").value("catchment").build()
        ));

        //When
        SolrIndex index = generator.generateIndex(model);
        List<String> actual = index.getInmsScale();

        //Then
        assertThat(actual, hasItems("global", "catchment"));
    }

    @Test
    void scaleAddedFromModelApplication() {
        //Given
        CehModelApplication application = new CehModelApplication();
        application.setKeywords(Arrays.asList(
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/global").value("global").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/catchment").value("catchment").build()
        ));
        CehModelApplication.ModelInfo info0 = new CehModelApplication.ModelInfo();
        info0.setSpatialExtentOfApplication("plot");
        CehModelApplication.ModelInfo info1 = new CehModelApplication.ModelInfo();
        application.setModelInfos(Arrays.asList(info0, info1));

        //When
        SolrIndex index = generator.generateIndex(application);
        List<String> actual = index.getInmsScale();

        //Then
        assertThat(actual, hasItems("global", "catchment", "plot"));
    }

    @Test
    void topicAddedFromModelApplication() {
        //Given
        CehModelApplication application = new CehModelApplication();
        application.setKeywords(Arrays.asList(
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/topic/geology").value("nitrogen").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/topic/soil").value("management").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/topic/weather").value("plot").build()
        ));

        //When
        when(vocabularyService.isMember(anyString(), anyString())).thenReturn(false);
        when(vocabularyService.isMember("topic", "http://vocabs.ceh.ac.uk/inms/topic/geology")).thenReturn(true);
        when(vocabularyService.isMember("topic", "http://vocabs.ceh.ac.uk/inms/topic/soil")).thenReturn(true);


        SolrIndex index = generator.generateIndex(application);
        List<String> actual = index.getInmsTopic();

        //Then
        assertThat(actual, hasItems("nitrogen", "management"));
    }

    @Test
    void checkThatTitleIsTransferredToIndex() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setTitle("my gemini document");

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertEquals("my gemini document", index.getTitle());
    }

    @Test
    void checkThatIdTransferredToIndex() {
        //Given
        String id = "some crazy long, hard to remember, number";
        when(documentIdentifierService.generateFileId(id)).thenReturn("myid");
        GeminiDocument document = new GeminiDocument();
        document.setId(id);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertEquals("myid", index.getIdentifier());
    }

    @Test
    void checkThatDescriptionIsTransferredToIndex() {
        //Given
        String description = "Once upon a time, there was a metadata record...";
        GeminiDocument document = new GeminiDocument();
        document.setDescription(description);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertEquals(description, index.getDescription());
    }

    @Test
    void checkThatResourceTypeIsTransferredToIndex() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setResourceType(Keyword.builder().value("dataset").build());
        when(codeLookupService.lookup("metadata.resourceType", "dataset")).thenReturn("Dataset");

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertEquals("Dataset", index.getResourceType());
    }

    @Test
    public void checkThatCatalogueIsTransferredToIndex() {
        //Given
        MetadataInfo info = MetadataInfo.builder().catalogue("eidc").build();
        MetadataDocument document = new GeminiDocument().setMetadata(info);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(
                "Expected to get 'eidc'",
                index.getCatalogue(),
                equalTo("eidc")
        );
    }

    @Test
    void resourceIdentifierWithCodeAndCodeSpaceIndexesBothBareCodeAndCombined() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setResourceIdentifiers(List.of(
            ResourceIdentifier.builder().code("fafa99").codeSpace("ukceh.eidc").build()
        ));

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(index.getResourceIdentifier(), containsInAnyOrder("fafa99", "ukceh.eidc:fafa99"));
    }

    @Test
    void resourceIdentifierWithCodeOnlyIndexesBareCodeOnly() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setResourceIdentifiers(List.of(
            ResourceIdentifier.builder().code("fafa99").build()
        ));

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(index.getResourceIdentifier(), contains("fafa99"));
    }

    @Test
    void resourceIdentifierWithCodeSpaceButNoCodeIsNotIndexed() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setResourceIdentifiers(List.of(
            ResourceIdentifier.builder().codeSpace("ukceh.eidc").build()
        ));

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(index.getResourceIdentifier(), empty());
    }

    @Test
    void fdriCatchmentAddedFromVocabularyKeywords() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setKeywordsTheme(Arrays.asList(
            Keyword.builder().URI(FDRI_VOCAB + "chess").value("Chess").build(),
            Keyword.builder().URI(FDRI_VOCAB + "upper-severn").value("Upper Severn").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/ukscape/water").value("Water").build()
        ));
        given(vocabularyService.isMember(anyString(), anyString())).willReturn(false);
        given(vocabularyService.isMember("catchment", FDRI_VOCAB + "chess")).willReturn(true);
        given(vocabularyService.isMember("catchment", FDRI_VOCAB + "upper-severn")).willReturn(true);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(index.getFdriCatchment(), containsInAnyOrder("Chess", "Upper Severn"));
    }

    @Test
    void fdriCategoryAddedFromVocabularyKeywords() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setKeywordsTheme(Arrays.asList(
            Keyword.builder().URI(FDRI_VOCAB + "hydrology").value("Hydrology").build(),
            Keyword.builder().URI(FDRI_VOCAB + "geology-and-soils").value("Geology and soils").build()
        ));
        given(vocabularyService.isMember(anyString(), anyString())).willReturn(false);
        given(vocabularyService.isMember("category", FDRI_VOCAB + "hydrology")).willReturn(true);
        given(vocabularyService.isMember("category", FDRI_VOCAB + "geology-and-soils")).willReturn(true);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(index.getFdriCategory(), containsInAnyOrder("Hydrology", "Geology and soils"));
    }

    @Test
    void fdriSpatialScaleAddedFromVocabularyKeyword() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setKeywordsTheme(List.of(
            Keyword.builder().URI(FDRI_VOCAB + "national").value("National").build()
        ));
        given(vocabularyService.isMember(anyString(), anyString())).willReturn(false);
        given(vocabularyService.isMember("spatial-scale", FDRI_VOCAB + "national")).willReturn(true);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(index.getFdriSpatialScale(), equalTo("National"));
    }

    @Test
    void fdriTimeseriesDataAddedFromVocabularyKeyword() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setKeywordsTheme(List.of(
            Keyword.builder().URI(FDRI_VOCAB + "timeseries-yes").value("Yes").build()
        ));
        given(vocabularyService.isMember(anyString(), anyString())).willReturn(false);
        given(vocabularyService.isMember("timeseries", FDRI_VOCAB + "timeseries-yes")).willReturn(true);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(index.getFdriTimeseriesData(), equalTo("Yes"));
    }

    @Test
    void untaggedDocumentHasNoFdriFacetValues() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setKeywordsTheme(List.of(
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/ukscape/water").value("Water").build()
        ));
        given(vocabularyService.isMember(anyString(), anyString())).willReturn(false);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(index.getFdriCatchment(), empty());
        assertThat(index.getFdriCategory(), empty());
        assertThat(index.getFdriSpatialScale(), nullValue());
        assertThat(index.getFdriTimeseriesData(), nullValue());
    }

}
