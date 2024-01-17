package uk.ac.ceh.gateway.catalogue.permission;

import lombok.Builder;
import lombok.Data;
import uk.ac.ceh.components.userstore.Group;

import java.util.List;

@Data
@Builder
public class CataloguePermission {
    private boolean view;
    private boolean edit;
    private boolean delete;
    private boolean upload;
    private boolean create;
    private boolean datacite;
    private boolean makePublic;
    private boolean editRestrictedFields;

    private String identity;
    private String catalogue;
    private String id;
    private List<Group> groups;
}
