package uk.ac.ceh.gateway.catalogue.elter;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.ELTER_INPUT_DOCUMENT_JSON_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.controllers.AbstractDocumentController;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Controller
public class InputController extends AbstractDocumentController {
  private ElterService elterService;

  @Autowired
  public InputController(DocumentRepository documentRepository, ElterService elterService) {
    super(documentRepository);
    this.elterService = elterService;
    this.catalogue = "elter";
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST,
      consumes = ELTER_INPUT_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument>
  newDocument(@ActiveUser CatalogueUser user, @RequestBody InputDocument document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new eLTER Temporal Procedure Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT,
      consumes = ELTER_INPUT_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument>
  saveDocument(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody InputDocument document) throws DocumentRepositoryException {
    return saveMetadataDocument(user, file, document);
  }

  @PreAuthorize("@permission.userCanCreate('elter')")
  @RequestMapping(value = "elter/inputs", method = RequestMethod.GET)
  @ResponseBody
  public List<InputDocument> getInputs(@ActiveUser CatalogueUser user) {
    return this.elterService.getInputs();
  }
}