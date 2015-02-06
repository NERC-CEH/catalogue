package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collections;
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
    private Multimap<Permission, String> permissions;
    
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
        if (permissions == null) {
            permissions = ArrayListMultimap.create();
        }
        permissions.put(permission, identity.toLowerCase());
    }
    
    public void addPermissions(Permission permission, List<String> identities) {
        Objects.requireNonNull(permission);
        Objects.requireNonNull(identities);
        if (permissions == null) {
            permissions = ArrayListMultimap.create();
        }
        permissions.putAll(permission, identities);
    }
    
    public void removePermission(Permission permission, String identity) {
        Objects.requireNonNull(permission);
        Objects.requireNonNull(Strings.emptyToNull(identity));
        Optional.ofNullable(permissions)
            .ifPresent(p -> {
                p.remove(permission, identity);
            });
    }
    
    public List<String> getIdentities(Permission permission) {
        Objects.requireNonNull(permission);
        if (permissions != null) {
            return permissions.get(permission)
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    public Set<Permission> getPermissions() {
        if (permissions != null) {
            return permissions.keySet();
        } else {
            return Collections.EMPTY_SET;
        }
    }
}
