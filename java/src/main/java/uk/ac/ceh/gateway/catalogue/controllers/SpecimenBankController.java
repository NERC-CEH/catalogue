package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
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
import uk.ac.ceh.gateway.catalogue.esb.SpecimenBank;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.SPECIMEN_BANK_JSON_VALUE;

@ToString(callSuper = true)
@Controller
public class SpecimenBankController extends AbstractDocumentController {

  @Autowired
  public SpecimenBankController(DocumentRepository documentRepository) {
    super(documentRepository);
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = SPECIMEN_BANK_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newSpecimenBank(@ActiveUser CatalogueUser user, @RequestBody SpecimenBank document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new Specimen bank");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = SPECIMEN_BANK_JSON_VALUE)
  public ResponseEntity<MetadataDocument> updateSpecimenBank(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody SpecimenBank document) throws DocumentRepositoryException {
    return saveMetadataDocument(user, file, document);
  }
}
