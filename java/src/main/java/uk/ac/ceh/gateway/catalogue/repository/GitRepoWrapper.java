package uk.ac.ceh.gateway.catalogue.repository;

import com.google.common.eventbus.EventBus;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@ToString(exclude = {"repo", "documentInfoMapper"})
@Service
public class GitRepoWrapper {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;

    private final BundledReaderService<MetadataDocument> bundledReader;

    private final EventBus eventBus;

    public GitRepoWrapper(
            DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> documentInfoMapper,
            BundledReaderService<MetadataDocument> bundledReader,
            EventBus eventBus
    ) {
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
        this.bundledReader = bundledReader;
        this.eventBus = eventBus;
        log.info("Creating {}", this);
    }

    @SneakyThrows
    public void save(CatalogueUser user, String id, String message, MetadataInfo metadataInfo, DataWriter dataWriter) {
        Optional<MonitoringFacility> preUpdateFacility = getMonitoringFacility(id);
        repo.submitData(String.format("%s.meta", id), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
            .submitData(String.format("%s.raw", id), dataWriter)
            .commit(user, message);
        Optional<MonitoringFacility> postUpdateFacility = getMonitoringFacility(id);
        Optional<FacilityBelongToRemovedEvent> facilityDeletedEvent = getFacilityUpdatedEvent(preUpdateFacility, postUpdateFacility);
        if(facilityDeletedEvent.isPresent()){
            eventBus.post(facilityDeletedEvent.get());
        }
    }

    public DataRevision<CatalogueUser> delete(CatalogueUser user, String id) throws DataRepositoryException {
        Optional<FacilityBelongToRemovedEvent> facilityDeletedEvent = getFacilityDeletedEvent(id);
        DataRevision<CatalogueUser> revision = repo.deleteData(id + ".meta")
                .deleteData(id + ".raw")
                .commit(user, String.format("delete document: %s", id));
        if(facilityDeletedEvent.isPresent()){
            eventBus.post(facilityDeletedEvent.get());
        }
        return revision;
    }

    @SneakyThrows
    private Optional<FacilityBelongToRemovedEvent> getFacilityDeletedEvent(String facilityId) {
        MetadataDocument document = bundledReader.readBundle(facilityId);
        if(document instanceof MonitoringFacility facility) {
            List<String> belongToIds = getBelongToIds(facility);
            if(belongToIds.size() > 0){
                return Optional.of(new FacilityBelongToRemovedEvent(facilityId, belongToIds));
            }
        }
        return Optional.empty();
    }

    @SneakyThrows
    /** This takes two versions of a monitoring facility.  They represent the state of the monitoring facility before and
     * after it was updated.  The monitoring facility before the update may not exist if the incoming is a new one.  If
     * any 'belongTo' relationships have been removed from the preUpdate document, then a new
     * FacilityBelongToRemovedEvent needs firing that contains the facilityId and the list of network ids that are no
     * longer referenced by this facility.
     */
    private Optional<FacilityBelongToRemovedEvent> getFacilityUpdatedEvent(Optional<MonitoringFacility> preUpdate, Optional<MonitoringFacility> postUpdate) {
        if(preUpdate.isPresent() && postUpdate.isPresent()) {
            List<String> preBelongToIds = getBelongToIds(preUpdate.get());
            List<String> postBelongToIds = getBelongToIds(postUpdate.get());
            preBelongToIds.removeAll(postBelongToIds);
            if(preBelongToIds.size() > 0 ) {
                return Optional.of(new FacilityBelongToRemovedEvent(preUpdate.get().getId(), preBelongToIds));
            }
        }
        return Optional.empty();
    }

    private Optional<MonitoringFacility> getMonitoringFacility(String id) {
        MetadataDocument document = null;
        try {
            document = bundledReader.readBundle(id);
        } catch (IOException e) {
            // It is correct that the document may not yet exist,
            // if the exception is for any other reason then throw it
            if(!"The file does not exist".equals(e.getMessage())) {
                throw new RuntimeException(e);
            }
        } catch (PostProcessingException e) {
            throw new RuntimeException(e);
        }
        if(document != null && document instanceof MonitoringFacility facility) {
            return Optional.of(facility);
        }
        return Optional.empty();
    }

    /**
     * This will return the list of ids of documents a monitoring facility 'belongsTo'
     * @param facility the monitoring facility
     * @return a list of ids of documents that the facility 'belongsTo'
     */
    private List<String> getBelongToIds(MonitoringFacility facility) {
        List<String> belongingIds = new ArrayList<String>();
        facility.getRelationships().stream().forEach(r -> {
            if (r.getRelation().equals(Ontology.BELONGS_TO.getURI())) {
                belongingIds.add(r.getTarget());
            }
        });
        return belongingIds;
    }
}
