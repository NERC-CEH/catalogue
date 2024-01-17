package uk.ac.ceh.gateway.catalogue.repository;

import java.io.InputStream;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

public interface DocumentRepository {

    DataRevision<CatalogueUser> delete(
        CatalogueUser user,
        String id
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
        String message
    ) throws DocumentRepositoryException;

    MetadataDocument saveNew(
        CatalogueUser user,
        MetadataDocument document,
        String catalogue,
        String message
    ) throws DocumentRepositoryException;

}
