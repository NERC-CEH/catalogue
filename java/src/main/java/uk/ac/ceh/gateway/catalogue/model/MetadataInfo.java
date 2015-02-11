package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.userstore.Group;

/**
 * The following class represents the state at which a document is in
 * @author Christopher Johnson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class MetadataInfo {
    private String rawType, state, documentType;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Multimap<Permission, String> permissions;
    private static final String PUBLIC_GROUP = "public";
    
    public MetadataInfo() {
        permissions = HashMultimap.create();
    }
    
    public MetadataInfo(MetadataInfo info) {
        this.rawType = info.rawType;
        this.state = info.state;
        this.documentType = info.documentType;
        this.permissions = HashMultimap.create(info.permissions);
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
    
    public void removePermission(Permission permission, String identity) {
        Objects.requireNonNull(permission);
        Objects.requireNonNull(Strings.emptyToNull(identity));
        permissions.remove(permission, identity);
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
            permissions.containsEntry(Permission.VIEW, "public")
            && 
            state.equalsIgnoreCase("published");
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
                .filter(name -> permissions.containsEntry(requested, name.toLowerCase()))
                .findFirst()
                .isPresent();
    }
}
