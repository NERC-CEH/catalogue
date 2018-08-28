package uk.ac.ceh.gateway.catalogue.upload;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.UPLOAD_DOCUMENT_JSON_VALUE;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;
import lombok.val;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.PermissionDeniedException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.JiraService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;

@Controller
@AllArgsConstructor
public class UploadController {
  private static final String START_PROGRESS = "751";

  private final UploadDocumentService uploadDocumentService;
  private final PermissionService permissionService;
  private final DocumentRepository documentRepository;
  private final JiraService jiraService;

  @RequestMapping(value = "upload/{id}", method = RequestMethod.GET)
  @ResponseBody
  public ModelAndView createOrGetUploadDocument(@ActiveUser CatalogueUser user,
      @PathVariable("id") String id) throws DocumentRepositoryException {
    val geminiDocument = (GeminiDocument) documentRepository.read(id);
    val canUpload = permissionService.userCanUpload(id);
    val canView = permissionService.userCanView(id);
    if (canView || canUpload) {
      Map<String, Object> model = new HashMap<>();
      model.put("id", id);
      model.put("title", geminiDocument.getTitle());
      return new ModelAndView("/html/upload/upload-document.ftl", model);
    } else {
      throw new PermissionDeniedException("Permissions denied");
    }
  }

  @RequestMapping(
      value = "documents/{id}", method = RequestMethod.GET, consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  get(@ActiveUser CatalogueUser user, @PathVariable("id") String id) {
    val document = uploadDocumentService.get(id);
    return ResponseEntity.ok(document);
  }

  @RequestMapping(value = "documents/{id}/add-upload-document", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<UploadDocument> addFile(
      @ActiveUser CatalogueUser user,
      @PathVariable("id") String id,
      @RequestParam("file") MultipartFile file
  ) throws IOException, DocumentRepositoryException {
      userCanUpload(id);
      try (InputStream in = file.getInputStream()) {
          val filename = file.getOriginalFilename();  
          uploadDocumentService.add(id, filename, in);
      }
      val document = uploadDocumentService.get(id);
      return ResponseEntity.ok(document);
  }

  @RequestMapping(value = "documents/{id}/delete-upload-file", method = RequestMethod.PUT,
      consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  deleteFile(@ActiveUser CatalogueUser user, @PathVariable("id") String id,
      @RequestParam("filename") String filename) {
    userCanUpload(id);
    val document = uploadDocumentService.delete(id, filename);
    return ResponseEntity.ok(document);
  }

  @RequestMapping(value = "documents/{id}/finish", method = RequestMethod.PUT,
      consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  finish(@ActiveUser CatalogueUser user, @PathVariable("id") String id)
      throws DocumentRepositoryException {
    userCanUpload(id);
    transitionIssueToStartProgress(user, id);
    val document = uploadDocumentService.get(id);
    return ResponseEntity.ok(document);
  }

  private void transitionIssueToStartProgress(CatalogueUser user, String guid) {
    val issues = jiraService.search(jql(guid));
    if (issues.size() != 1)
      throw new NonUniqueJiraIssue();
    val issue = issues.get(0);
    val key = issue.getKey();
    jiraService.transition(key, START_PROGRESS);
    jiraService.comment(
        key, String.format("%s has finished uploading the documents to dropbox", user.getEmail()));
  }

  @ResponseStatus(value = HttpStatus.BAD_REQUEST,
      reason = "Can not finish, contact admin to resolve issue clash")
  class NonUniqueJiraIssue extends RuntimeException {
    static final long serialVersionUID = 1L;
  }

  // private void removeUploadPermission(CatalogueUser user, String guid) throws
  // DocumentRepositoryException {
  //     MetadataDocument document = documentRepository.read(guid);
  //     MetadataInfo info = document.getMetadata();
  //     info.removePermission(Permission.UPLOAD, user.getUsername());
  //     document.setMetadata(info);
  //     documentRepository.save(user, document, guid, String.format("Permissions of %s changed.",
  //     guid));
  // }

  private String jql(String guid) {
    String jqlTemplate = "project=eidchelp and component='data transfer' and labels=%s";
    return String.format(jqlTemplate, guid);
  }

  private void userCanUpload(String id) {
    if (!permissionService.userCanUpload(id))
      throw new PermissionDeniedException("Invalid Permissions");
  }

  // @RequestMapping(value = "documents/{id}/accept-upload-file", method = RequestMethod.PUT,
  // consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  // public ResponseEntity<MetadataDocument> acceptFile(
  //     @ActiveUser CatalogueUser user,
  //     @PathVariable("id") String id,
  //     @RequestParam("name") String name,
  //     @RequestParam("filename") String filename,
  //     @RequestBody UploadDocument document
  // ) {
  //     userCanUpload(document);
  //     uploadDocumentService.acceptInvalid(user, document, name, filename);
  //     return ResponseEntity.ok(document);
  // }

  // @RequestMapping(value = "documents/{id}/move-upload-file", method = RequestMethod.PUT, consumes
  // = UPLOAD_DOCUMENT_JSON_VALUE)
  // public ResponseEntity<MetadataDocument> acceptFile(
  //     @ActiveUser CatalogueUser user,
  //     @PathVariable("id") String id,
  //     @RequestParam("from") String from,
  //     @RequestParam("to") String to,
  //     @RequestParam("filename") String filename,
  //     @RequestBody UploadDocument document
  // ){
  //     userCanUpload(document);
  //     uploadDocumentService.move(user, document, from, to, filename);
  //     return ResponseEntity.ok(document);
  // }

  // @RequestMapping(value = "documents/{id}/validate", method = RequestMethod.GET)
  // @SneakyThrows
  // public ResponseEntity<MetadataDocument> acceptFile(
  //     @ActiveUser CatalogueUser user,
  //     @PathVariable("id") String id
  // ) {
  //     val document = (UploadDocument) documentRepository.read(id);
  //     userCanUpload(document);
  //     val doc = documentRepository.save(user, document, id, String.format("Validated %s", id));
  //     return ResponseEntity.ok(doc);
  // }

  // @RequestMapping(value = "documents/{id}/move-to-datastore", method = RequestMethod.PUT,
  // consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  // public ResponseEntity<MetadataDocument> moveToDatastore(
  //     @ActiveUser CatalogueUser user,
  //     @PathVariable("id") String id,
  //     @RequestBody UploadDocument document
  // ) {
  //     userCanUpload(document);
  //     uploadDocumentService.moveToDatastore(user, document);
  //     return ResponseEntity.ok(document);
  // }

  // @RequestMapping(value = "documents/{id}/zip-upload-files", method = RequestMethod.PUT, consumes
  // = UPLOAD_DOCUMENT_JSON_VALUE)
  // public ResponseEntity<MetadataDocument> zip(
  //     @ActiveUser CatalogueUser user,
  //     @PathVariable("id") String id,
  //     @RequestBody UploadDocument document
  // ) {
  //     userCanUpload(document);
  //     uploadDocumentService.zip(user, document);
  //     return ResponseEntity.ok(document);
  // }

  // @RequestMapping(value = "documents/{id}/unzip-upload-files", method = RequestMethod.PUT,
  // consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  // public ResponseEntity<MetadataDocument> unzip(
  //     @ActiveUser CatalogueUser user,
  //     @PathVariable("id") String id,
  //     @RequestBody UploadDocument document
  // ) {
  //     userCanUpload(document);
  //     uploadDocumentService.unzip(user, document);
  //     return ResponseEntity.ok(document);
  // }
}