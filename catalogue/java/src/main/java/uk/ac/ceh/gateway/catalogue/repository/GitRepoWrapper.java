package uk.ac.ceh.gateway.catalogue.repository;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;

public class GitRepoWrapper {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;

    @Autowired
    public GitRepoWrapper(DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> documentInfoMapper) {
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
    }
    
    public void save(CatalogueUser user, String id, String message, MetadataInfo metadataInfo, DataWriter dataWriter) throws DataRepositoryException {
        repo.submitData(String.format("%s.meta", id), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
            .submitData(String.format("%s.raw", id), dataWriter)
            .commit(user, message);
    }
    
    public DataRevision<CatalogueUser> delete(CatalogueUser user, String id) throws DataRepositoryException {
        return repo.deleteData(id + ".meta")
            .deleteData(id + ".raw")
            .commit(user, String.format("delete document: %s", id));
    }
}