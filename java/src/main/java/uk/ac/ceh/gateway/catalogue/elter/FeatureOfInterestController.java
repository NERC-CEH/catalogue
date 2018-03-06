package uk.ac.ceh.gateway.catalogue.elter;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.ELTER_FEATURE_OF_INTEREST_DOCUMENT_JSON_VALUE;

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

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.controllers.AbstractDocumentController;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Controller
public class FeatureOfInterestController extends AbstractDocumentController {
  private ElterService elterService;

  @Autowired
  public FeatureOfInterestController(DocumentRepository documentRepository, ElterService elterService) {
    super(documentRepository);
    this.elterService = elterService;
    this.catalogue = "elter";
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = ELTER_FEATURE_OF_INTEREST_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newDocument(@ActiveUser CatalogueUser user,
      @RequestBody FeatureOfInterestDocument document, @RequestParam("catalogue") String catalogue)
      throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new eLTER Manufacturer Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = ELTER_FEATURE_OF_INTEREST_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> saveDocument(@ActiveUser CatalogueUser user,
      @PathVariable("file") String file, @RequestBody FeatureOfInterestDocument document)
      throws DocumentRepositoryException {
    setObservationPlaceholder(document, user, document.getOriginalStreamName(), document.getOriginalStream());
    cleanTempDocumentNames(document);
    return saveMetadataDocument(user, file, document);
  }

  @SneakyThrows
  private void setObservationPlaceholder(FeatureOfInterestDocument document, CatalogueUser user, String name,
      List<String> observationPlaceholders) {
    if (!StringUtils.isBlank(name)) {
      val observationPlaceholder = new ObservationPlaceholderDocument();
      observationPlaceholder.setTitle(name);
      saveNewMetadataDocument(user, observationPlaceholder, "new eLTER Observation Placeholder Document");
      if (observationPlaceholders != null)
        observationPlaceholders.add(observationPlaceholder.getId());
    }
  }

  private void cleanTempDocumentNames(FeatureOfInterestDocument document) {
    document.setOriginalStreamName(null);
  }
}