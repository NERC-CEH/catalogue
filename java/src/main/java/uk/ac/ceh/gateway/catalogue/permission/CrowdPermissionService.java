package uk.ac.ceh.gateway.catalogue.permission;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.controllers.DataciteController;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.PermissionDeniedException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.String.format;

@Slf4j
@ToString
@Service("permission")
public class CrowdPermissionService implements PermissionService {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final GroupStore<CatalogueUser> groupStore;
    public static final String FOLDER = "service-agreement/";

    public CrowdPermissionService(
            @NonNull DataRepository<CatalogueUser> repo,
            @NonNull DocumentInfoMapper<MetadataInfo> documentInfoMapper,
            @NonNull GroupStore<CatalogueUser> groupStore
    ) {
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
        this.groupStore = groupStore;
        log.info("Creating CrowdPermissionService");
        log.debug("CrowdPermissionService has {}", this.groupStore);
    }

    @Override
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
                format(
                    "No document found for: %s",
                    file
                ),
                ex
            );
        }
    }

    @Override
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
        boolean canAccess = document.isPubliclyViewable(requested)
                ||
                document.canAccess(requested, user, getGroupsForUser(user));
        log.debug("Can {} access document with {} permission? {}", user.getUsername(), permission, canAccess);
        return canAccess;
    }

    @Override
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
                format(
                    "No document found for: %s",
                    file
                ),
                ex
            );
        }
    }

    @Override
    public boolean userCanUpload(@NonNull String file) {
        if (userIsAdmin()) return true;
        try {
            val user = getCurrentUser();
            val latestRevision = repo.getLatestRevision();
            val document = getMetadataInfo(file, latestRevision.getRevisionID());
            log.debug(
                    "Current user is {}, users with upload permission for {} are {}",
                    user.getUsername(),
                    file,
                    document.getIdentities(Permission.UPLOAD)
            );
            val canUpload = !user.isPublic() && toAccess(user, document, "UPLOAD");
            log.debug("Can user upload? {}", canUpload);
            return canUpload;
        } catch (DataRepositoryException ex) {
            String message = format("No document found for: %s", file);
            throw new PermissionDeniedException(message, ex);
        }
    }

    @Override
    public boolean userCanEditServiceAgreement(@NonNull String file) {
        return this.userCanEdit(FOLDER + file);
    }

    @Override
    public boolean userCanDelete(@NonNull String file) {
        try {
            CatalogueUser user = getCurrentUser();
            DataRevision<CatalogueUser> latestRevision = repo.getLatestRevision();
            MetadataInfo document = getMetadataInfo(file, latestRevision.getRevisionID());
            return !user.isPublic() && toAccess(user, document, "DELETE");
        } catch (DataRepositoryException ex) {
            String message = format("No document found for: %s", file);
            throw new PermissionDeniedException(message, ex);
        }
    }

    @Override
    public boolean userCanDeleteServiceAgreement(@NonNull String file) {
        return this.userCanDelete(FOLDER + file);
    }

    @Override
    public boolean userCanEditRestrictedFields(@NonNull String catalogue) {
        return userCanCreate(catalogue) || userCanMakePublic(catalogue);
    }

    @Override
    public boolean userCanViewOrIsInGroup(@NonNull String file, @NonNull String group) {
        return userInGroup(group) || userCanView(file);
    }

    @Override
    public boolean userCanView(@NonNull String file) {
        try {
            CatalogueUser user = getCurrentUser();
            DataRevision<CatalogueUser> latestRevision = repo.getLatestRevision();
            MetadataInfo document = getMetadataInfo(file, latestRevision.getRevisionID());
            return !user.isPublic() && toAccess(user, document, "VIEW");
        } catch (DataRepositoryException ex) {
            String message = format("No document found for: %s", file);
            throw new PermissionDeniedException(message, ex);
        }
    }

    @Override
    public boolean userCanViewServiceAgreement(@NonNull String file) {
        return this.userCanView(FOLDER + file);
    }

    @Override
    public boolean userCanCreate(@NonNull String catalogue) {
        log.debug("user can create in {}", catalogue);
        return userCan((String name) -> name.equalsIgnoreCase(
            format("role_%s_editor", catalogue)
        ));
    }

    @Override
    public boolean userCanMakePublic(@NonNull String catalogue) {
        log.debug("user can make public in {}", catalogue);
        return userCan((String name) -> name.equalsIgnoreCase(
            format("role_%s_publisher", catalogue)
        ));
    }

    @Override
    public boolean userCanDatacite() {
        return userCan((String name) -> name.equalsIgnoreCase(DataciteController.DATACITE_ROLE));
    }

    @Override
    public boolean userInGroup (String group) {
        return userCan((String name) -> name.equalsIgnoreCase(group));
    }

    @Override
    public boolean userIsAdmin() {
        return userInGroup(DocumentController.MAINTENANCE_ROLE);
    }

    @Override
    public List<Group> getGroupsForUser(CatalogueUser user) {
        return (user.isPublic())
            ? Collections.emptyList()
            : groupStore.getGroups(user);
    }

    private boolean userCan(Predicate<String> filter) {
        final CatalogueUser user = getCurrentUser();
        if (user.isPublic()) {
            return false;
        } else {
            val groups = groupStore.getGroups(user);
            log.debug("{} has groups {}", user.getUsername(), groups);
            return groups.stream()
                .map(Group::getName)
                .anyMatch(filter);
        }
    }

    // If the current thread of execution is running outside of spring mvc, an
    // authentication may not have been set. If this is the case, we can assume
    // that the current user is PUBLIC.
    private CatalogueUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        val currentUser = (authentication != null) ? (CatalogueUser)authentication.getPrincipal() : CatalogueUser.PUBLIC_USER;
        log.debug("User in SecurityContext {}", currentUser);
        return currentUser;
    }

    private MetadataInfo getMetadataInfo(String file, String revision) {
        try {
            val dataDocument = repo.getData(revision, format("%s.meta", file));
            return documentInfoMapper.readInfo(dataDocument.getInputStream());
        } catch (IOException ex) {
            throw new PermissionDeniedException(
                format(
                    "No document found for: %s at revision: %s",
                    file,
                    revision
                ),
                ex
            );
        }
    }
}
