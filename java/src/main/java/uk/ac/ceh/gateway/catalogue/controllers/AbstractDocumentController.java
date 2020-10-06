package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.net.URI;

@Slf4j
@ToString
public abstract class AbstractDocumentController {
  protected final DocumentRepository documentRepository;
  protected String catalogue;

  public AbstractDocumentController(DocumentRepository documentRepository) {
    this.documentRepository = documentRepository;
    log.info("Creating {}", this);
  }

  protected ResponseEntity<MetadataDocument> saveNewMetadataDocument(
      CatalogueUser user, MetadataDocument document, String catalogue, String message)
      throws DocumentRepositoryException {
    MetadataDocument data = documentRepository.saveNew(user, document, catalogue, message);
    return ResponseEntity.created(URI.create(data.getUri())).body(data);
  }

  protected ResponseEntity<MetadataDocument> saveNewMetadataDocument(CatalogueUser user,
      MetadataDocument document, String message) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, message);
  }

  protected ResponseEntity<MetadataDocument> saveMetadataDocument(CatalogueUser user, String file,
      MetadataDocument document) throws DocumentRepositoryException {
    document.setMetadata(documentRepository.read(file).getMetadata());
    return ResponseEntity.ok(
        documentRepository.save(user, document, file, String.format("Edited document: %s", file)));
  }
}
