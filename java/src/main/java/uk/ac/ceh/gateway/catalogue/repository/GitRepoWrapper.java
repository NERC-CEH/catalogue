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
        repo.submitData(String.format("%s.meta", id), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
            .submitData(String.format("%s.raw", id), dataWriter)
            .commit(user, message);
    }

    public DataRevision<CatalogueUser> delete(CatalogueUser user, String id) throws DataRepositoryException {
        Optional<FacilityDeletedEvent> facilityDeletedEvent = getFacilityDeletedEvent(id);
        DataRevision<CatalogueUser> revision = repo.deleteData(id + ".meta")
                .deleteData(id + ".raw")
                .commit(user, String.format("delete document: %s", id));
        if(facilityDeletedEvent.isPresent()){
            eventBus.post(facilityDeletedEvent.get());
        }
        return revision;
    }

    @SneakyThrows
    private Optional<FacilityDeletedEvent> getFacilityDeletedEvent(String facilityId) {
        List<String> belongingIds = new ArrayList<String>();
        MetadataDocument document = bundledReader.readBundle(facilityId);
        if(document instanceof MonitoringFacility facility) {
            facility.getRelationships().stream().forEach(r -> {
                if (r.getRelation().equals(Ontology.BELONGS_TO.getURI())) {
                    belongingIds.add(r.getTarget());
                }
            });
        }
        if(belongingIds.size() > 0){
            return Optional.of(new FacilityDeletedEvent(facilityId, belongingIds));
        }
        return Optional.empty();
    }
}
