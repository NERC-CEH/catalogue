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
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb2.TDB2Factory;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.geometry.Geometry;
import uk.ac.ceh.gateway.catalogue.geometry.PointGeometry;
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

import static org.apache.jena.rdf.model.ResourceFactory.createResource;
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
    private final BigDecimal precision = BigDecimal.valueOf((PointGeometry.POINT_PRECISION + 0.0000001));
    private final String commitMessageTemplate = NetworkIndexingService.COMMIT_MESSAGE_TEMPLATE;
    private static final String REVISION = "rev123";

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
                    new Relationship(Ontology.DCTERMS_ISPARTOF.getURI(), network.getUri())
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
        when(bundledReader.readBundle(f1.getId(), REVISION)).thenReturn(f1);
        when(bundledReader.readBundle(f2.getId(), REVISION)).thenReturn(f2);
        when(bundledReader.readBundle(f3.getId(), REVISION)).thenReturn(f3);
        when(bundledReader.readBundle(f4.getId(), REVISION)).thenReturn(f4);
        when(bundledReader.readBundle(n1.getId(), REVISION)).thenReturn(n1);
        when(lookupService.inverseRelationships(n1.getUri(), Ontology.DCTERMS_ISPARTOF.getURI())).thenReturn(links);

        networkIndexingService.indexDocuments(toIndex, REVISION);
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
    @DisplayName("Reads the triggering commit revision, never the cache-stale latest")
    @SneakyThrows
    void indexDocumentsReadsAtEventRevision() {
        // given a freshly-committed facility: the no-revision readBundle would resolve the stale pre-commit
        // HEAD (the write-path @CacheEvict has not run yet) and throw GitFileNotFoundException for the new file.
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        MonitoringFacility f1 = getMonitoringFacility("f1", "50,50", n1);
        List<Link> links = List.of(Link.builder().geometry(f1.getGeometry().getGeometryString()).href(f1.getId()).build());

        when(bundledReader.readBundle(f1.getId(), REVISION)).thenReturn(f1);
        when(bundledReader.readBundle(n1.getId(), REVISION)).thenReturn(n1);
        when(lookupService.inverseRelationships(n1.getUri(), Ontology.DCTERMS_ISPARTOF.getURI())).thenReturn(links);

        // when
        networkIndexingService.indexDocuments(List.of(f1.getId()), REVISION);

        // then every read is pinned to the event revision; the stale latest overload is never touched
        verify(bundledReader).readBundle(f1.getId(), REVISION);
        verify(bundledReader, never()).readBundle(anyString());
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
        when(lookupService.inverseRelationships(n1.getUri(), Ontology.DCTERMS_ISPARTOF.getURI())).thenReturn(links);

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
    @DisplayName("Rebuilds the bounding box from a real Jena lookup when a facility has several identifiers")
    @SneakyThrows
    void updatesBoundingBoxWhenFacilityHasMultipleIdentifiers() {
        // given a facility indexed with a geometry AND more than one dcterms:identifier -- every record
        // gets one identifier for its own id plus one per resourceIdentifier. This drives the real
        // JenaLookupService rather than a mock, because the defect being guarded against was the query
        // emitting the GeoJSON once per identifier: a hand-fed geometry string could never catch it.
        // The malformed string reached Geometry.builder() here and threw "Error parsing geometry JSON",
        // aborting the network bounding-box rebuild (dri-one #279).
        double lon = 50.0;
        double lat = 60.0;
        String networkUri = "http://network/n1";
        String facilityUri = "http://facility/f1";
        String geoJson = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]}}";

        Dataset jenaTdb = TDB2Factory.createDataset();
        jenaTdb.begin(ReadWrite.WRITE);
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource(facilityUri), Ontology.DCTERMS_TITLE, "Monitoring Facility");
        triples.add(createResource(facilityUri), Ontology.METADATA_STATUS, "published");
        triples.add(createResource(facilityUri), Ontology.DCTERMS_TYPE, "monitoringFacility");
        triples.add(createResource(facilityUri), Ontology.DCTERMS_ISPARTOF, createResource(networkUri));
        triples.add(createResource(facilityUri), Ontology.SF_GEOMETRY, geoJson);
        triples.add(createResource(facilityUri), Ontology.DCTERMS_IDENTIFIER, "f1");
        triples.add(createResource(facilityUri), Ontology.DCTERMS_IDENTIFIER, "http://vocabs#UKEOF1234");
        jenaTdb.commit();

        MonitoringNetwork n1 = new MonitoringNetwork();
        n1.setId("n1");
        n1.setUri(networkUri);
        MonitoringFacility f1 = getMonitoringFacility("f1", lon + "," + lat, n1);

        // the facility node URI contains the facility id, so addFacility correctly sees it as already
        // present in the Jena results and does not double-count it
        BundledReaderService<MetadataDocument> reader = mock(BundledReaderService.class);
        when(reader.readBundle(f1.getId(), REVISION)).thenReturn(f1);
        when(reader.readBundle(networkUri, REVISION)).thenReturn(n1);
        DocumentRepository repository = mock(DocumentRepository.class);

        NetworkIndexingService service = new NetworkIndexingService(
            mock(DocumentListingService.class),
            reader,
            repository,
            new JenaLookupService(jenaTdb)
        );

        // when
        service.indexDocuments(List.of(f1.getId()), REVISION);

        // then the network is saved with the facility's bounding box, not aborted mid-rebuild
        ArgumentCaptor<MonitoringNetwork> saved = ArgumentCaptor.forClass(MonitoringNetwork.class);
        verify(repository).save(any(CatalogueUser.class), saved.capture(), anyString());
        BoundingBox actual = saved.getValue().getBoundingBox();

        assertThat(actual.getNorthBoundLatitude(), is(closeTo(BigDecimal.valueOf(lat), precision)));
        assertThat(actual.getSouthBoundLatitude(), is(closeTo(BigDecimal.valueOf(lat), precision)));
        assertThat(actual.getEastBoundLongitude(), is(closeTo(BigDecimal.valueOf(lon), precision)));
        assertThat(actual.getWestBoundLongitude(), is(closeTo(BigDecimal.valueOf(lon), precision)));
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
