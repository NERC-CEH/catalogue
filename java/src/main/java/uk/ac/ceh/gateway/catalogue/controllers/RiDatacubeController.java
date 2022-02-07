package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.ri.RiDatacube;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RI_DATACUBE_JSON_VALUE;

@Slf4j
@Controller
@RequestMapping("documents")
public class RiDatacubeController extends AbstractDocumentController {

  public RiDatacubeController(DocumentRepository documentRepository) {
    super(documentRepository);
    log.info("Creating");
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @PostMapping(consumes = RI_DATACUBE_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newRiDatacube(
      @ActiveUser CatalogueUser user,
      @RequestBody RiDatacube document,
      @RequestParam("catalogue") String catalogue
  ) {
    return saveNewMetadataDocument(user, document, catalogue, "new Research infrastructure");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @PutMapping(value = "{file}", consumes = RI_DATACUBE_JSON_VALUE)
  public ResponseEntity<MetadataDocument> updateRiDatacube(
      @ActiveUser CatalogueUser user,
      @PathVariable("file") String file,
      @RequestBody RiDatacube document
  ) {
    return saveMetadataDocument(user, file, document);
  }
}
