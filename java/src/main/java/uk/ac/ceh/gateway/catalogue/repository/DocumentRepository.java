package uk.ac.ceh.gateway.catalogue.repository;

import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.io.InputStream;

public interface DocumentRepository {

    DataRevision<CatalogueUser> delete(
        CatalogueUser user,
        String id
    ) throws DocumentRepositoryException;

    /**
     * Delete with an explicit commit message, so an administrative deletion is distinguishable from an
     * ordinary one in the datastore's history.
     *
     * @param id the file id without extension, which may include a folder prefix
     *           (e.g. {@code service-agreement/abc-123})
     */
    DataRevision<CatalogueUser> delete(
        CatalogueUser user,
        String id,
        String message
    ) throws DocumentRepositoryException;

    MetadataDocument read(
        String file
    ) throws DocumentRepositoryException;

    MetadataDocument read(
        String file,
        String revision
    ) throws DocumentRepositoryException;

    MetadataDocument save(
        CatalogueUser user,
        InputStream inputStream,
        MediaType mediaType,
        String documentType,
        String catalogue,
        String message
    ) throws DocumentRepositoryException;

    MetadataDocument save(
        CatalogueUser user,
        MetadataDocument document,
        String id,
        String message
    ) throws DocumentRepositoryException;

    MetadataDocument save(
        CatalogueUser user,
        MetadataDocument document,
        String id,
        String message,
        String expectedRevision
    ) throws DocumentRepositoryException;

    MetadataDocument save(
        CatalogueUser user,
        MetadataDocument document,
        String message
    ) throws DocumentRepositoryException;

    MetadataDocument saveNew(
        CatalogueUser user,
        MetadataDocument document,
        String catalogue,
        String message
    ) throws DocumentRepositoryException;

}
