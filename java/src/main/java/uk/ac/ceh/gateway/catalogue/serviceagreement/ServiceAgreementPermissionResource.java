package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource;

import java.util.*;

@ConvertUsing({
    @Template(called="html/service_agreement/service-agreement-permission.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
@Value
public class ServiceAgreementPermissionResource {
    String id, catalogue;
    Set<PermissionResource.IdentityPermissions> permissions;

    public ServiceAgreementPermissionResource(@NonNull MetadataDocument document) {
        MetadataInfo info = Objects.requireNonNull(document.getMetadata());
        this.id = document.getId();
        this.catalogue = document.getCatalogue();
        this.permissions = createPermissions(info);
    }

    @JsonCreator
    private ServiceAgreementPermissionResource(
        @JsonProperty("id") String id,
        @JsonProperty("catalogue") String catalogue,
        @JsonProperty("permissions") Set<PermissionResource.IdentityPermissions> permissions
    ){
        this.id = id;
        this.catalogue = catalogue;
        this.permissions = permissions;
    }

    public MetadataInfo updatePermissions(@NonNull MetadataInfo original) {
        return original.replaceAllPermissions(permissions);
    }

    private Set<PermissionResource.IdentityPermissions> createPermissions(MetadataInfo info) {
        Set<PermissionResource.IdentityPermissions> toReturn = new HashSet<>();
        Map<String, PermissionResource.IdentityPermissions.IdentityPermissionsBuilder> builders = new HashMap<>();

        addPermissions(info, builders, Permission.VIEW);
        addPermissions(info, builders, Permission.EDIT);

        builders.forEach((k, v) -> toReturn.add(v.build()));
        return toReturn;
    }

    private void addPermissions(MetadataInfo info, Map<String, PermissionResource.IdentityPermissions.IdentityPermissionsBuilder> builders, Permission permission) {
        info.getIdentities(permission)
            .forEach(v -> {
                PermissionResource.IdentityPermissions.IdentityPermissionsBuilder builder;
                if (builders.containsKey(v)) {
                    builder = builders.get(v);
                } else {
                    builder = PermissionResource.IdentityPermissions.builder().identity(v.toLowerCase());
                    builders.put(v, builder);
                }
                switch (permission) {
                    case VIEW:
                        builder.canView(true);
                        break;
                    case EDIT:
                        builder.canEdit(true);
                        break;
                }
            });
    }

    @Value
    public static class IdentityPermissions {
        String identity;
        boolean canView, canEdit;

        @Builder
        @JsonCreator
        private IdentityPermissions(
            @JsonProperty("identity") String identity,
            @JsonProperty("canView") boolean canView,
            @JsonProperty("canEdit") boolean canEdit
        ) {
            this.identity = Objects.requireNonNull(Strings.emptyToNull(identity));
            this.canView = canView;
            this.canEdit = canEdit;
        }
    }
}


