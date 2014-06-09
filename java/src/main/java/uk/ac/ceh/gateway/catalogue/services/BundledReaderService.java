package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import uk.ac.ceh.components.datastore.DataRepositoryException;

/**
 *
 * @author cjohn
 */
public interface BundledReaderService<D> {
    D readBundle(String file, String revision) throws DataRepositoryException, IOException, UnknownContentTypeException;
}
