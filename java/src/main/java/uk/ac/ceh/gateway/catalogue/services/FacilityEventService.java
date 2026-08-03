package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.eventbus.EventBus;
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

    /**
     * Asked by {@link uk.ac.ceh.gateway.catalogue.repository.GitRepoWrapper#delete} <em>before</em> it removes
     * anything, so this must not fail for a document that cannot be read — otherwise such a document cannot be
     * deleted at all. It goes through {@link #getMonitoringFacility} rather than reading directly for exactly
     * that reason; see the unreadable-document note there.
     */
    public Optional<FacilityBelongToRemovedEvent> getFacilityDeletedEvent(String facilityId) {
        return getMonitoringFacility(facilityId)
            .map(this::getBelongToIds)
            .filter(belongToIds -> !belongToIds.isEmpty())
            .map(belongToIds -> new FacilityBelongToRemovedEvent(facilityId, belongToIds));
    }

    public Optional<MonitoringFacility> getMonitoringFacility(String id) {
        return toMonitoringFacility(id, () -> bundledReader.readBundle(id));
    }

    /**
     * Read a facility at an explicit revision. Used for the post-commit read in {@code GitRepoWrapper.save}: the
     * triggering commit's {@code @CacheEvict} has not yet run, so the cached "latest" still resolves to the
     * pre-commit revision and would return stale (or, for a new document, missing) content.
     */
    public Optional<MonitoringFacility> getMonitoringFacility(String id, String revision) {
        return toMonitoringFacility(id, () -> bundledReader.readBundle(id, revision));
    }

    private Optional<MonitoringFacility> toMonitoringFacility(String id, BundleReader reader) {
        MetadataDocument document = null;
        try {
            document = reader.read();
        } catch (IOException e) {
            // It is correct that the document may not yet exist,
            // if the exception is for any other reason then throw it
            if(!(e instanceof GitFileNotFoundException)) {
                throw new RuntimeException(e);
            }
        } catch (PostProcessingException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            // The document's stored documentType has no registered class, so it cannot be deserialised. That
            // also settles the only question asked here: MonitoringFacility *is* a registered type, so a
            // document whose type does not resolve cannot be one. Answering "not a facility" is therefore
            // correct rather than merely tolerant, and it lets callers carry on — without it, deleting such a
            // document was impossible, because the delete path asks this before removing anything.
            // Retired document types leave records like this behind; see NERC-CEH/dri-one#239.
            log.warn("Could not read {} to determine whether it is a monitoring facility: {}", id, e.getMessage());
        }
        if(document instanceof MonitoringFacility facility) {
            return Optional.of(facility);
        }
        return Optional.empty();
    }

    @FunctionalInterface
    private interface BundleReader {
        MetadataDocument read() throws IOException, PostProcessingException;
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
                .filter(r -> r.getRelation().equals(Ontology.DCTERMS_ISPARTOF.getURI()))
                .map(r -> r.getTarget())
                .toList();
        }
}
