package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModelUse;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.NERC_MODEL_USE_JSON_VALUE;

@Slf4j
@ToString(callSuper = true)
@Controller
public class NercModelUseController extends AbstractDocumentController {

  public NercModelUseController(DocumentRepository documentRepository) {
    super(documentRepository);
    log.info("Creating {}", this);
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = NERC_MODEL_USE_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newNercModelUse(@ActiveUser CatalogueUser user, @RequestBody NercModelUse document,
      @RequestParam("catalogue") String catalogue) {
    return saveNewMetadataDocument(user, document, catalogue, "new Model use (NERC)");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = NERC_MODEL_USE_JSON_VALUE)
  public ResponseEntity<MetadataDocument> updateNercModelUse(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody NercModelUse document) {
    return saveMetadataDocument(user, file, document);
  }
}
