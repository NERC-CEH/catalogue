package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

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

    @SneakyThrows
    protected ResponseEntity<MetadataDocument> saveNewMetadataDocument(
                    CatalogueUser user,
                    MetadataDocument document,
                    String catalogue,
                    String message
    ) {
        MetadataDocument data = documentRepository.saveNew(user, document, catalogue, message);
        return ResponseEntity.created(URI.create(data.getUri())).body(data);
    }

    @SneakyThrows
    protected ResponseEntity<MetadataDocument> saveMetadataDocument(
                    CatalogueUser user,
                    String file,
                    MetadataDocument document,
                    String ifMatch
    ) {
        String expectedRevision = IfMatchRevision.require(ifMatch);
        document.setMetadata(documentRepository.read(file).getMetadata());
        MetadataDocument saved = documentRepository.save(user, document, file, String.format("Edited document: %s", file), expectedRevision);
        String newRevision = cachedDataRepository.getDocumentRevisionId(file + ".meta");
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (newRevision != null) {
            builder.eTag(newRevision); // Spring quotes this into a strong ETag: "revision"
        }
        return builder.body(saved);
    }
}
