package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.ProvDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.PROV_JSON_VALUE;

@Controller
@RequestMapping("documents")
public class ProvDocumentController extends AbstractDocumentController {

  @Autowired
  public ProvDocumentController(DocumentRepository documentRepository) {
    super(documentRepository);
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @PostMapping(consumes = PROV_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newProvDocument(
      @ActiveUser CatalogueUser user,
      @RequestBody ProvDocument document,
      @RequestParam("catalogue") String catalogue
  ) {
    return saveNewMetadataDocument(user, document, catalogue, "new prov document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @PutMapping(value = "{file}", consumes = PROV_JSON_VALUE)
  public ResponseEntity<MetadataDocument> saveProvDocument(
      @ActiveUser CatalogueUser user,
      @PathVariable("file") String file,
      @RequestBody ProvDocument document
  ) {
    return saveMetadataDocument(user, file, document);
  }
}