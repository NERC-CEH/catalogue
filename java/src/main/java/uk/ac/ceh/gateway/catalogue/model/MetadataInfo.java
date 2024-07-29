package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The following class represents the state at which a document is in
 */
@Slf4j
@Value
// N.B. JsonAutoDetect needed to get 'permissions' written to file
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class MetadataInfo {
    String rawType, state;
    String documentType, catalogue;
    @Getter(AccessLevel.PUBLIC)
    Multimap<Permission, String> permissions;
    public static final String PUBLIC_GROUP = "public";
    public static final String READONLY_GROUP = "ROLE_CIG_READONLY";
    public static final String PUBLISHER_GROUP = "role_%s_publisher";

    @JsonCreator
    @Builder
    private MetadataInfo(
        @JsonProperty("rawType") String rawType,
        @JsonProperty("state") String state,
        @JsonProperty("documentType") String documentType,
        @JsonProperty("catalogue") String catalogue,
        @JsonProperty("permissions") Multimap<Permission, String> permissions
    ) {
        this.rawType = rawType;
        this.state = Optional.ofNullable(state).orElse("draft");
        this.documentType = Optional.ofNullable(documentType).orElse("");
        this.catalogue = catalogue;
        if (permissions != null) {
            this.permissions = HashMultimap.create(permissions);
        } else {
            this.permissions = HashMultimap.create();
        }
    }

    public MetadataInfo(MetadataInfo info) {
        this(
            info.rawType,
            info.state,
            info.documentType,
            info.catalogue,
            info.permissions
        );
    }

    public MetadataInfo withCatalogue(@NonNull String catalogue) {
        return MetadataInfo.builder()
            .rawType(this.rawType)
            .state(this.state)
            .documentType(this.documentType)
            .catalogue(catalogue)
            .permissions(this.permissions)
            .build();
    }

    public MetadataInfo withRawType(String rawType) {
        return MetadataInfo.builder()
            .rawType(rawType)
            .state(this.state)
            .documentType(this.documentType)
            .catalogue(this.catalogue)
            .permissions(this.permissions)
            .build();
    }

    public MetadataInfo withState(@NonNull String state) {
        return MetadataInfo.builder()
            .rawType(this.rawType)
            .state(state)
            .documentType(this.documentType)
            .catalogue(this.catalogue)
            .permissions(this.permissions)
            .build();
    }

    public MetadataInfo withPermissions(@NonNull Multimap<Permission, String> permissions) {
        return MetadataInfo.builder()
            .rawType(this.rawType)
            .state(state)
            .documentType(this.documentType)
            .catalogue(this.catalogue)
            .permissions(permissions)
            .build();
    }

    @JsonIgnore
    public MediaType getRawMediaType() {
        return MediaType.parseMediaType(rawType);
    }

    public void addPermission(@NonNull Permission permission, @NonNull String identity) {
        if (PUBLIC_GROUP.equalsIgnoreCase(identity)) {
            if(Permission.VIEW.equals(permission)) {
                // Only allow VIEW permission to be set for the PUBLIC_GROUP
                permissions.put(permission, identity.toLowerCase());
            }
        } else {
            permissions.put(permission, identity.toLowerCase());
        }
    }

    public void removePermission(Permission permission, String identity) {
        permissions.remove(permission, identity);
    }

    public MetadataInfo replaceAllPermissions(@NonNull Set<IdentityPermissions> updated) {
        MetadataInfo toReturn = new MetadataInfo(this);
        toReturn.permissions.clear();

        updated.forEach(ip -> {
            if (ip.isCanView()) {
                toReturn.addPermission(Permission.VIEW, ip.getIdentity());
            }
            if (ip.isCanEdit()) {
                toReturn.addPermission(Permission.EDIT, ip.getIdentity());
            }
            if (ip.isCanDelete()) {
                toReturn.addPermission(Permission.DELETE, ip.getIdentity());
            }
            if (ip.isCanUpload()) {
                toReturn.addPermission(Permission.UPLOAD, ip.getIdentity());
            }
        });
        preventAllPermissionsBeingRemovedOrOnlyPublic(toReturn);
        return toReturn;
    }

    private void preventAllPermissionsBeingRemovedOrOnlyPublic(MetadataInfo updated) {
        if (updated.permissions.isEmpty()) {
            updated.permissions.putAll(this.permissions);
        } else {
            Collection<String> view = updated.permissions.get(Permission.VIEW);
            Optional<String> possible = view.stream().filter(PUBLIC_GROUP::equalsIgnoreCase).findFirst();
            if (view.size() == 1 && possible.isPresent()) {
                updated.permissions.clear();
                updated.permissions.putAll(this.permissions);
            }
        }
    }

    public List<String> getIdentities(Permission permission) {
        Objects.requireNonNull(permission);
        return permissions.get(permission)
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    }

    public boolean isPubliclyViewable(Permission requested) {
        return
            Permission.VIEW.equals(requested)
            &&
            permissions.containsEntry(Permission.VIEW, PUBLIC_GROUP)
            &&
            getState().equalsIgnoreCase("published");
    }

    public boolean canAccess(Permission requested, CatalogueUser user, List<Group> groups) {
        Objects.requireNonNull(requested);
        Objects.requireNonNull(user);
        Objects.requireNonNull(groups);

        if (user.isPublic()) {
            return false;
        }
        log.debug("Permissions are {}", permissions);
        val canAccessAsUser = permissions.containsEntry(requested, user.getUsername().toLowerCase());
        log.debug("Can {} as {}? {}", requested, user.getUsername(), canAccessAsUser);
        val canAccessFromGroup = groups
                .stream()
                .map(Group::getName)
                .anyMatch(name -> {
                    val canGroupAccess = permissions.containsEntry(requested, name.toLowerCase());
                    log.debug("Can {} {}? {}", name, requested, canGroupAccess);
                    val canReadOnlyAccess = Permission.VIEW.equals(requested) && READONLY_GROUP.equalsIgnoreCase(name);
                    log.debug("Can {} {}? {}", name, requested, canReadOnlyAccess);
                    val canPublisherAccess = Permission.VIEW.equals(requested) && String.format(PUBLISHER_GROUP, catalogue).equalsIgnoreCase(name);
                    log.debug("Can {} {}? {}", name, requested, canPublisherAccess);
                    return canGroupAccess || canReadOnlyAccess || canPublisherAccess;
                });
        log.debug("Can {} from group? {}", requested, canAccessAsUser);
        return canAccessAsUser || canAccessFromGroup;
    }
}
