package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.eventbus.EventBus;
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
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;
import uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.repository.FacilityBelongToRemovedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@DisplayName("FacilityEventService")
@ExtendWith(MockitoExtension.class)
class FacilityEventServiceTest {

    @Mock
    BundledReaderService<MetadataDocument> bundledReader;
    @Mock
    EventBus eventBus;
    @Captor ArgumentCaptor<FacilityBelongToRemovedEvent> postCapture;
    @InjectMocks
    private FacilityEventService facilityEventService;

    @Test
    @SneakyThrows
    void getFacilityDeletedEvent() {
        // given
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        MonitoringFacility f1 = getMonitoringFacility("f1", "50,50", n1);
        Optional<FacilityBelongToRemovedEvent> expected = Optional.of(new FacilityBelongToRemovedEvent(f1.getId(), Arrays.asList(n1.getId())));

        // when
        when(bundledReader.readBundle(f1.getId())).thenReturn(f1);
        Optional<FacilityBelongToRemovedEvent> actual = facilityEventService.getFacilityDeletedEvent(f1.getId());

        // then
        assertTrue(actual.isPresent());
        assertEquals(actual.get().getFacilityId(), expected.get().getFacilityId());
        assertThat(actual.get().getBelongToFilenames(), is(expected.get().getBelongToFilenames()));
    }

    @Test
    @SneakyThrows
    void getFacilityDeletedEventNoRelationship() {
        // given
        MonitoringFacility f1 = new MonitoringFacility();
        f1.setId("f1");

        // when
        when(bundledReader.readBundle(f1.getId())).thenReturn(f1);
        Optional<FacilityBelongToRemovedEvent> actual = facilityEventService.getFacilityDeletedEvent(f1.getId());

        // then
        assertTrue(actual.isEmpty());
    }

    @Test
    @SneakyThrows
    void getMonitoringFacility() {
        // given
        MonitoringFacility f1 = new MonitoringFacility();
        f1.setId("f1");
        Optional<MonitoringFacility> expected = Optional.of(f1);

        // when
        when(bundledReader.readBundle(f1.getId())).thenReturn(f1);
        Optional<MonitoringFacility> actual = facilityEventService.getMonitoringFacility(f1.getId());

        // then
        assertTrue(actual.isPresent());
        assertEquals(actual.get().getId(), expected.get().getId());
    }

    @Test
    @SneakyThrows
    void getMonitoringFacilityWrongType() {
        // given
        MonitoringNetwork n1 = new MonitoringNetwork();
        n1.setId("n1");

        // when
        when(bundledReader.readBundle(n1.getId())).thenReturn(n1);
        Optional<MonitoringFacility> actual = facilityEventService.getMonitoringFacility(n1.getId());

        // then
        assertTrue(actual.isEmpty());
    }

    @Test
    @SneakyThrows
    void getMonitoringFacilityNoMatch() {
        // given
        String lookup = "f1";

        // when
        when(bundledReader.readBundle(lookup)).thenReturn(null);
        Optional<MonitoringFacility> actual = facilityEventService.getMonitoringFacility(lookup);

        // then
        assertTrue(actual.isEmpty());
    }

    @Test
    void postRemovedEvent() {
        // given
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        MonitoringFacility f1 = getMonitoringFacility("f1", "50,50", n1);
        MonitoringFacility f2 = new MonitoringFacility();
        f2.setId("f2");
        Optional<MonitoringFacility> preUpdateFacility = Optional.of(f1);
        Optional<MonitoringFacility> postUpdateFacility = Optional.of(f2);
        Optional<FacilityBelongToRemovedEvent> expected = Optional.of(new FacilityBelongToRemovedEvent(f1.getId(), Arrays.asList(n1.getId())));

        // when
        facilityEventService.postRemovedEvent(preUpdateFacility, postUpdateFacility);

        // then
        verify(eventBus, times(1)).post(postCapture.capture());

        // then
        assertEquals(postCapture.getValue().getFacilityId(), expected.get().getFacilityId());
        assertEquals(postCapture.getValue().getBelongToFilenames().get(0), expected.get().getBelongToFilenames().get(0));
        assertEquals(postCapture.getValue().getBelongToFilenames().size(), 1);
    }

    @Test
    void postRemovedEventNoChange() {
        // given
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        MonitoringFacility f1 = getMonitoringFacility("f1", "50,50", n1);
        MonitoringFacility f2 = getMonitoringFacility("f2", "100,100", n1);
        Optional<MonitoringFacility> preUpdateFacility = Optional.of(f1);
        Optional<MonitoringFacility> postUpdateFacility = Optional.of(f2);
        Optional<FacilityBelongToRemovedEvent> expected = Optional.of(new FacilityBelongToRemovedEvent(f1.getId(), Arrays.asList(n1.getId())));

        // when
        facilityEventService.postRemovedEvent(preUpdateFacility, postUpdateFacility);

        // then
        verify(eventBus, times(0)).post(postCapture.capture());
    }

    @Test
    void postDeletedEvent() {
        // given
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        MonitoringFacility f1 = getMonitoringFacility("f1", "50,50", n1);
        Optional<FacilityBelongToRemovedEvent> expected = Optional.of(new FacilityBelongToRemovedEvent(f1.getId(), Arrays.asList(n1.getId())));

        // when
        facilityEventService.postDeletedEvent(expected);

        // then
        verify(eventBus, times(1)).post(postCapture.capture());
        assertEquals(postCapture.getValue().getFacilityId(), expected.get().getFacilityId());
        assertEquals(postCapture.getValue().getBelongToFilenames().size(), 1);
        assertEquals(postCapture.getValue().getBelongToFilenames().get(0), expected.get().getBelongToFilenames().get(0));
    }

    @Test
    void postDeletedEventEmpty() {
        // given
        Optional<FacilityBelongToRemovedEvent> expected = Optional.empty();

        // when
        facilityEventService.postDeletedEvent(expected);

        // then
        verify(eventBus, times(0)).post(postCapture.capture());
    }

    @Test
    public void facilityRelationshipsBelongsTo() {
        // given
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        MonitoringFacility f1 = getMonitoringFacility("f1", "50,50", n1);

        // when
        List<String> actual = facilityEventService.getBelongToIds(f1);

        // then
        assertEquals(actual.size(), 1);
        assertEquals(actual.get(0), n1.getId());
    }

    @Test
    public void facilityNoRelationshipsGetBelongsTo() {
        // given
        MonitoringNetwork n1 = getMonitoringNetwork("n1");
        MonitoringFacility f1 = new MonitoringFacility();

        // when
        List<String> actual = facilityEventService.getBelongToIds(f1);

        // then
        assertEquals(actual.size(), 0);
    }
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
}
