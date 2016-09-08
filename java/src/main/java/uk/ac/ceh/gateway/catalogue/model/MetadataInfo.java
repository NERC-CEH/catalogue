package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions;

/**
 * The following class represents the state at which a document is in
 * @author Christopher Johnson
 */
@Data
@Accessors(chain = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class MetadataInfo {
    private String rawType, state, documentType, catalogue;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Multimap<Permission, String> permissions;
    public static final String PUBLIC_GROUP = "public";
    public static final String READONLY_GROUP = "role_cig_readonly";
    public static final String PUBLISHER_GROUP = "role_cig_publisher";
    
    public MetadataInfo() {
        permissions = HashMultimap.create();
    }
    
    public MetadataInfo(MetadataInfo info) {
        this.rawType = info.rawType;
        this.state = info.state;
        this.documentType = info.documentType;
        this.permissions = HashMultimap.create(info.permissions);
        this.catalogue = info.catalogue;
    }
      
    @JsonIgnore
    public MediaType getRawMediaType() {
        return MediaType.parseMediaType(rawType);
    }
    
    public void hideMediaType() {
        setRawType(null);
    }
    
    public String getState() {
        return Optional.ofNullable(state).orElse("draft");
    }
    
    public String getDocumentType() {
        return Optional.ofNullable(documentType).orElse("");
    }
    
    public void addPermission(Permission permission, String identity) {
        Objects.requireNonNull(permission);
        Objects.requireNonNull(Strings.emptyToNull(identity));
        if (PUBLIC_GROUP.equalsIgnoreCase(identity)) {
            if(Permission.VIEW.equals(permission)) {
                // Only allow VIEW permission to be set for the PUBLIC_GROUP
                permissions.put(permission, identity.toLowerCase());
            }
        } else {
            permissions.put(permission, identity.toLowerCase());
        }
    }
    
    public MetadataInfo replaceAllPermissions(Set<IdentityPermissions> updated) {
        Objects.requireNonNull(updated);
        MetadataInfo toReturn = new MetadataInfo(this);
        toReturn.permissions.clear();
        
        for (IdentityPermissions ip : updated) {
            if (ip.isCanView()) {
                toReturn.addPermission(Permission.VIEW, ip.getIdentity());

                if (ip.isCanEdit()) {
                    toReturn.addPermission(Permission.EDIT, ip.getIdentity());

                    if (ip.isCanDelete()) {
                        toReturn.addPermission(Permission.DELETE, ip.getIdentity());
                    }
                }
            }
        }
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