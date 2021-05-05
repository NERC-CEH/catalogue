package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions.IdentityPermissionsBuilder;

import java.util.*;

@ConvertUsing({
    @Template(called="html/permission.ftl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Value
public class PermissionResource {
    String id, catalogue;
    Set<IdentityPermissions> permissions;

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
        addPermissions(info, builders, Permission.UPLOAD);

        builders.forEach((k, v) -> toReturn.add(v.build()));
        return toReturn;
    }

    private void addPermissions(MetadataInfo info, Map<String, IdentityPermissionsBuilder> builders, Permission permission) {
        info.getIdentities(permission)
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
                    case UPLOAD:
                        builder.canUpload(true);
                        break;
                }
            });
    }

    @Value
    public static class IdentityPermissions {
        String identity;
        boolean canView, canEdit, canDelete, canUpload;

        @Builder
        @JsonCreator
        private IdentityPermissions(
            @JsonProperty("identity") String identity,
            @JsonProperty("canView") boolean canView,
            @JsonProperty("canEdit") boolean canEdit,
            @JsonProperty("canDelete") boolean canDelete,
            @JsonProperty("canUpload") boolean canUpload
        ) {
            this.identity = Objects.requireNonNull(Strings.emptyToNull(identity));
            this.canView = canView;
            this.canEdit = canEdit;
            this.canDelete = canDelete;
            this.canUpload = canUpload;
        }
    }
}
