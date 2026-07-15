package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataPreconditionRequiredException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.net.URI;

@Slf4j
@ToString
public abstract class AbstractDocumentController {
    protected final DocumentRepository documentRepository;

    public AbstractDocumentController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
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
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new MetadataPreconditionRequiredException(
                "An If-Match header carrying the record's current revision is required to update it.");
        }
        String expectedRevision = unquoteETag(ifMatch);
        document.setMetadata(documentRepository.read(file).getMetadata());
        return ResponseEntity.ok(
            documentRepository.save(user, document, file, String.format("Edited document: %s", file), expectedRevision));
    }

    // ETag values are quoted per HTTP (e.g. "abc123"); the stored git revision is unquoted.
    private static String unquoteETag(String etag) {
        String trimmed = etag.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
