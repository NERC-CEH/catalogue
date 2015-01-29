package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import static java.lang.String.format;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import static uk.ac.ceh.gateway.catalogue.model.Permission.*;

@Service("permission")
@Slf4j
public class PermissionService {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final GroupStore<CatalogueUser> groupStore;

    @Autowired
    public PermissionService(DataRepository<CatalogueUser> repo, DocumentInfoMapper documentInfoMapper, GroupStore<CatalogueUser> groupStore) {
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
        this.groupStore = groupStore;
    }
    
    public boolean toAccess(CatalogueUser user, String file, String permission) throws IOException {
        return toAccess(user, file, repo.getLatestRevision().getRevisionID(), permission);
    }
    
    public boolean toAccess(CatalogueUser user, String file, String revision, String permission) throws IOException {
        Objects.requireNonNull(user);
        Objects.requireNonNull(file);
        Objects.requireNonNull(revision);
        boolean toReturn = false;
        Permission requested = Permission.valueOf(Objects.requireNonNull(permission).toUpperCase());
        
        DataDocument data;
        try {
            data = repo.getData(revision, format("%s.meta", file));
        } catch (DataRepositoryException ex) {
            return false;
        }
        Optional<MetadataInfo> document = Optional.ofNullable(documentInfoMapper.readInfo(data.getInputStream()));
        
        if (document.isPresent()) {
            MetadataInfo metadataInfo = document.get();
            toReturn = 
                isPubliclyViewable(metadataInfo, requested)
                || 
                userCanAccess(user, metadataInfo, requested)
                ||
                authorCanAccess(user, revision);
        }
        return toReturn;
    }
    
    private boolean isPubliclyViewable(MetadataInfo metadataInfo, Permission requested) {
        return metadataInfo.getState().equalsIgnoreCase("public") && VIEW.equals(requested);
    }
    
    private boolean userCanAccess(CatalogueUser user, MetadataInfo metadataInfo, Permission requested) {
        List<String> permittedIdentities = metadataInfo.getIdentities(requested);
        Optional<String> permittedGroup = groupStore.getGroups(user)
            .stream()
            .map(Group::getName)
            .filter(name -> permittedIdentities.contains(name.toLowerCase()))
            .findFirst();
        return permittedIdentities.contains(user.getUsername()) || permittedGroup.isPresent();
    }
    
    private boolean authorCanAccess(CatalogueUser user, String revision) throws DataRepositoryException {
        List<DataRevision<CatalogueUser>> revisions = repo.getRevisions(revision);
        if ( !revisions.isEmpty()) {
            DataRevision<CatalogueUser> get = revisions.get(revisions.size() - 1);
            return user.equals(get.getAuthor());
        } else {
            return false;
        }
    }

}