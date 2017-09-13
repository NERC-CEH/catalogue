package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.net.URI;

public abstract class AbstractDocumentController {
    protected final DocumentRepository documentRepository;

    @Autowired
    public AbstractDocumentController(
        DocumentRepository documentRepository
    ) {
        this.documentRepository = documentRepository;
    }

    protected ResponseEntity<MetadataDocument> saveNewMetadataDocument(
        CatalogueUser user,
        MetadataDocument document,
        String catalogue,
        String message
    ) throws DocumentRepositoryException {
        MetadataDocument data = documentRepository.saveNew(
            user,
            document,
            catalogue,
            message
        );
        return ResponseEntity
            .created(URI.create(data.getUri()))
            .body(data);

    }

    protected ResponseEntity<MetadataDocument> saveMetadataDocument(
        CatalogueUser user,
        String file,
        MetadataDocument document
    ) throws DocumentRepositoryException {
        document.setMetadata(
            documentRepository.read(file).getMetadata()
        );
        return ResponseEntity.ok(
            documentRepository.save(
                user,
                document,
                file,
                String.format("Edited document: %s", file)
            )
        );
    }
}
