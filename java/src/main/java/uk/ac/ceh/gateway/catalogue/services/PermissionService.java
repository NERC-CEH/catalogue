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
            log.debug("revision from dataDocument: {}", data.getRevision());
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
                authorCanAccess(user, file);
        }
        return toReturn;
    }
    
    private boolean isPubliclyViewable(MetadataInfo metadataInfo, Permission requested) {
        log.debug("isPubliclyViewable");
        return metadataInfo.getState().equalsIgnoreCase("public") && VIEW.equals(requested);
    }
    public boolean userCanAccess(CatalogueUser user, MetadataInfo metadataInfo) {
        return user.isPublic() && metadataInfo.getState().equalsIgnoreCase("public");
    }
    
    private boolean userCanAccess(CatalogueUser user, MetadataInfo metadataInfo, Permission requested) {
        log.debug("userCanAccess");
        List<String> permittedIdentities = metadataInfo.getIdentities(requested);
        log.debug("user requesting access: {}", user);
        if ( !user.isPublic()) {
            Optional<String> permittedGroup = groupStore.getGroups(user)
                .stream()
                .map(Group::getName)
                .filter(name -> permittedIdentities.contains(name.toLowerCase()))
                .findFirst();
            return permittedIdentities.contains(user.getUsername()) || permittedGroup.isPresent();
        } else {
            return false;
        }
    }
    
    private boolean authorCanAccess(CatalogueUser user, String file) throws DataRepositoryException {
        log.debug("authorCanAccess");
        
        Optional<CatalogueUser> author = repo.getRevisions(format("%s.meta", file))
            .stream()
            .findFirst()
            .map(DataRevision<CatalogueUser>::getAuthor);
        
        if (author.isPresent()) {
            log.debug("author found, about to check permission");
            return user.equals(author.get());
        } else {
            log.debug("author absent");
            return false;
        }
    }
}