package uk.ac.ceh.gateway.catalogue.elter;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.ELTER_SINGLE_SYSTEM_DEPLOYMENT_DOCUMENT_JSON_VALUE;

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
public class SingleSystemDeploymentController extends AbstractDocumentController {
  private ElterService elterService;

  @Autowired
  public SingleSystemDeploymentController(DocumentRepository documentRepository, ElterService elterService) {
    super(documentRepository);
    this.elterService = elterService;
    this.catalogue = "elter";
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = ELTER_SINGLE_SYSTEM_DEPLOYMENT_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newDocument(@ActiveUser CatalogueUser user,
      @RequestBody SingleSystemDeploymentDocument document, @RequestParam("catalogue") String catalogue)
      throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new eLTER Single System Deployment Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = ELTER_SINGLE_SYSTEM_DEPLOYMENT_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> saveDocument(@ActiveUser CatalogueUser user,
      @PathVariable("file") String file, @RequestBody SingleSystemDeploymentDocument document)
      throws DocumentRepositoryException {
    setSingleSystemDeployment(document, user, document.getPowersName(), document.getPowers());

    setDeploymentRelatedProcessDuration(document, user, document.getDeploymentInstallationName(),
        document.getDeploymentInstallation());
    setDeploymentRelatedProcessDuration(document, user, document.getDeploymentRemovalName(),
        document.getDeploymentRemoval());
    setDeploymentRelatedProcessDuration(document, user, document.getDeploymentCleaningName(),
        document.getDeploymentCleaning());
    setDeploymentRelatedProcessDuration(document, user, document.getDeploymentMaintenanceName(),
        document.getDeploymentMaintenance());
    setDeploymentRelatedProcessDuration(document, user, document.getDeploymentProgramUpdateName(),
        document.getDeploymentProgramUpdate());

    cleanTempDocumentNames(document);
    updateRelationships(document);

    return saveMetadataDocument(user, file, document);
  }

  @SneakyThrows
  private void setDeploymentRelatedProcessDuration(SingleSystemDeploymentDocument document, CatalogueUser user,
      String name, Set<String> docs) {
    if (!StringUtils.isBlank(name)) {
      val newDoc = new DeploymentRelatedProcessDurationDocument();
      newDoc.setTitle(name);
      saveNewMetadataDocument(user, newDoc, "new eLTER Deployment Related Process Duration");
      if (docs != null)
        docs.add(newDoc.getId());
    }
  }

  @SneakyThrows
  private void setSingleSystemDeployment(SingleSystemDeploymentDocument document, CatalogueUser user, String name,
      Set<String> docs) {
    if (!StringUtils.isBlank(name)) {
      val newDoc = new SingleSystemDeploymentDocument();
      newDoc.setTitle(name);
      saveNewMetadataDocument(user, newDoc, "new eLTER Single System Deployment Document");
      if (docs != null)
        docs.add(newDoc.getId());
    }
  }

  private void cleanTempDocumentNames(SingleSystemDeploymentDocument document) {
    document.setPowersName(null);
    document.setDeploymentInstallationName(null);
    document.setDeploymentRemovalName(null);
    document.setDeploymentCleaningName(null);
    document.setDeploymentMaintenanceName(null);
    document.setDeploymentProgramUpdateName(null);
  }

  @PreAuthorize("@permission.userCanCreate('elter')")
  @RequestMapping(value = "elter/single-system-deployments", method = RequestMethod.GET)
  @ResponseBody
  public List<SingleSystemDeploymentDocument> getSingleSystemDeploymentDocument(@ActiveUser CatalogueUser user) {
    return this.elterService.getSingleSystemDeployments();
  }

  private void updateRelationships(SingleSystemDeploymentDocument document) {
    Set<Relationship> relationships = Sets.newHashSet();
    updateRelationship(relationships, document, "powers", document.getPowers());
    updateRelationship(relationships, document, "installs", document.getDeploymentInstallation());
    updateRelationship(relationships, document, "removes", document.getDeploymentRemoval());
    updateRelationship(relationships, document, "cleans", document.getDeploymentCleaning());
    updateRelationship(relationships, document, "manitains", document.getDeploymentMaintenance());
    updateRelationship(relationships, document, "program-updates", document.getDeploymentProgramUpdate());
    document.setRelationships(relationships);
  }

  private void updateRelationship(Set<Relationship> relationships, SingleSystemDeploymentDocument document, String relationship, Set<String> docs) {
    if (docs != null)
      for (val doc : docs)
        if (doc != null)
          relationships.add(new Relationship("http://purl.org/dc/terms/" + relationship, doc));
  }
}