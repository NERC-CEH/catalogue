package uk.ac.ceh.gateway.catalogue.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

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
 */
@AllArgsConstructor
public class DocumentIdentifierService {
    @Getter
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
    public String generateUri(@NonNull String identifier) {
        return String.format("%s/id/%s", baseUri, generateFileId(identifier));
    }
    
    /**
     * Generates a uri for a document at a paricular revision.
     * @param identifier the identifier of the document
     * @param revision the revision which the document is being read from
     * @return a string representation of a document from history
     */
    public String generateUri(
        @NonNull String identifier,
        @NonNull String revision) {
        return String.format(
            "%s/history/%s/%s",
            baseUri,
            revision,
            generateFileId(identifier)
        );
    }
    
    /**
     * Generate a fresh metadata document file identifier
     * @return a uuid which can be used as a file name
     */
    public String generateFileId() {
        return UUID.randomUUID().toString();
    }
}
