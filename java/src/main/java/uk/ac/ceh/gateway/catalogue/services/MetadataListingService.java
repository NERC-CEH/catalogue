package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

/**
 * The following is a cacheable metadata file lister. It will scan the data 
 * repository for metadata files of a particular type which are accessible by a 
 * particular user.
 * 
 * @author cjohn
 */
@Data
@Slf4j
public class MetadataListingService {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentListingService listingService;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final PermissionService permissions;
    
    /**
     * Returns a list of metadata ids of documents which are publicly accessable
     * of the given metadata type.
     * @param revision the data revision to list files from
     * @param type of document which should be listed
     * @return A list of metadata ids which are present in the git repository
     * @throws DataRepositoryException
     */
    public List<String> getPublicDocuments(String revision, Class<? extends MetadataDocument> type) throws DataRepositoryException, IOException {
        return getDocuments(revision, type, CatalogueUser.PUBLIC_USER);
    }
    
    /**
     * Gets a list of metadata ids of documents in the data repository which are 
     * accessible by the given user. This method will be cached to the 
     * metadata-listings cache.
     * @param revision of the data repository to read from
     * @param type of the document to list
     * @param user who can access the documents
     * @return a list of metadata ids
     * @throws DataRepositoryException
     * @throws IOException 
     */
    @Cacheable("metadata-listings")
    public List<String> getDocuments(String revision, Class<? extends MetadataDocument> type, CatalogueUser user) throws DataRepositoryException, IOException {
        List<String> toReturn = new ArrayList<>();
        List<String> documents = listingService.filterFilenames(repo.getFiles(revision));
        
        log.debug("Building metadata listing for " + user + " @ " + revision + " of type " + type);
        for(String file : documents) {
            try {
                MetadataDocument doc = documentBundleReader.readBundle(file, revision);
                if(type.isAssignableFrom(doc.getClass()) && permissions.userCanAccess(user, doc.getMetadata())) {
                    toReturn.add(doc.getId());
                }
            }
            catch(RuntimeException | UnknownContentTypeException ex) {
                log.error("Failed to read " + file + " @ " + revision);
            }
        }
        return toReturn;
    }
}
