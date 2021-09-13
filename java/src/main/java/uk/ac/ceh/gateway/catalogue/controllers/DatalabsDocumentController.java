package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.datalabs.DatalabsDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.*;

@Controller
public class DatalabsDocumentController extends AbstractDocumentController {

  @Autowired
  public DatalabsDocumentController(DocumentRepository documentRepository) {
    super(documentRepository);
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = DATALABS_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newDatalabsDocument(@ActiveUser CatalogueUser user, @RequestBody DatalabsDocument document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new DataLabs document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = DATALABS_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> saveDatalabsDocument(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody DatalabsDocument document) throws DocumentRepositoryException {
    return saveMetadataDocument(user, file, document);
  }
}