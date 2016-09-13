package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Value;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions;

/**
 * The following class represents the state at which a document is in
 * @author Christopher Johnson
 */
@Value
public class MetadataInfo {
    private final String rawType, state;
    private final String documentType, catalogue;;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Multimap<Permission, String> permissions;
    public static final String PUBLIC_GROUP = "public";
    public static final String READONLY_GROUP = "role_cig_readonly";
    public static final String PUBLISHER_GROUP = "role_cig_publisher";
    
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
    
    public MetadataInfo replaceAllPermissions(@NonNull Set<IdentityPermissions> updated) {
        MetadataInfo toReturn = new MetadataInfo(this);
        toReturn.permissions.clear();
               
        updated.stream().filter((ip) -> (ip.isCanView())).map((ip) -> {
            toReturn.addPermission(Permission.VIEW, ip.getIdentity());
            return ip;
        }).filter((ip) -> (ip.isCanEdit())).map((ip) -> {
            toReturn.addPermission(Permission.EDIT, ip.getIdentity());
            return ip;
        }).filter((ip) -> (ip.isCanDelete())).forEach((ip) -> {
            toReturn.addPermission(Permission.DELETE, ip.getIdentity());
        });
        preventAllPermissionsBeingRemovedOrOnlyPublic(toReturn);
        return toReturn;
    }
    
    private void preventAllPermissionsBeingRemovedOrOnlyPublic(MetadataInfo updated) {
        if (updated.permissions.isEmpty()) {
            updated.permissions.putAll(this.permissions);
        } else {
            Collection<String> view = updated.permissions.get(Permission.VIEW);
            Optional<String> possible = view.stream().filter(v -> PUBLIC_GROUP.equalsIgnoreCase(v)).findFirst();
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
        
        return 
            permissions.containsEntry(requested, user.getUsername().toLowerCase())
            ||
            groups
                .stream()
                .map(Group::getName)
                .filter(name -> {
                    return
                        permissions.containsEntry(requested, name.toLowerCase())
                        ||
                        (Permission.VIEW.equals(requested) && READONLY_GROUP.equalsIgnoreCase(name))
                        ||
                        (Permission.VIEW.equals(requested) && PUBLISHER_GROUP.equalsIgnoreCase(name));
                })
                .findFirst()
                .isPresent();
    }
}