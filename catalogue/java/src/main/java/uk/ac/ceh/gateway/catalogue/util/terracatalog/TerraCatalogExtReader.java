package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author cjohn
 */
public class TerraCatalogExtReader {
    public TerraCatalogExt readTerraCatalogExt(InputStream in) throws IOException {
        Properties prop = new Properties();
        try (InputStream stream = in){
            prop.load(stream);
            return readTerraCatalogExt(prop);
        }
    }
    
    protected TerraCatalogExt readTerraCatalogExt(Properties ext) {
        return new TerraCatalogExt( ext.getProperty("owner"),
                                    ext.getProperty("ownerGroup"),
                                    ext.getProperty("status"),
                                    ext.getProperty("protection"));
    }
}
