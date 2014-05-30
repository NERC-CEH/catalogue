package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.util.Properties;
import lombok.Data;

/**
 *
 * @author cjohn
 */
@Data
public class TerraCatalogExt {
    private final String owner, ownerGroup, status, protection;
    
    public TerraCatalogExt(Properties ext) {
        this.owner = ext.getProperty("owner");
        this.ownerGroup = ext.getProperty("ownerGroup");
        this.status = ext.getProperty("status");
        this.protection = ext.getProperty("protection");
    }
}
