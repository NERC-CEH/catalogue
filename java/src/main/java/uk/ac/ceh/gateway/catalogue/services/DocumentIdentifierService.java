package uk.ac.ceh.gateway.catalogue.services;

import java.util.Objects;
import java.util.UUID;
import lombok.Data;

/**
 * This application hosts metadata documents on paths which take the form:
 *    http://somehost/id/{metadata-id}
 * 
 * An issue with this is that (in general) metadata identifiers can take any
 * form. For example, thredds generates Gemini xml with identifiers that contain
 * slashes and dots (/, .). These will break the above url scheme.
 * 
 * As such, it is important that we are able to produce a internal identifier
 * which can be used to create a valid url in the above scheme. This class is
 * responsible for doing just that.
 * @author cjohn
 */
@Data
public class DocumentIdentifierService {
    private final String baseUri;
    private final char replacement;
    
    /**
     * Takes a document describing identifier and derives a new one which 
     * replaces any url (or filesystem) invalid characters
     * @param identifier from a document (e.g. fileIdentifier in Gemini)
     * @return an identifier compatible in the system
     */
    public String generateFileId(String identifier) {
        if(identifier == null) {
            return null;
        }
        return identifier
                .replace('/', replacement)
                .replace('.', replacement);
    }
    
    /**
     * Generates the latest uri for a document represented by a supplied 
     *  identifier.
     * @param identifier the identifier of the document
     * @return a string representation of the uri for the document
     */
    public String generateUri(String identifier) {
        String id = Objects.requireNonNull(identifier, "A identifier is required for it to be assigned a uri");
        return baseUri + "/id/" + generateFileId(id);
    }
    
    /**
     * Generates a uri for a document at a paricular revision.
     * @param identifier the identifier of the document
     * @param revision the revision which the document is being read from
     * @return a string representation of a document from history
     */
    public String generateUri(String identifier, String revision) {
        String id = Objects.requireNonNull(identifier, "A identifier is required for it to be assigned a uri");
        return baseUri + "/history/" + revision + "/" + generateFileId(id);
    }
    
    /**
     * Generate a fresh metadata document file identifier
     * @return a uuid which can be used as a file name
     */
    public String generateFileId() {
        return UUID.randomUUID().toString();
    }
}
