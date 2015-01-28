package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import static java.lang.String.format;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Service("permission")
public class PermissionService {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;

    @Autowired
    public PermissionService(DataRepository<CatalogueUser> repo, DocumentInfoMapper documentInfoMapper) {
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
    }
    
    public boolean toAccess(CatalogueUser user, String file, String permission) throws IOException {
        return toAccess(user, file, repo.getLatestRevision().getRevisionID(), permission);
    }
    
    public boolean toAccess(CatalogueUser user, String file, String revision, String permission) throws IOException {
        Optional<MetadataInfo> metadataInfo = Optional.ofNullable(
            documentInfoMapper.readInfo(repo.getData(revision, format("%s.meta", file)).getInputStream()));
        
        return metadataInfo.isPresent() 
            && (
                documentIsPublic(metadataInfo.get(), permission)
                || 
                userCanAccess(user, metadataInfo.get(), permission)
            );
        
    }
    
    private boolean documentIsPublic(MetadataInfo metadataInfo, String permission) {
        return metadataInfo.getState().equalsIgnoreCase("public") && permission.equalsIgnoreCase("document_read");
    }
    
    private boolean userCanAccess(CatalogueUser user, MetadataInfo metadataInfo, String permission) {
        return user.isPublic() && metadataInfo.getState().equalsIgnoreCase("public") && permission.equalsIgnoreCase("document_read");
    }
    
    private DataRevision<CatalogueUser> lastCommit(String file) throws DataRepositoryException {
        List<DataRevision<CatalogueUser>> revisions =  repo.getRevisions(file);
        return revisions.get(revisions.size()-1);
    }

}