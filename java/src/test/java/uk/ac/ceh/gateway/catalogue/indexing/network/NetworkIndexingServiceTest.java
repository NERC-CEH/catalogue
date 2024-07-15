package uk.ac.ceh.gateway.catalogue.indexing.network;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;
import uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@DisplayName("NetworkIndexing")
@ExtendWith(MockitoExtension.class)
class NetworkIndexingServiceTest {
    @Mock BundledReaderService<MetadataDocument> bundledReader;
    @Mock DocumentRepository documentRepository;
    @Mock JenaLookupService lookupService;
    @Captor ArgumentCaptor<CatalogueUser> userCaptor;
    @Captor ArgumentCaptor<MonitoringNetwork> networkDocCaptor;
    @Captor ArgumentCaptor<String> commitMessageCaptor;

    @InjectMocks
    private NetworkIndexingService networkIndexingService;
    private final BigDecimal precision = BigDecimal.valueOf((Geometry.POINT_PRECISION + 0.0000001));
    private final String commitMessageTemplate = NetworkIndexingService.COMMIT_MESSAGE_TEMPLATE;

    private MonitoringFacility getMonitoringFacility(String id, String coords, MonitoringNetwork network) {
        MonitoringFacility mf = new MonitoringFacility();
        mf.setId(id);
        String geojsonPolygon = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[" + coords + "]}}";
        mf.setGeometry(Geometry.builder()
                .geometryString(geojsonPolygon)
                .build()
        );
        if(network != null) {
            mf.setRelationships(
                com.google.common.collect.Sets.newHashSet(
                    new Relationship(Ontology.BELONGS_TO.getURI(), network.getUri())
                )
            );
        }
        return mf;
    }

    private MonitoringNetwork getMonitoringNetwork(String id) {
        MonitoringNetwork mn = new MonitoringNetwork();
        mn.setId(id);
        mn.setUri(id);
        return mn;
    }

    @Test
    @SneakyThrows
    void indexDocuments() {
        // given
        BigDecimal expectedNorth = BigDecimal.valueOf(200);
        BigDecimal expectedSouth = BigDecimal.valueOf(0);
        BigDecimal expectedEast = BigDecimal.valueOf(200);
        BigDecimal expectedWest = BigDecimal.valueOf(0);
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        MonitoringFacility f1 = getMonitoringFacility("f1", expectedEast + ",50", n1);
        MonitoringFacility f2 = getMonitoringFacility("f2", expectedWest + ",50", n1);
        MonitoringFacility f3 = getMonitoringFacility("f3", "50," + expectedNorth, n1);
        MonitoringFacility f4 = getMonitoringFacility("f4", "50," + expectedSouth, n1);
        List<String> toIndex = Arrays.asList(f1.getId(), f2.getId(), f3.getId(), f4.getId());
        List<Link> links = Arrays.asList(f1,f2,f3, f4).stream()
            .map(f -> Link.builder()
                    .geometry(f.getGeometry().getGeometryString())
                    .href(f.getId())
                    .build()
            )
            .collect(Collectors.toList());
        String expectedCommitMessage = String.format(commitMessageTemplate, toIndex.get(toIndex.size() - 1));

        // when
        when(bundledReader.readBundle(f1.getId())).thenReturn(f1);
        when(bundledReader.readBundle(f2.getId())).thenReturn(f2);
        when(bundledReader.readBundle(f3.getId())).thenReturn(f3);
        when(bundledReader.readBundle(f4.getId())).thenReturn(f4);
        when(bundledReader.readBundle(n1.getId())).thenReturn(n1);
        when(lookupService.inverseRelationships(n1.getUri(), Ontology.BELONGS_TO.getURI())).thenReturn(links);

        networkIndexingService.indexDocuments(toIndex);
        verify(documentRepository, times(toIndex.size())).save(userCaptor.capture(), networkDocCaptor.capture(), commitMessageCaptor.capture());
        BoundingBox actualEnvelope = networkDocCaptor.getAllValues().get(toIndex.size() - 1).getBoundingBox();
        String actualCommitMessage = commitMessageCaptor.getAllValues().get(toIndex.size() - 1);

        // then
        assertThat(actualEnvelope.getNorthBoundLatitude(), is(closeTo(expectedNorth, precision)));
        assertThat(actualEnvelope.getSouthBoundLatitude(), is(closeTo(expectedSouth, precision)));
        assertThat(actualEnvelope.getEastBoundLongitude(), is(closeTo(expectedEast, precision)));
        assertThat(actualEnvelope.getWestBoundLongitude(), is(closeTo(expectedWest, precision)));
        assertThat(actualCommitMessage, equalTo(expectedCommitMessage));
    }
    @Test
    @SneakyThrows
    void unindexDocuments() {
        // given
        BigDecimal expectedNorth = BigDecimal.valueOf(200);
        BigDecimal expectedSouth = BigDecimal.valueOf(0);
        BigDecimal expectedEast = BigDecimal.valueOf(200);
        BigDecimal expectedWest = BigDecimal.valueOf(0);
        BigDecimal unexpectedNorth = BigDecimal.valueOf(500);
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        MonitoringFacility f1 = getMonitoringFacility("f1", expectedEast + ",50", n1);
        MonitoringFacility f2 = getMonitoringFacility("f2", expectedWest + "," + expectedSouth, n1);
        MonitoringFacility f3 = getMonitoringFacility("f3", "50," + expectedNorth, n1);
        MonitoringFacility f4 = getMonitoringFacility("f4", "50," + unexpectedNorth, n1);
        List<Link> links = Arrays.asList(f1,f2,f3,f4).stream()
            .map(f -> Link.builder()
                    .geometry(f.getGeometry().getGeometryString())
                    .href(f.getId())
                    .build()
            )
            .collect(Collectors.toList());
        String expectedCommitMessage = String.format(commitMessageTemplate, f4.getId());

        // when
        when(bundledReader.readBundle(n1.getId())).thenReturn(n1);
        when(lookupService.inverseRelationships(n1.getUri(), Ontology.BELONGS_TO.getURI())).thenReturn(links);

        networkIndexingService.unindexDocuments(f4.getId(), Arrays.asList(n1.getId()));
        verify(documentRepository, times(1)).save(userCaptor.capture(), networkDocCaptor.capture(), commitMessageCaptor.capture());
        BoundingBox actualEnvelope = networkDocCaptor.getAllValues().get(0).getBoundingBox();
        String actualCommitMessage = commitMessageCaptor.getAllValues().get(0);

        // then
        assertThat(actualEnvelope.getNorthBoundLatitude(), is(closeTo(expectedNorth, precision)));
        assertThat(actualEnvelope.getSouthBoundLatitude(), is(closeTo(expectedSouth, precision)));
        assertThat(actualEnvelope.getEastBoundLongitude(), is(closeTo(expectedEast, precision)));
        assertThat(actualEnvelope.getWestBoundLongitude(), is(closeTo(expectedWest, precision)));
        assertThat(actualCommitMessage, equalTo(expectedCommitMessage));
    }

    @Test
    public void getCorrectCombindedEnvelope() {
        // given
        BigDecimal expectedNorth = BigDecimal.valueOf(1000);
        BigDecimal expectedSouth = BigDecimal.valueOf(10);
        BigDecimal expectedEast = BigDecimal.valueOf(1000);
        BigDecimal expectedWest = BigDecimal.valueOf(10);
        List<BoundingBox> bboxes = Arrays.asList(
            BoundingBox.builder().northBoundLatitude(expectedNorth.toPlainString()).southBoundLatitude("100").eastBoundLongitude("110").westBoundLongitude("100").build(),
            BoundingBox.builder().northBoundLatitude("210").southBoundLatitude(expectedSouth.toPlainString()).eastBoundLongitude("110").westBoundLongitude("200").build(),
            BoundingBox.builder().northBoundLatitude("310").southBoundLatitude("300").eastBoundLongitude(expectedEast.toPlainString()).westBoundLongitude("300").build(),
            BoundingBox.builder().northBoundLatitude("410").southBoundLatitude("400").eastBoundLongitude("110").westBoundLongitude(expectedWest.toPlainString()).build()
        );

        // when
        Optional<BoundingBox> actual = networkIndexingService.getEnvelope(bboxes);

        // then
        assertTrue(actual.isPresent());
        assertThat(actual.get().getNorthBoundLatitude(), is(closeTo(expectedNorth, precision)));
        assertThat(actual.get().getSouthBoundLatitude(), is(closeTo(expectedSouth, precision)));
        assertThat(actual.get().getEastBoundLongitude(), is(closeTo(expectedEast, precision)));
        assertThat(actual.get().getWestBoundLongitude(), is(closeTo(expectedWest, precision)));
    }
    @Test
    public void getCorrectEmptyCombinedEnvelope() {
        // given
        List<BoundingBox> bboxes = new ArrayList<>();

        // when
        Optional<BoundingBox> actual = networkIndexingService.getEnvelope(bboxes);

        // then
        assertTrue(actual.isEmpty());
    }
}
