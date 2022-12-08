package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.MethodDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.METHOD_JSON_VALUE;

@Controller
@RequestMapping("documents")
public class MethodDocumentController extends AbstractDocumentController {

  @Autowired
  public MethodDocumentController(DocumentRepository documentRepository) {
    super(documentRepository);
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @PostMapping(consumes = METHOD_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newMethodDocument(
      @ActiveUser CatalogueUser user,
      @RequestBody MethodDocument document,
      @RequestParam("catalogue") String catalogue
  ) {
    return saveNewMetadataDocument(user, document, catalogue, "new method document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @PutMapping(value = "{file}", consumes = METHOD_JSON_VALUE)
  public ResponseEntity<MetadataDocument> saveMethodDocument(
      @ActiveUser CatalogueUser user,
      @PathVariable("file") String file,
      @RequestBody MethodDocument document
  ) {
    return saveMetadataDocument(user, file, document);
  }
}