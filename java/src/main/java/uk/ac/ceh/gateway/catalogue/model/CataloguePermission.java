package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import uk.ac.ceh.components.userstore.Group;

@Data
@Builder
public class CataloguePermission {
    private String identity;
    private boolean view;
    private boolean edit;
    private boolean delete;
    private boolean upload;
    private boolean create;
    private boolean datacite;
    private String catalogue;
    private String id;
    private List<Group> groups;
}