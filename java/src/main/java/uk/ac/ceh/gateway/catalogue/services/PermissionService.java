package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import static java.lang.String.format;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.controllers.DataciteController;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.PermissionDeniedException;

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
    
    public boolean toAccess(CatalogueUser user, String file, String permission) {
        try {
            return toAccess(
                user,
                file,
                repo.getLatestRevision().getRevisionID(),
                permission
            );
        } catch (DataRepositoryException ex) {
            throw new PermissionDeniedException(
                String.format(
                    "No document found for: %s",
                    file
                ),
                ex
            );
        }
    }
    
    public boolean toAccess(
        @NonNull CatalogueUser user,
        @NonNull String file,
        @NonNull String revision,
        @NonNull String permission
    ) {
        MetadataInfo document = getMetadataInfo(file, revision);
        return toAccess(user, document, permission);
    }
    
    private boolean toAccess(
        @NonNull CatalogueUser user,
        @NonNull MetadataInfo document,
        @NonNull String permission
    ) {
        Permission requested = Permission.valueOf(permission.toUpperCase());
        return 
            document.isPubliclyViewable(requested)
            || document.canAccess(requested, user, getGroupsForUser(user))
            || false;
    }
    
    public boolean userCanEdit(@NonNull String file) {
        try {
            final CatalogueUser user = getCurrentUser();
            final DataRevision<CatalogueUser> latestRevision = repo.getLatestRevision();
            final String revisionID = latestRevision.getRevisionID();
            final MetadataInfo document = getMetadataInfo(file, revisionID);
            if (user.isPublic()) {
                return false;
            } else if(userCanMakePublic(document.getCatalogue())) {
                return true;
            } else {
                return toAccess(user, document, "EDIT");
            }
        } catch (DataRepositoryException ex) {
            throw new PermissionDeniedException(
                String.format(
                    "No document found for: %s",
                    file
                ),
                ex
            );
        }
    }
    
    public boolean userCanUpload(@NonNull String file) {
        try {
            CatalogueUser user = getCurrentUser();
            DataRevision<CatalogueUser> latestRevision = repo.getLatestRevision();
            MetadataInfo document = getMetadataInfo(file, latestRevision.getRevisionID());
            return !user.isPublic() && toAccess(user, document, "UPLOAD");
        } catch (DataRepositoryException ex) {
            String message = String.format("No document found for: %s", file);
            throw new PermissionDeniedException(message, ex);
        }
    }
    public boolean userCanCreate(@NonNull String catalogue) {
        return userCan((String name) -> name.equalsIgnoreCase(
            format("role_%s_editor", catalogue)
        ));
    }
    
    public boolean userCanMakePublic(@NonNull String catalogue) {
        return userCan((String name) -> name.equalsIgnoreCase(
            format("role_%s_publisher", catalogue)
        ));
    }
    
    public boolean userCanDatacite() {
        return userCan((String name) -> name.equalsIgnoreCase(DataciteController.DATACITE_ROLE));
    }
    
    private List<Group> getGroupsForUser(CatalogueUser user) {
        return (user.isPublic())
            ? Collections.emptyList()
            : groupStore.getGroups(user);
    }
    
    private boolean userCan(Predicate<String> filter) {
        final CatalogueUser user = getCurrentUser();
        if (user.isPublic()) {
            return false;
        } else {
            return groupStore.getGroups(user)
                .stream()
                .map(Group::getName)
                .anyMatch(filter);
        }
    }
    
    // If the current thread of execution is running outside of spring mvc, an
    // authentication may not have been set. If this is the case, we can assume
    // that the current user is PUBLIC.
    private CatalogueUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null) ? (CatalogueUser)authentication.getPrincipal() : CatalogueUser.PUBLIC_USER;
    }
    
    private MetadataInfo getMetadataInfo(String file, String revision) {
        try {
            final DataDocument dataDocument = repo.getData(revision, format("%s.meta", file));
            return documentInfoMapper.readInfo(dataDocument.getInputStream());
        } catch (IOException ex) {
            throw new PermissionDeniedException(
                String.format(
                    "No document found for: %s at revision: %s",
                    file,
                    revision
                ),
                ex
            );
        }
    }
}