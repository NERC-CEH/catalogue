package uk.ac.ceh.gateway.catalogue.elter;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.ELTER_TEMPORAL_PROCEDURE_DOCUMENT_JSON_VALUE;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.controllers.AbstractDocumentController;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.DocumentReader;

@Controller
public class TemporalProdecureController extends AbstractDocumentController {
  private ElterService elterService;

  @Autowired
  public TemporalProdecureController(DocumentRepository documentRepository, ElterService elterService) {
    super(documentRepository);
    this.elterService = elterService;
    this.catalogue = "elter";
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = ELTER_TEMPORAL_PROCEDURE_DOCUMENT_JSON_VALUE)
  @SneakyThrows
  public ResponseEntity<MetadataDocument> newDocument(@ActiveUser CatalogueUser user, @RequestParam("catalogue") String catalogue, @RequestBody TemporalProcedureDocument document) {
    return saveNewMetadataDocument(user, document, catalogue, "new eLTER Temporal Procedure Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = ELTER_TEMPORAL_PROCEDURE_DOCUMENT_JSON_VALUE)
  @SneakyThrows
  public ResponseEntity<MetadataDocument> saveDocument(@ActiveUser CatalogueUser user, @PathVariable("file") String file, @RequestBody TemporalProcedureDocument document) {
    document.getReplacedBy().removeIf(id -> StringUtils.isBlank(id) || !DocumentReader.exists(id));
    setReplacedBY(document, user);
    return saveMetadataDocument(user, file, document);
  }

  @PreAuthorize("@permission.userCanCreate('elter')")
  @RequestMapping(value = "elter/temporal-procedures", method = RequestMethod.GET)
  @ResponseBody
  public List<TemporalProcedureDocument> getTemporalProcedures(@ActiveUser CatalogueUser user) {
    return this.elterService.getTemporalProcedures();
  }

  @SneakyThrows
  private void setReplacedBY(TemporalProcedureDocument document, CatalogueUser user) {
    val replacedByName = document.getReplacedByName();
    if (!StringUtils.isBlank(replacedByName)) {
      val temporalProcedure = new TemporalProcedureDocument();
      temporalProcedure.setTitle(replacedByName);
      saveNewMetadataDocument(user, temporalProcedure, "new eLTER Temporal Procedure Document");
      document.getReplacedBy().add(temporalProcedure.getId());
    }
    document.setReplacedByName(null);
  }
}