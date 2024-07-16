package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.eventbus.EventBus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.repository.FacilityBelongToRemovedEvent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This service is principally used by the GitRepoWrapper to orchestrate the firing of Events
 * when MonitoringFacility documents are edited.
 */
@Slf4j
@Service
public class FacilityEventService {
    private final BundledReaderService<MetadataDocument> bundledReader;
    private final EventBus eventBus;

    public FacilityEventService(BundledReaderService<MetadataDocument> bundledReader, EventBus eventBus) {
        this.bundledReader = bundledReader;
        this.eventBus = eventBus;
    }

    @SneakyThrows
    public Optional<FacilityBelongToRemovedEvent> getFacilityDeletedEvent(String facilityId) {
        MetadataDocument document = bundledReader.readBundle(facilityId);
        if(document instanceof MonitoringFacility facility) {
            List<String> belongToIds = getBelongToIds(facility);
            if(belongToIds.size() > 0){
                return Optional.of(new FacilityBelongToRemovedEvent(facilityId, belongToIds));
            }
        }
        return Optional.empty();
    }

    public Optional<MonitoringFacility> getMonitoringFacility(String id) {
        MetadataDocument document = null;
        try {
            document = bundledReader.readBundle(id);
        } catch (IOException e) {
            // It is correct that the document may not yet exist,
            // if the exception is for any other reason then throw it
            if(!(e instanceof GitFileNotFoundException)) {
                throw new RuntimeException(e);
            }
        } catch (PostProcessingException e) {
            throw new RuntimeException(e);
        }
        if(document instanceof MonitoringFacility facility) {
            return Optional.of(facility);
        }
        return Optional.empty();
    }

    /**
     * Fire a FacilityBelongToRemovedEvent if a pre-existing relationship to a network is removed from a facility.
     * @param preUpdateFacility The facility before it was edited/created, it won't exist if a new facility is being created
     * @param postUpdateFacility The facility after it was edited/created
     */
    public void postRemovedEvent(Optional<MonitoringFacility> preUpdateFacility, Optional<MonitoringFacility> postUpdateFacility) {
        getFacilityRemovedEvent(preUpdateFacility, postUpdateFacility).ifPresent(eventBus::post);
    }

    public void postDeletedEvent(Optional<FacilityBelongToRemovedEvent> facilityDeletedEvent){
        facilityDeletedEvent.ifPresent(eventBus::post);
    }

    /** This takes two versions of a monitoring facility.  They represent the state of the monitoring facility before and
     * after it was updated.  The monitoring facility before the update may not exist if the incoming is a new one.  If
     * any 'belongTo' relationships have been removed from the preUpdate document, then a new
     * FacilityBelongToRemovedEvent needs firing that contains the facilityId and the list of network ids that are no
     * longer referenced by this facility.
     */
    private Optional<FacilityBelongToRemovedEvent> getFacilityRemovedEvent(Optional<MonitoringFacility> preUpdate, Optional<MonitoringFacility> postUpdate) {
        if(preUpdate.isPresent() && postUpdate.isPresent()) {
            List<String> preBelongToIds = getBelongToIds(preUpdate.get());
            List<String> postBelongToIds = getBelongToIds(postUpdate.get());
            List<String> networksToUpdate = preBelongToIds.stream()
                .filter(element -> !postBelongToIds.contains(element))
                .toList();
            if(networksToUpdate.size() > 0 ) {
                return Optional.of(new FacilityBelongToRemovedEvent(preUpdate.get().getId(), networksToUpdate));
            }
        }
        return Optional.empty();
    }

    /**
     * This will return the list of ids of documents a monitoring facility 'belongsTo'
     * @param facility the monitoring facility
     * @return a list of ids of documents that the facility 'belongsTo'
     */
    protected List<String> getBelongToIds(MonitoringFacility facility) {
            return facility.getRelationships().stream()
                .filter(r -> r.getRelation().equals(Ontology.BELONGS_TO.getURI()))
                .map(r -> r.getTarget())
                .toList();
        }
}
