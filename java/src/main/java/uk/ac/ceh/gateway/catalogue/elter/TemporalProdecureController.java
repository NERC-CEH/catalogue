package uk.ac.ceh.gateway.catalogue.elter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.controllers.AbstractDocumentController;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

import java.util.List;

@Controller
public class TemporalProdecureController extends AbstractDocumentController {
  private ElterService elterService;

  @Autowired
  public TemporalProdecureController(DocumentRepository documentRepository, ElterService elterService) {
    super(documentRepository);
    this.elterService = elterService;
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST,
      consumes = ELTER_TEMPORAL_PROCEDURE_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument>
  newDocument(@ActiveUser CatalogueUser user, @RequestBody TemporalProcedureDocument document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new eLTER Temporal Procedure Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT,
      consumes = ELTER_TEMPORAL_PROCEDURE_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument>
  saveDocument(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody TemporalProcedureDocument document) throws DocumentRepositoryException {
        System.out.println(String.format("replaced by", document.getReplacedBy()));
    return saveMetadataDocument(user, file, document);
  }

  @PreAuthorize("@permission.userCanCreate('elter')")
  @RequestMapping(value = "elter/temporal-procedures", method = RequestMethod.GET)
  @ResponseBody
  public List<TemporalProcedureDocument> getTemporalProcedures(@ActiveUser CatalogueUser user)
      throws DocumentRepositoryException {
    return this.elterService.getTemporalProcedures();
  }
}