package uk.ac.ceh.gateway.catalogue.permission;

import lombok.NonNull;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;

public interface PermissionService {
    boolean toAccess(CatalogueUser user, String file, String permission);

    boolean toAccess(
            @NonNull CatalogueUser user,
            @NonNull String file,
            @NonNull String revision,
            @NonNull String permission
    );

    boolean userCanEdit(@NonNull String file);

    boolean userCanEditServiceAgreement(@NonNull String file);

    boolean userCanUpload(@NonNull String file);

    boolean userCanDelete(@NonNull String file);

    boolean userCanDeleteServiceAgreement(@NonNull String file);

    boolean userCanEditRestrictedFields(@NonNull String catalogue);

    boolean userCanViewOrIsInGroup(@NonNull String file, @NonNull String group);

    boolean userCanView(@NonNull String file);

    boolean userCanViewServiceAgreement(@NonNull String file);

    boolean userCanCreate(@NonNull String catalogue);

    boolean userCanMakePublic(@NonNull String catalogue);

    boolean userCanDatacite();

    boolean userInGroup(String group);

    boolean userIsAdmin();

    List<Group> getGroupsForUser(CatalogueUser user);
}
