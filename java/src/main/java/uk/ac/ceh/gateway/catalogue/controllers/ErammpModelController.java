package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpModel;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Controller
public class ErammpModelController extends AbstractDocumentController {

  @Autowired
  public ErammpModelController(DocumentRepository documentRepository) {
    super(documentRepository);
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = ERAMMP_MODEL_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newErammpModel(@ActiveUser CatalogueUser user, @RequestBody ErammpModel document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new ERAMMP model");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = ERAMMP_MODEL_JSON_VALUE)
  public ResponseEntity<MetadataDocument> updateErammpModel(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody ErammpModel document) throws DocumentRepositoryException {
    return saveMetadataDocument(user, file, document);
  }
}
