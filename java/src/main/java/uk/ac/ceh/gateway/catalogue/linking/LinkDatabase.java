package uk.ac.ceh.gateway.catalogue.linking;

import java.util.Collection;

public interface LinkDatabase {
    /**
     * Empty the database of all data.
     * 
     * @throws DocumentLinkingException 
     */
    void empty() throws DocumentLinkingException;
    
    /**
     * Delete metadata from the database.
     * 
     * @param metadata to delete 
     * @throws DocumentLinkingException 
     */
    void delete(Metadata metadata) throws DocumentLinkingException;
    
    /**
     * Add metadata to the database.
     * 
     * @param metadata to add
     * @throws DocumentLinkingException 
     */
    void add(Metadata metadata) throws DocumentLinkingException;
    
    /**
     * Add collection of metadata to the database.
     * 
     * @param metadata collection of metadata to add
     * @throws DocumentLinkingException 
     */
    void add(Collection<Metadata> metadata) throws DocumentLinkingException;
    
    /**
     * Get dataset metadata for a service.
     * 
     * @param fileIdentifier of service
     * @return Collection of metadata
     */
    Collection<Metadata> findDatasetsForService(String fileIdentifier);
    
    /**
     * Get service metadata for a dataset.
     * 
     * @param fileIdentifier of dataset
     * @return Collection of metadata
     */
    Collection<Metadata> findServicesForDataset(String fileIdentifier);
}