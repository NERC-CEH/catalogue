package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Value;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions.IdentityPermissionsBuilder;

@ConvertUsing({
    @Template(called="html/permission.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Value
public class PermissionResource {
    private final String id, catalogue;
    private final Set<IdentityPermissions> permissions;
    
    public PermissionResource(@NonNull MetadataDocument document) {
        MetadataInfo info = Objects.requireNonNull(document.getMetadata());
        this.id = document.getId();
        this.catalogue = document.getCatalogue();
        this.permissions = createPermissions(info);
    }
    
    @JsonCreator
    private PermissionResource(
        @JsonProperty("id") String id, 
        @JsonProperty("catalogue") String catalogue,
        @JsonProperty("permissions") Set<IdentityPermissions> permissions
    ){
        this.id = id;
        this.catalogue = catalogue;
        this.permissions = permissions;
    }
    
    public MetadataInfo updatePermissions(@NonNull MetadataInfo original) {
        return original.replaceAllPermissions(permissions);
    }

    private Set<IdentityPermissions> createPermissions(MetadataInfo info) {
        Set<IdentityPermissions> toReturn = new HashSet<>();
        Map<String, IdentityPermissions.IdentityPermissionsBuilder> builders = new HashMap<>();
        
        addPermissions(info, builders, Permission.VIEW);
        addPermissions(info, builders, Permission.EDIT);
        addPermissions(info, builders, Permission.DELETE);
        
        builders.forEach((k, v) -> {
            toReturn.add(v.build());
        });
        return toReturn;
    }
    
    private void addPermissions(MetadataInfo info, Map<String, IdentityPermissionsBuilder> builders, Permission permission) {
        info.getIdentities(permission)
            .stream()
            .forEach(v -> {
                IdentityPermissions.IdentityPermissionsBuilder builder;
                if (builders.containsKey(v)) {
                    builder = builders.get(v);
                } else {
                    builder = IdentityPermissions.builder().identity(v.toLowerCase());
                    builders.put(v, builder);
                }
                switch (permission) {
                    case VIEW:
                        builder.canView(true);
                        break;
                    case EDIT:
                        builder.canEdit(true);
                        break;
                    case DELETE:
                        builder.canDelete(true);
                        break;
                }
            });
    }

    @Value
    public static class IdentityPermissions {
        private final String identity;
        private final boolean canView, canEdit, canDelete;

        @Builder
        @JsonCreator
        private IdentityPermissions(
            @JsonProperty("identity") String identity,
            @JsonProperty("canView") boolean canView,
            @JsonProperty("canEdit") boolean canEdit,
            @JsonProperty("canDelete") boolean canDelete
        ) {
            this.identity = Objects.requireNonNull(Strings.emptyToNull(identity));
            this.canView = canView;
            this.canEdit = canEdit;
            this.canDelete = canDelete;
        }
    }
}