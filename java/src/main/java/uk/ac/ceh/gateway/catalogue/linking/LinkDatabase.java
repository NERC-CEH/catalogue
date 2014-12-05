package uk.ac.ceh.gateway.catalogue.linking;

import java.util.List;
import java.util.Set;

public interface LinkDatabase {
    /**
     * Check if the database is empty
     */
    boolean isEmpty();
    
    /**
     * Empty the database of all data.
     * 
     */
    void empty();
    
    /**
     * Delete metadata from the database.
     * 
     * @param metadata to delete 
     */
    void deleteMetadata(Metadata metadata);
    
    /**
     * Delete coupledResources from the database.
     * 
     * @param coupledResources to delete 
     */
    void deleteCoupledResources(Set<CoupledResource> coupledResources);
    
    /**
     * Add metadata to the database.
     * 
     * @param metadata to add
     */
    void addMetadata(Metadata metadata);
    
    /**
     * Add set of Metadata to the database.
     * 
     * @param metadata set of Metadata to add
     */
    void addMetadata(Set<Metadata> metadata);
    
    /**
     * Add set of CoupledResource to the database.
     * 
     * @param coupledResources set of CoupledResource to add 
     */
    void addCoupledResources(Set<CoupledResource> coupledResources);
    
    /**
     * Get dataset metadata for a service.
     * 
     * @param fileIdentifier of service
     * @return list of metadata
     */
    List<Metadata> findDatasetsForService(String fileIdentifier);
    
    /**
     * Get service metadata for a dataset.
     * 
     * @param fileIdentifier of dataset
     * @return set of metadata
     */
    List<Metadata> findServicesForDataset(String fileIdentifier);
    
    /**
     * Get parent's metadata.
     * 
     * @param fileIdentifier of child
     * @return parent's metadata
     */
    Metadata findParent(String fileIdentifier);
    
    /**
     * Get children's metadata.
     * 
     * @param fileIdentifier of parent
     * @return set of children's metadata
     */
    List<Metadata> findChildren(String fileIdentifier);
    
    /**
     * Get revised's metadata.
     * 
     * @param fileIdentifier of deprecated metadata
     * @return revised's metadata
     */
    Metadata findRevised(String fileIdentifier);
    
    Metadata findRevisionOf(String fileIdentifier);
}