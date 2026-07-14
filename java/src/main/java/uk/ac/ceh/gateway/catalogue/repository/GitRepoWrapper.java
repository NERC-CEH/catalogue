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
import uk.ac.ceh.gateway.catalogue.model.MetadataConflictException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.services.FacilityEventService;

import java.util.List;
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

    // Note: this delegates via a self-invocation ("this.save(...)") to the 7-arg overload below. Spring's
    // proxy-based AOP does not intercept self-invocations, so the 7-arg method's own @CacheEvict would NOT
    // fire when reached this way. This 5-arg method therefore carries its own identical @Caching(evict=...)
    // block so external callers (e.g. GitDocumentRepository) going through the proxy still get the eviction.
    @Caching(evict = {
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.meta'"),
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.raw'"),
        @CacheEvict(value = CachedDataRepository.DOC_REVISION_CACHE, key = "#id + '.meta'"),
        @CacheEvict(value = CachedDataRepository.REVISION_ID_CACHE, allEntries = true)
    })
    public void save(CatalogueUser user, String id, String message, MetadataInfo metadataInfo, DataWriter dataWriter) throws DataRepositoryException {
        save(user, id, message, metadataInfo, dataWriter, null, null);
    }

    @Caching(evict = {
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.meta'"),
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.raw'"),
        @CacheEvict(value = CachedDataRepository.DOC_REVISION_CACHE, key = "#id + '.meta'"),
        @CacheEvict(value = CachedDataRepository.REVISION_ID_CACHE, allEntries = true)
    })
    public void save(CatalogueUser user, String id, String message, MetadataInfo metadataInfo,
                     DataWriter dataWriter, String expectedRevision, MetadataDocument submittedForEcho) throws DataRepositoryException {
        // Guard read-check-write as one unit: two concurrent saves must not both pass the check before
        // either commits. The check reads the revision fresh (uncached) so it is always authoritative.
        synchronized (this) {
            if (expectedRevision != null) {
                String current = currentDocumentRevision(id);
                if (!expectedRevision.equals(current)) {
                    throw new MetadataConflictException(
                        "This record was changed by another user since you opened it.", submittedForEcho);
                }
            }
            Optional<MonitoringFacility> preUpdateFacility = facilityEventService.getMonitoringFacility(id);
            DataRevision<CatalogueUser> revision = repo.submitData(String.format("%s.meta", id), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
                .submitData(String.format("%s.raw", id), dataWriter)
                .commit(user, message);
            // Read the post-update facility at the commit's own revision: this method's @CacheEvict has not yet run,
            // so the cached "latest" still points at the pre-commit revision and would return stale (or missing) content.
            Optional<MonitoringFacility> postUpdateFacility = facilityEventService.getMonitoringFacility(id, revision.getRevisionID());
            facilityEventService.postRemovedEvent(preUpdateFacility, postUpdateFacility);
        }
    }

    private String currentDocumentRevision(String id) throws DataRepositoryException {
        List<DataRevision<CatalogueUser>> revisions = repo.getRevisions(id + ".meta");
        return revisions.isEmpty() ? null : revisions.get(0).getRevisionID();
    }

    @Caching(evict = {
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.meta'"),
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.raw'"),
        @CacheEvict(value = CachedDataRepository.DOC_REVISION_CACHE, key = "#id + '.meta'"),
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
