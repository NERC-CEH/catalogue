package uk.ac.ceh.gateway.catalogue.permission;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteController;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.PermissionDeniedException;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.String.format;

@Slf4j
@ToString
@Service("permission")
public class CrowdPermissionService implements PermissionService {
    private final CachedDataRepository cachedDataRepository;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final GroupStore<CatalogueUser> groupStore;
    public static final String SERVICE_AGREEMENT_FOLDER = "service-agreement/";

    public CrowdPermissionService(
            @NonNull CachedDataRepository cachedDataRepository,
            @NonNull DocumentInfoMapper<MetadataInfo> documentInfoMapper,
            @NonNull GroupStore<CatalogueUser> groupStore
    ) {
        this.cachedDataRepository = cachedDataRepository;
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
                cachedDataRepository.getLatestRevisionId(),
                permission
            );
        } catch (IOException ex) {
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
    public boolean hasLogin(CatalogueUser user) {
        return !(user == null || user.isPublic());
    }

    @Override
    public boolean userCanEdit(@NonNull String file) {
        try {
            final CatalogueUser user = getCurrentUser();
            final String revisionID = cachedDataRepository.getLatestRevisionId();
            final MetadataInfo document = getMetadataInfo(file, revisionID);
            if (user.isPublic()) {
                return false;
            } else if(userCanMakePublic(document.getCatalogue())) {
                return true;
            } else {
                return toAccess(user, document, "EDIT");
            }
        } catch (IOException ex) {
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
            val revisionID = cachedDataRepository.getLatestRevisionId();
            val document = getMetadataInfo(file, revisionID);
            log.debug(
                    "Current user is {}, users with upload permission for {} are {}",
                    user.getUsername(),
                    file,
                    document.getIdentities(Permission.UPLOAD)
            );
            val canUpload = !user.isPublic() && toAccess(user, document, "UPLOAD");
            log.debug("Can user upload? {}", canUpload);
            return canUpload;
        } catch (IOException ex) {
            String message = format("No document found for: %s", file);
            throw new PermissionDeniedException(message, ex);
        }
    }

    @Override
    public boolean userCanEditServiceAgreement(@NonNull String file) {
        return this.userCanEdit(SERVICE_AGREEMENT_FOLDER + file);
    }

    @Override
    public boolean userCanDelete(@NonNull String file) {
        try {
            CatalogueUser user = getCurrentUser();
            String revisionID = cachedDataRepository.getLatestRevisionId();
            MetadataInfo document = getMetadataInfo(file, revisionID);
            return !user.isPublic() && toAccess(user, document, "DELETE");
        } catch (IOException ex) {
            String message = format("No document found for: %s", file);
            throw new PermissionDeniedException(message, ex);
        }
    }

    @Override
    public boolean userCanDeleteServiceAgreement(@NonNull String file) {
        return this.userCanDelete(SERVICE_AGREEMENT_FOLDER + file);
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
            String revisionID = cachedDataRepository.getLatestRevisionId();
            MetadataInfo document = getMetadataInfo(file, revisionID);
            return !user.isPublic() && toAccess(user, document, "VIEW");
        } catch (IOException ex) {
            String message = format("No document found for: %s", file);
            throw new PermissionDeniedException(message, ex);
        }
    }

    @Override
    public boolean userCanViewServiceAgreement(@NonNull String file) {
        return this.userCanView(SERVICE_AGREEMENT_FOLDER + file);
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
            val bytes = cachedDataRepository.readAtRevision(revision, format("%s.meta", file));
            return documentInfoMapper.readInfo(new ByteArrayInputStream(bytes));
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
