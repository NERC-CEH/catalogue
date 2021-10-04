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

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.DATALABS_JSON_VALUE;

@Controller
@RequestMapping("documents")
public class DatalabsDocumentController extends AbstractDocumentController {

  @Autowired
  public DatalabsDocumentController(DocumentRepository documentRepository) {
    super(documentRepository);
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @PostMapping(consumes = DATALABS_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newDatalabsDocument(
      @ActiveUser CatalogueUser user,
      @RequestBody DatalabsDocument document,
      @RequestParam("catalogue") String catalogue
  ) {
    return saveNewMetadataDocument(user, document, catalogue, "new DataLabs document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @PutMapping(value = "{file}", consumes = DATALABS_JSON_VALUE)
  public ResponseEntity<MetadataDocument> saveDatalabsDocument(
      @ActiveUser CatalogueUser user,
      @PathVariable("file") String file,
      @RequestBody DatalabsDocument document
  ) {
    return saveMetadataDocument(user, file, document);
  }
}