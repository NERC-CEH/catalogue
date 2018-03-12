package uk.ac.ceh.gateway.catalogue.elter;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.ELTER_DEPLOYMENT_RELATED_PROCESS_DURATION_DOCUMENT_JSON_VALUE;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
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
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Controller
public class DeploymentRelatedProcessDurationController extends AbstractDocumentController {
  private ElterService elterService;

  @Autowired
  public DeploymentRelatedProcessDurationController(DocumentRepository documentRepository, ElterService elterService) {
    super(documentRepository);
    this.elterService = elterService;
    this.catalogue = "elter";
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST,
      consumes = ELTER_DEPLOYMENT_RELATED_PROCESS_DURATION_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument>
  newDocument(@ActiveUser CatalogueUser user, @RequestBody DeploymentRelatedProcessDurationDocument document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new eLTER Deployment Related Process Duration Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT,
      consumes = ELTER_DEPLOYMENT_RELATED_PROCESS_DURATION_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument>
  saveDocument(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody DeploymentRelatedProcessDurationDocument document) throws DocumentRepositoryException {
    setCarriedOutBy(document, user, document.getCarriedOutByName(), document.getCarriedOutBy());
    cleanTempDocumentNames(document);
    document.setRelationships(Sets.newHashSet());
    updateRelationship(document.getRelationships(), document, "cerried-out-by", document.getCarriedOutBy());
    return saveMetadataDocument(user, file, document);
  }

  @PreAuthorize("@permission.userCanCreate('elter')")
  @RequestMapping(value = "elter/deployment-related-process-durations", method = RequestMethod.GET)
  @ResponseBody
  public List<DeploymentRelatedProcessDurationDocument> getDeploymentRelatedProcessDurationDocument(@ActiveUser CatalogueUser user) {
    return this.elterService.getDeploymentRelatedProcessDurations();
  }

  @SneakyThrows
  private void setCarriedOutBy(
    DeploymentRelatedProcessDurationDocument document, CatalogueUser user, String name, Set<String> docs) {
    if (!StringUtils.isBlank(name)) {
      val newDoc = new PersonDocument();
      newDoc.setTitle(name);
      saveNewMetadataDocument(user, newDoc, "new eLTER Person Document");
      if (docs != null)
        docs.add(newDoc.getId());
    }
  }

  private void cleanTempDocumentNames(DeploymentRelatedProcessDurationDocument document) {
    document.setCarriedOutByName(null);
  }

  private void updateRelationship(Set<Relationship> relationships, DeploymentRelatedProcessDurationDocument document, String relationship, Set<String> docs) {
    if (docs != null)
      for (val doc : docs)
        if (doc != null)
          relationships.add(new Relationship("http://purl.org/dc/terms/" + relationship, doc));
  }
}