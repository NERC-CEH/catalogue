package uk.ac.ceh.gateway.catalogue.datapreviewer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology;
import uk.ac.ceh.gateway.catalogue.model.Fileset;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.ObservedProperty;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataPreviewerServiceTest {

    @Mock
    private UploadService uploadService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private JenaLookupService jenaLookupService;

    @InjectMocks
    private DataPreviewerService service;

    private List<Fileset> filesetList;
    private List<TimePeriod> timePeriodsList;

    @BeforeEach
    void setup() {
        ObservedProperty prop = ObservedProperty.builder()
            .value(" temp ")
            .title(" Temperature ")
            .build();

        filesetList = List.of(Fileset.builder()
            .observedProperty(List.of(prop))
            .includes("")
            .build()
        );

        timePeriodsList = List.of(
            TimePeriod.builder()
                .begin("2020-01-01")
                .end("2020-12-31")
                .build()
        );
    }

    @Test
    void previewDatasetReturnsFilesAndObservedProperties() throws Exception {
        GeminiDocument dataset = mock(GeminiDocument.class);
        MetadataInfo metadata = mock(MetadataInfo.class);

        Multimap<Permission, String> perms = ArrayListMultimap.create();
        perms.put(Permission.VIEW, "public");

        when(dataset.getType()).thenReturn("dataset");
        when(dataset.getId()).thenReturn("ds1");
        when(dataset.getTitle()).thenReturn("Dataset 1");
        when(dataset.getMetadata()).thenReturn(metadata);
        when(dataset.getResourceStatus()).thenReturn("Available");
        when(dataset.getAccessLimitation()).thenReturn(
            new AccessLimitation(
                "Open", "", "", "http://purl.org/coar/access_right/c_abf2"
            )
        );
        when(metadata.getPermissions()).thenReturn(perms);

        when(dataset.getFileset()).thenReturn(filesetList);

        HubbubResponse.FileInfo dataFile = new HubbubResponse.FileInfo(
            12345L, "ds1", "eidchub", "csv", "hash", 0.1,
            LocalDateTime.now(), LocalDateTime.now(),
            "/mock/ds1/example.csv", "VALID", "sha256", "text/csv"
        );

        HubbubResponse.FileInfo metadataFile = new HubbubResponse.FileInfo(
            2048L, "ds1", "eidchub", "csv", "hash", 0.1,
            LocalDateTime.now(), LocalDateTime.now(),
            "/mock/ds1/metadata.csv", "VALID", "sha256", "text/csv"
        );

        when(uploadService.get("ds1", "eidchub", 1, 10000))
            .thenReturn(
                new HubbubResponse(List.of(dataFile, metadataFile), null, null)
            );

        when(documentRepository.read("ds1")).thenReturn(dataset);

        when(dataset.getTemporalExtents()).thenReturn(timePeriodsList);

        DatasetPreviewResponse result =
            (DatasetPreviewResponse) service.preview("ds1");

        assertThat(result.type()).isEqualTo("dataset");
        assertThat(result.id()).isEqualTo("ds1");
        assertThat(result.files()).hasSize(2);
        assertThat(result.observedProperties())
            .containsEntry("temp", "Temperature");
        assertThat(result.timePeriods()).containsAll(timePeriodsList);
    }

    @Test
    void previewCollectionReturnsDatasets() throws Exception {
        GeminiDocument collection = mock(GeminiDocument.class);
        GeminiDocument dataset = mock(GeminiDocument.class);
        MetadataInfo metadata = mock(MetadataInfo.class);

        Multimap<Permission, String> perms = ArrayListMultimap.create();
        perms.put(Permission.VIEW, "public");

        when(collection.getType()).thenReturn("aggregate");
        when(collection.getId()).thenReturn("col1");
        when(collection.getTitle()).thenReturn("Collection 1");
        when(collection.getUri()).thenReturn("http://example.org/id/col1");

        when(dataset.getType()).thenReturn("dataset");
        when(dataset.getId()).thenReturn("ds1");
        when(dataset.getTitle()).thenReturn("Dataset 1");
        when(dataset.getMetadata()).thenReturn(metadata);
        when(dataset.getResourceStatus()).thenReturn("Available");
        when(dataset.getAccessLimitation()).thenReturn(
            new AccessLimitation(
                "Open", "", "", "http://purl.org/coar/access_right/c_abf2"
            )
        );
        when(dataset.getFileset()).thenReturn(filesetList);
        when(dataset.getTemporalExtents()).thenReturn(timePeriodsList);
        when(metadata.getPermissions()).thenReturn(perms);

        when(documentRepository.read("col1")).thenReturn(collection);
        when(documentRepository.read("ds1")).thenReturn(dataset);

        when(jenaLookupService.inverseRelationships(
            "http://example.org/id/col1",
            Ontology.EIDC_MEMBER_OF.getURI()
        )).thenReturn(
            List.of(Link.builder().href("http://example.org/id/ds1").build())
        );

        CollectionPreviewResponse result =
            (CollectionPreviewResponse) service.preview("col1");

        assertThat(result.type()).isEqualTo("aggregate");
        assertThat(result.datasets()).hasSize(1);
        assertThat(result.datasets().get(0).id()).isEqualTo("ds1");
        assertThat(result.datasets().get(0).observedProperties())
            .containsEntry("temp", "Temperature");
        assertThat(result.datasets().get(0).timePeriods()).containsAll(timePeriodsList);
        assertThat(result.collections()).isEmpty();
    }

    @Test
    void previewCollectionSkipsNonPublicDatasets() throws Exception {
        GeminiDocument collection = mock(GeminiDocument.class);
        GeminiDocument dataset = mock(GeminiDocument.class);
        MetadataInfo metadata = mock(MetadataInfo.class);

        when(collection.getType()).thenReturn("aggregate");
        when(collection.getId()).thenReturn("col1");
        when(collection.getUri()).thenReturn("http://example.org/id/col1");

        when(dataset.getType()).thenReturn("dataset");
        when(dataset.getId()).thenReturn("ds1");
        when(dataset.getMetadata()).thenReturn(metadata);

        when(metadata.getPermissions()).thenReturn(ArrayListMultimap.create());

        when(documentRepository.read("col1")).thenReturn(collection);
        when(documentRepository.read("ds1")).thenReturn(dataset);

        when(jenaLookupService.inverseRelationships(
            "http://example.org/id/col1",
            Ontology.EIDC_MEMBER_OF.getURI()
        )).thenReturn(
            List.of(Link.builder().href("http://example.org/id/ds1").build())
        );

        CollectionPreviewResponse result =
            (CollectionPreviewResponse) service.preview("col1");

        assertThat(result.datasets()).isEmpty();
        assertThat(result.collections()).isEmpty();
    }

    @Test
    void previewCollectionIncludesSubCollectionDatasets() throws Exception {
        GeminiDocument collection = mock(GeminiDocument.class);
        GeminiDocument subCollection = mock(GeminiDocument.class);
        GeminiDocument dataset = mock(GeminiDocument.class);
        MetadataInfo metadata = mock(MetadataInfo.class);

        Multimap<Permission, String> perms = ArrayListMultimap.create();
        perms.put(Permission.VIEW, "public");

        when(collection.getType()).thenReturn("aggregate");
        when(collection.getId()).thenReturn("col1");
        when(collection.getUri()).thenReturn("http://example.org/id/col1");

        when(subCollection.getType()).thenReturn("aggregate");
        when(subCollection.getId()).thenReturn("col2");
        when(subCollection.getUri()).thenReturn("http://example.org/id/col2");

        when(dataset.getType()).thenReturn("dataset");
        when(dataset.getId()).thenReturn("ds1");
        when(dataset.getTitle()).thenReturn("Dataset 1");
        when(dataset.getMetadata()).thenReturn(metadata);
        when(dataset.getResourceStatus()).thenReturn("Available");
        when(dataset.getAccessLimitation()).thenReturn(
            new AccessLimitation(
                "Open", "", "", "http://purl.org/coar/access_right/c_abf2"
            )
        );
        when(dataset.getFileset()).thenReturn(List.of());
        when(metadata.getPermissions()).thenReturn(perms);

        when(documentRepository.read("col1")).thenReturn(collection);
        when(documentRepository.read("col2")).thenReturn(subCollection);
        when(documentRepository.read("ds1")).thenReturn(dataset);

        when(jenaLookupService.inverseRelationships(
            "http://example.org/id/col1",
            Ontology.EIDC_MEMBER_OF.getURI()
        )).thenReturn(
            List.of(Link.builder().href("http://example.org/id/col2").build())
        );

        when(jenaLookupService.inverseRelationships(
            "http://example.org/id/col2",
            Ontology.EIDC_MEMBER_OF.getURI()
        )).thenReturn(
            List.of(Link.builder().href("http://example.org/id/ds1").build())
        );

        CollectionPreviewResponse result =
            (CollectionPreviewResponse) service.preview("col1");

        assertThat(result.datasets()).isEmpty();
        assertThat(result.collections()).hasSize(1);
        assertThat(result.collections().get(0).id()).isEqualTo("col2");
        assertThat(result.collections().get(0).datasets()).hasSize(1);
        assertThat(result.collections().get(0).datasets().get(0).id())
            .isEqualTo("ds1");
    }

    @Test
    void nonPublicDatasetThrowsSecurity() throws Exception {
        GeminiDocument dataset = mock(GeminiDocument.class);
        MetadataInfo metadata = mock(MetadataInfo.class);

        when(dataset.getType()).thenReturn("dataset");
        when(dataset.getMetadata()).thenReturn(metadata);

        Multimap<Permission, String> perms = ArrayListMultimap.create();
        when(metadata.getPermissions()).thenReturn(perms);

        when(documentRepository.read("ds1")).thenReturn(dataset);

        assertThatThrownBy(() -> service.preview("ds1"))
            .isInstanceOf(SecurityException.class);
    }
}
