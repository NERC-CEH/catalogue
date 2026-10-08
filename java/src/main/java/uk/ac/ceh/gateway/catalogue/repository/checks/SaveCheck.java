package uk.ac.ceh.gateway.catalogue.repository.checks;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

/**
 * A check that can refuse a save. {@code GitDocumentRepository} runs every check once, in order,
 * before it commits anything, so a refused save leaves nothing behind in the datastore.
 * <p>
 * A check refuses by throwing; the exception decides the HTTP response
 * ({@code ExceptionControllerHandler}).
 */
public interface SaveCheck {

    /**
     * @param user the user saving
     * @param incoming the document as it will be written
     * @param id the id it will be written under
     * @param stored the version already in the datastore, read only if a check asks for it
     */
    void check(CatalogueUser user, MetadataDocument incoming, String id, StoredVersion stored);
}
