package uk.ac.ceh.gateway.catalogue.repository;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.services.FacilityEventService;

import java.util.Optional;

@Slf4j
@ToString(exclude = {"repo", "documentInfoMapper"})
@Service
public class GitRepoWrapper {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final FacilityEventService facilityEventService;

    public GitRepoWrapper(
            DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> documentInfoMapper,
            FacilityEventService facilityEventService
    ) {
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
        this.facilityEventService = facilityEventService;
        log.info("Creating {}", this);
    }

    @SneakyThrows
    public void save(CatalogueUser user, String id, String message, MetadataInfo metadataInfo, DataWriter dataWriter) {
        Optional<MonitoringFacility> preUpdateFacility = facilityEventService.getMonitoringFacility(id);
        repo.submitData(String.format("%s.meta", id), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
            .submitData(String.format("%s.raw", id), dataWriter)
            .commit(user, message);
        Optional<MonitoringFacility> postUpdateFacility = facilityEventService.getMonitoringFacility(id);
        facilityEventService.postRemovedEvent(preUpdateFacility, postUpdateFacility);
    }

    public DataRevision<CatalogueUser> delete(CatalogueUser user, String id) throws DataRepositoryException {
        Optional<FacilityBelongToRemovedEvent> facilityDeletedEvent = facilityEventService.getFacilityDeletedEvent(id);
        DataRevision<CatalogueUser> revision = repo.deleteData(id + ".meta")
                .deleteData(id + ".raw")
                .commit(user, String.format("delete document: %s", id));
        facilityEventService.postDeletedEvent(facilityDeletedEvent);
        return revision;
    }

}
