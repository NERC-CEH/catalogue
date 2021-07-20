package uk.ac.ceh.gateway.catalogue.document.reading;

import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;

import java.io.IOException;

public interface BundledReaderService<D> {
    /**
     * Get the latest version of D (some file)
     *
     * @param file
     * @return
     * @throws DataRepositoryException
     * @throws IOException
     * @throws PostProcessingException
     * @throws UnknownContentTypeException
     */
    D readBundle(String file) throws DataRepositoryException, IOException, PostProcessingException, UnknownContentTypeException;
    /**
     * Get D (some file) at this revision
     *
     * @param file
     * @param revision
     * @return
     * @throws DataRepositoryException
     * @throws IOException
     * @throws PostProcessingException
     * @throws UnknownContentTypeException
     */
    D readBundle(String file, String revision) throws DataRepositoryException, IOException, PostProcessingException, UnknownContentTypeException;
}
