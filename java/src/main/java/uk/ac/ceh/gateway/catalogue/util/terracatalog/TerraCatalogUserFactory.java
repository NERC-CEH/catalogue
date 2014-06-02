package uk.ac.ceh.gateway.catalogue.util.terracatalog;

/**
 * The following interface specifies how to generate a Domain specific user 
 * from a given TerraCatalogExt object. Implementations may back on some 
 * UserRepository system, or you may be able to generate users offline
 * @param <U> The domain specific user type
 * @see OfflineTerraCatalogUserFactory
 * @author cjohn
 */
public interface TerraCatalogUserFactory<U> {
    /**
     * Obtain a user based on the information stored inside the TerraCatalogExt
     * object
     * @param externalInfo
     * @throws IllegalArgumentException If the TerraCatalogExt can not be used to 
     *  create a user
     * @return A domain specific user which represents the author of the 
     *  TerraCatalogExt
     */
    U getAuthor(TerraCatalogExt externalInfo);
}
