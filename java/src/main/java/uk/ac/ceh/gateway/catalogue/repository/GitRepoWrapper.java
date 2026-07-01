package uk.ac.ceh.gateway.catalogue.repository;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
        log.info("Creating");
    }

    @Caching(evict = {
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.meta'"),
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.raw'"),
        @CacheEvict(value = CachedDataRepository.REVISION_ID_CACHE, allEntries = true)
    })
    public void save(CatalogueUser user, String id, String message, MetadataInfo metadataInfo, DataWriter dataWriter) throws DataRepositoryException {
        Optional<MonitoringFacility> preUpdateFacility = facilityEventService.getMonitoringFacility(id);
        DataRevision<CatalogueUser> revision = repo.submitData(String.format("%s.meta", id), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
            .submitData(String.format("%s.raw", id), dataWriter)
            .commit(user, message);
        // Read the post-update facility at the commit's own revision: this method's @CacheEvict has not yet run,
        // so the cached "latest" still points at the pre-commit revision and would return stale (or missing) content.
        Optional<MonitoringFacility> postUpdateFacility = facilityEventService.getMonitoringFacility(id, revision.getRevisionID());
        facilityEventService.postRemovedEvent(preUpdateFacility, postUpdateFacility);
    }

    @Caching(evict = {
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.meta'"),
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.raw'"),
        @CacheEvict(value = CachedDataRepository.REVISION_ID_CACHE, allEntries = true)
    })
    public DataRevision<CatalogueUser> delete(CatalogueUser user, String id) throws DataRepositoryException {
        Optional<FacilityBelongToRemovedEvent> facilityDeletedEvent = facilityEventService.getFacilityDeletedEvent(id);
        DataRevision<CatalogueUser> revision = repo.deleteData(id + ".meta")
                .deleteData(id + ".raw")
                .commit(user, String.format("delete document: %s", id));
        facilityEventService.postDeletedEvent(facilityDeletedEvent);
        return revision;
    }

}
