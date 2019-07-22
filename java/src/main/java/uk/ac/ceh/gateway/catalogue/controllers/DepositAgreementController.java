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
import uk.ac.ceh.gateway.catalogue.eidc.DepositAgreement;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Controller
public class DepositAgreementController extends AbstractDocumentController {

  @Autowired
  public DepositAgreementController(DocumentRepository documentRepository) {
    super(documentRepository);
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = DEPOSITAGREEMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newDepositAgreement(@ActiveUser CatalogueUser user, @RequestBody DepositAgreement document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new ERAMMP data cube 2");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = DEPOSITAGREEMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> updateDepositAgreement(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody DepositAgreement document) throws DocumentRepositoryException {
    return saveMetadataDocument(user, file, document);
  }
}
