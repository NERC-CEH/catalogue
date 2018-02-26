package uk.ac.ceh.gateway.catalogue.elter;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.ELTER_OBSERVATION_PLACEHOLDER_DOCUMENT_JSON_VALUE;

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
import uk.ac.ceh.gateway.catalogue.services.DocumentReader;

@Controller
public class ObservationPlaceholderController extends AbstractDocumentController {
  private ElterService elterService;

  @Autowired
  public ObservationPlaceholderController(DocumentRepository documentRepository, ElterService elterService) {
    super(documentRepository);
    this.elterService = elterService;
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST,
      consumes = ELTER_OBSERVATION_PLACEHOLDER_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument>
  newDocument(@ActiveUser CatalogueUser user, @RequestBody ObservationPlaceholderDocument document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new eLTER Manufacturer Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT,
      consumes = ELTER_OBSERVATION_PLACEHOLDER_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument>
  saveDocument(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody ObservationPlaceholderDocument document) throws DocumentRepositoryException {
    if (document.getRoutedTo() != null) document.getRoutedTo().removeIf(id -> StringUtils.isBlank(id) || !DocumentReader.exists(id));
    setRoutedTo(document, user);
    return saveMetadataDocument(user, file, document);
  }

  @SneakyThrows
  private void setRoutedTo(ObservationPlaceholderDocument document, CatalogueUser user) {
    val routedToName = document.getRoutedToName();

    if (!StringUtils.isBlank(routedToName)) {
      val input = new InputDocument();
      input.setTitle(routedToName);
      saveNewMetadataDocument(user, input, "new eLTER Input Document");
      if (document.getRoutedTo() != null)  document.getRoutedTo().add(input.getId());
    }
    document.setRoutedToName(null);
  }
}