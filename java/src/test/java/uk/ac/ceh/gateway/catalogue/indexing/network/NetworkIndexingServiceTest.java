package uk.ac.ceh.gateway.catalogue.indexing.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import static org.mockito.Mockito.*;

@DisplayName("NetworkIndexing")
@ExtendWith(MockitoExtension.class)
class NetworkIndexingServiceTest {
    @Mock DocumentListingService listingService;
    @Mock BundledReaderService<MetadataDocument> bundledReader;
    @Mock DocumentRepository documentRepository;
    @Mock JenaLookupService lookupService;
    @Captor ArgumentCaptor<CatalogueUser> userCaptor;
    @Captor ArgumentCaptor<MonitoringNetwork> networkDocCaptor;
    @Captor ArgumentCaptor<String> commitMessageCaptor;

    @InjectMocks
    private NetworkIndexingService service;

    private MonitoringFacility getMonitoringFacility(String id, String coords) {
        MonitoringFacility mf = new MonitoringFacility();
        mf.setId(id);
        String geojsonPolygon = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[" + coords + "]}}";
        mf.setGeometry(Geometry.builder()
                .geometryString(geojsonPolygon)
                .build()
        );
        return mf;
    }

    private MonitoringNetwork getMonitoringNetwork(String id) {
        MonitoringNetwork mn = new MonitoringNetwork();
        mn.setId(id);
        mn.setUri(id);
        return mn;
    }

    @Test
    void addFacilityOutsideNetwork() {
        BigDecimal expectedNorth = BigDecimal.valueOf(200);
        BigDecimal expectedSouth = BigDecimal.valueOf(0);
        BigDecimal expectedEast = BigDecimal.valueOf(200);
        BigDecimal expectedWest = BigDecimal.valueOf(0);
        MonitoringFacility f1 = getMonitoringFacility("f1", expectedEast + "," + expectedNorth);
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        n1.setBoundingBox(BoundingBox.builder()
            .northBoundLatitude("100")
            .southBoundLatitude(expectedSouth+"")
            .eastBoundLongitude("100")
            .westBoundLongitude(expectedWest+"")
            .build()
        );
        List<String> newFacility = Arrays.asList(f1.getId());

        f1.setRelationships(
            com.google.common.collect.Sets.newHashSet(
                new Relationship(Ontology.BELONGS_TO.getURI(), n1.getUri())
            )
        );

        // when
        try {
            when(bundledReader.readBundle(f1.getId())).thenReturn(f1);
            when(bundledReader.readBundle(n1.getId())).thenReturn(n1);
            service.indexDocuments(newFacility);
            verify(documentRepository).save(userCaptor.capture(), networkDocCaptor.capture(), commitMessageCaptor.capture());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (PostProcessingException e) {
            throw new RuntimeException(e);
        } catch (DocumentIndexingException e) {
            throw new RuntimeException(e);
        } catch (DocumentRepositoryException e) {
            throw new RuntimeException(e);
        }

        // then
        BigDecimal precision = BigDecimal.valueOf((Geometry.POINT_PRECISION + 0.0000001));
        BigDecimal actualNorth = networkDocCaptor.getValue().getBoundingBox().getNorthBoundLatitude();
        BigDecimal actualSouth = networkDocCaptor.getValue().getBoundingBox().getSouthBoundLatitude();
        BigDecimal actualEast = networkDocCaptor.getValue().getBoundingBox().getEastBoundLongitude();
        BigDecimal actualWest = networkDocCaptor.getValue().getBoundingBox().getWestBoundLongitude();
        assertThat(actualNorth, is(closeTo(expectedNorth, precision)));
        assertThat(actualSouth, is(closeTo(expectedSouth, precision)));
        assertThat(actualEast, is(closeTo(expectedEast, precision)));
        assertThat(actualWest, is(closeTo(expectedWest, precision)));
    }

    @Test
    void addFacilityInsideNetwork() {
        BigDecimal expectedNorth = BigDecimal.valueOf(200);
        BigDecimal expectedSouth = BigDecimal.valueOf(0);
        BigDecimal expectedEast = BigDecimal.valueOf(200);
        BigDecimal expectedWest = BigDecimal.valueOf(0);
        MonitoringFacility f1 = getMonitoringFacility("f1", "50,50");
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        n1.setBoundingBox(BoundingBox.builder()
            .northBoundLatitude(expectedNorth+"")
            .southBoundLatitude(expectedSouth+"")
            .eastBoundLongitude(expectedEast+"")
            .westBoundLongitude(expectedWest+"")
            .build()
        );
        List<String> newFacility = Arrays.asList(f1.getId());

        f1.setRelationships(
            com.google.common.collect.Sets.newHashSet(
                new Relationship(Ontology.BELONGS_TO.getURI(), n1.getUri())
            )
        );

        // when
        try {
            when(bundledReader.readBundle(f1.getId())).thenReturn(f1);
            when(bundledReader.readBundle(n1.getId())).thenReturn(n1);
            service.indexDocuments(newFacility);

            // then
            verify(documentRepository, never()).save(userCaptor.capture(), networkDocCaptor.capture(), commitMessageCaptor.capture());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (PostProcessingException e) {
            throw new RuntimeException(e);
        } catch (DocumentIndexingException e) {
            throw new RuntimeException(e);
        } catch (DocumentRepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void unindexDocuments() {
        // given
        BigDecimal expectedNorth = BigDecimal.valueOf(60);
        BigDecimal expectedSouth = BigDecimal.valueOf(50);
        BigDecimal expectedEast = BigDecimal.valueOf(10);
        BigDecimal expectedWest = BigDecimal.valueOf(5);
        MonitoringFacility f1 = getMonitoringFacility("f1", "0,80");
        MonitoringFacility f2 = getMonitoringFacility("f2", expectedWest + "," + expectedSouth);
        MonitoringFacility f3 = getMonitoringFacility("f3", expectedEast + "," + expectedNorth);
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        List<Link> links = Arrays.asList(f1,f2,f3).stream()
                .map(f -> {
                    return Link.builder()
                            .geometry(f.getGeometry().getGeometryString())
                            .href(f.getId())
                            .build();
                })
                .collect(Collectors.toList());

        String deletedFacilityId = f1.getId();
        List<String> belongToIds = Arrays.asList(n1.getId());


        // when
        try {
            when(bundledReader.readBundle(n1.getId())).thenReturn(n1);
            when(lookupService.inverseRelationships(n1.getUri(), Ontology.BELONGS_TO.getURI())).thenReturn(links);
            service.unindexDocuments(deletedFacilityId, belongToIds);
            verify(documentRepository).save(userCaptor.capture(), networkDocCaptor.capture(), commitMessageCaptor.capture());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (PostProcessingException e) {
            throw new RuntimeException(e);
        } catch (DocumentIndexingException e) {
            throw new RuntimeException(e);
        } catch (DocumentRepositoryException e) {
            throw new RuntimeException(e);
        }

        // then
        BigDecimal precision = BigDecimal.valueOf((Geometry.POINT_PRECISION + 0.0000001));
        BigDecimal actualNorth = networkDocCaptor.getValue().getBoundingBox().getNorthBoundLatitude();
        BigDecimal actualSouth = networkDocCaptor.getValue().getBoundingBox().getSouthBoundLatitude();
        BigDecimal actualEast = networkDocCaptor.getValue().getBoundingBox().getEastBoundLongitude();
        BigDecimal actualWest = networkDocCaptor.getValue().getBoundingBox().getWestBoundLongitude();
        assertThat(actualNorth, is(closeTo(expectedNorth, precision)));
        assertThat(actualSouth, is(closeTo(expectedSouth, precision)));
        assertThat(actualEast, is(closeTo(expectedEast, precision)));
        assertThat(actualWest, is(closeTo(expectedWest, precision)));
    }

    @Test
    void relationshipRemovedFromFacility() {
        BigDecimal expectedNorth = BigDecimal.valueOf(200);
        BigDecimal expectedSouth = BigDecimal.valueOf(0);
        BigDecimal expectedEast = BigDecimal.valueOf(200);
        BigDecimal expectedWest = BigDecimal.valueOf(0);
        MonitoringFacility f1 = getMonitoringFacility("f1", "50,50");
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        n1.setBoundingBox(BoundingBox.builder()
            .northBoundLatitude(expectedNorth+"")
            .southBoundLatitude(expectedSouth+"")
            .eastBoundLongitude(expectedEast+"")
            .westBoundLongitude(expectedWest+"")
            .build()
        );
        List<String> newFacility = Arrays.asList(f1.getId());

        f1.setRelationships(
            com.google.common.collect.Sets.newHashSet(
                new Relationship(Ontology.BELONGS_TO.getURI(), n1.getUri())
            )
        );

        // when
        try {
            when(bundledReader.readBundle(f1.getId())).thenReturn(f1);
            when(bundledReader.readBundle(n1.getId())).thenReturn(n1);
            service.indexDocuments(newFacility);

            // then
            verify(documentRepository, never()).save(userCaptor.capture(), networkDocCaptor.capture(), commitMessageCaptor.capture());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (PostProcessingException e) {
            throw new RuntimeException(e);
        } catch (DocumentIndexingException e) {
            throw new RuntimeException(e);
        } catch (DocumentRepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}
