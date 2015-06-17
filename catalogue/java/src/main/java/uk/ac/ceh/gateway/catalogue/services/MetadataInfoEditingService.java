package uk.ac.ceh.gateway.catalogue.services;

import java.net.URI;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

/**
 * A service capable of retrieving and saving MetadataInfo.
 * 
 * For use when just the MetadataInfo portion of a metadata record
 * needs to be edited e.g changing publication state or permissions
 * 
 */
public interface MetadataInfoEditingService {
    /**
     * Retrieve a MetadataDocument, the MetadataInfo portion is capable of being saved.
     * 
     * @param fileIdentifier identifier for metadata record
     * @param metadataUri the URI of the metadata record
     * @return a MetadataDocument
     */
    MetadataDocument getMetadataDocument(String fileIdentifier, URI metadataUri);
    
    /**
     * Save MetadataInfo for a metadata record.
     * 
     * @param fileIdentifier identifier of metadata record
     * @param info the MetadataInfo to save
     * @param user the user saving the MetadataInfo
     * @param commitMessage the message to save as the commit message 
     */
    void saveMetadataInfo(String fileIdentifier, MetadataInfo info, CatalogueUser user, String commitMessage);

}