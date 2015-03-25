package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import static java.lang.String.format;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Permission;

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
        
        Optional<MetadataInfo> document = getMetadataInfo(file, revision);
        if (document.isPresent()) {
            MetadataInfo metadataInfo = document.get();
            toReturn = 
                metadataInfo.isPubliclyViewable(requested)
                || 
                metadataInfo.canAccess(requested, user, groupStore.getGroups(user));
        }
        return toReturn;
    }
    
    public boolean userCanEdit(String file) throws IOException {
        Objects.requireNonNull(file);
        CatalogueUser user = (CatalogueUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isPublic()) {
            return false;
        } else {
            return toAccess(user, file, "EDIT");
        }
    }
    
    public boolean userCanCreate() {
        return userCan((String name) -> name.equalsIgnoreCase(DocumentController.EDITOR_ROLE));
    }
    
    public boolean userCanMakePublic() {
        return userCan((String name) -> name.equalsIgnoreCase(DocumentController.PUBLISHER_ROLE));
    }
    
    public boolean userCanPublish(String file) throws IOException {
        return userCanEdit(file) || userCanMakePublic();
    }
    
    private boolean userCan(Predicate<String> filter) {
        CatalogueUser user = (CatalogueUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("principal: {}", user);
        if (user.isPublic()) {
            return false;
        } else {
            return groupStore.getGroups(user)
                .stream()
                .map(Group::getName)
                .filter(filter)
                .findFirst()
                .isPresent();
        }
    }
    
    private Optional<MetadataInfo> getMetadataInfo(String file, String revision) {
        DataDocument data;
        Optional<MetadataInfo> toReturn;
        try {
            data = repo.getData(revision, format("%s.meta", file));
            log.debug("revision from dataDocument: {}", data.getRevision());
            toReturn = Optional.ofNullable(documentInfoMapper.readInfo(data.getInputStream()));
        } catch (IOException ex) {
            toReturn = Optional.empty();
        }
        return toReturn;
    }
}