package uk.ac.ceh.gateway.catalogue.linking;

import java.util.List;

public interface LinkDatabase {
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
    void deleteCoupledResources(List<CoupledResource> coupledResources);
    
    /**
     * Add metadata to the database.
     * 
     * @param metadata to add
     */
    void addMetadata(Metadata metadata);
    
    /**
     * Add list of Metadata to the database.
     * 
     * @param metadata list of Metadata to add
     */
    void addMetadata(List<Metadata> metadata);
    
    /**
     * Add list of CoupledResource to the database.
     * 
     * @param coupledResources list of CoupledResource to add 
     */
    void addCoupledResources(List<CoupledResource> coupledResources);
    
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
     * @return Collection of metadata
     */
    List<Metadata> findServicesForDataset(String fileIdentifier);
}