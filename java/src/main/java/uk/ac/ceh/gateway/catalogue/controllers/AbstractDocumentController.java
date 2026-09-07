package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.io.IOException;

import java.net.URI;

@Slf4j
@ToString
public abstract class AbstractDocumentController {
    protected final DocumentRepository documentRepository;
    protected final CachedDataRepository cachedDataRepository;

    public AbstractDocumentController(DocumentRepository documentRepository, CachedDataRepository cachedDataRepository) {
        this.documentRepository = documentRepository;
        this.cachedDataRepository = cachedDataRepository;
    }

    /**
     * Creates a document and returns it with the revision it was committed at as the response {@code ETag}.
     *
     * <p>The editor keeps its model in memory across a create and every subsequent save in the same
     * session — it does not re-read the record in between — so this response is the only place the
     * client can learn the revision its <em>next</em> save must be predicated on. Without it that save
     * carries no {@code If-Match} and {@link IfMatchRevision#require} rejects it with a 428 that the
     * user can only escape by leaving the editor and re-entering, forcing a fresh GET.</p>
     */
    protected ResponseEntity<MetadataDocument> saveNewMetadataDocument(
                    CatalogueUser user,
                    MetadataDocument document,
                    String catalogue,
                    String message
    ) throws DocumentRepositoryException, IOException {
        MetadataDocument data = documentRepository.saveNew(user, document, catalogue, message);
        String revision = cachedDataRepository.getDocumentRevisionToken(data.getId());
        ResponseEntity.BodyBuilder builder = ResponseEntity.created(URI.create(data.getUri()));
        if (revision != null) {
            builder.eTag(revision); // Spring quotes this into a strong ETag: "revision"
        }
        return builder.body(data);
    }

    protected ResponseEntity<MetadataDocument> saveMetadataDocument(
                    CatalogueUser user,
                    String file,
                    MetadataDocument document,
                    String ifMatch
    ) throws DocumentRepositoryException, IOException {
        String expectedRevision = IfMatchRevision.require(ifMatch);
        document.setMetadata(documentRepository.read(file).getMetadata());
        MetadataDocument saved = documentRepository.save(user, document, file, String.format("Edited document: %s", file), expectedRevision);
        String newRevision = cachedDataRepository.getDocumentRevisionToken(file);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (newRevision != null) {
            builder.eTag(newRevision); // Spring quotes this into a strong ETag: "revision"
        }
        return builder.body(saved);
    }
}
