package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Permission;

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
    
    /**
     * Returns a list of metadata ids of documents which are:
     *  - publicly accessible, 
     *  - of the correct document type (eg GeminiDocument),
     *  - have a resourceType of dataset, series or service.  
     * The list will be cached to the  metadata-listings cache.
     * @param revision of the data repository to read from
     * @param type of the document to list (eg GeminiDocument)
     * @param resourceTypes resourceTypes describe a bit more about a metadata 
     *        document than its simple type, eg GeminiMetadata documents can be 
     *        Datasets, Series, Service, etc.  So this param lists the resourceTypes
     *        we want document ids for.
     * @return a list of metadata ids
     * @throws DataRepositoryException
     * @throws IOException 
     */
    @Cacheable("metadata-listings")
    public List<String> getPublicDocuments(String revision, Class<? extends MetadataDocument> type, List<String> resourceTypes) throws DataRepositoryException, IOException {
        List<String> toReturn = new ArrayList<>();
        List<String> documents = listingService.filterFilenames(repo.getFiles(revision));
        
        log.debug("Building metadata listing @ {} of type: {}", revision, type);
        for(String file : documents) {
            try {
                MetadataDocument doc = documentBundleReader.readBundle(file, revision);
                if(type.isAssignableFrom(doc.getClass()) && doc.getMetadata().isPubliclyViewable(Permission.VIEW) && caseInsensitiveContains(resourceTypes, doc.getType())) {
                    toReturn.add(doc.getId());
                }
            }
            catch(RuntimeException | UnknownContentTypeException ex) {
                log.error("Failed to read " + file + " @ " + revision);
            }
        }
        return toReturn;
    }
    
    private boolean caseInsensitiveContains(List<String> referenceList, String testValue){
        return referenceList
            .stream()
            .anyMatch((s) -> s.equalsIgnoreCase(testValue));
    }
}
