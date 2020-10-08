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
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@ToString(callSuper = true)
@Controller
public class ErammpDatacubeController extends AbstractDocumentController {

  @Autowired
  public ErammpDatacubeController(DocumentRepository documentRepository) {
    super(documentRepository);
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = ERAMMP_DATACUBE_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newErammpDatacube(@ActiveUser CatalogueUser user, @RequestBody ErammpDatacube document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new ERAMMP data cube");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = ERAMMP_DATACUBE_JSON_VALUE)
  public ResponseEntity<MetadataDocument> updateErammpDatacube(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody ErammpDatacube document) throws DocumentRepositoryException {
    return saveMetadataDocument(user, file, document);
  }
}
