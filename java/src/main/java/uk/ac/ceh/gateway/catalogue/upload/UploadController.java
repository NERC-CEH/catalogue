package uk.ac.ceh.gateway.catalogue.upload;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.JiraService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.UPLOAD_DOCUMENT_JSON_VALUE;

@Controller
@Slf4j
@ToString
public class UploadController {
  private static final String START_PROGRESS = "751";
  private static final String HOLD = "831";
  private static final String UNHOLD = "811";
  private static final String APPROVE = "711";
  private static final String SCHEDULED = "741";

  private final UploadDocumentService uploadDocumentService;
  private final PermissionService permissionService;
  private final DocumentRepository documentRepository;
  private final JiraService jiraService;

  public UploadController(
          UploadDocumentService uploadDocumentService,
          PermissionService permissionService,
          DocumentRepository documentRepository,
          JiraService jiraService
  ) {
    this.uploadDocumentService = uploadDocumentService;
    this.permissionService = permissionService;
    this.documentRepository = documentRepository;
    this.jiraService = jiraService;
    log.info("Creating {}", this);
  }

  @PreAuthorize("@permission.userCanUpload(#id)")
  @RequestMapping(value = "upload/{id}", method = RequestMethod.GET)
  @ResponseBody
  public ModelAndView createOrGetUploadDocument(@ActiveUser CatalogueUser user, @PathVariable("id") String id) throws DocumentRepositoryException {
    val geminiDocument = (GeminiDocument) documentRepository.read(id);
    Map<String, Object> model = new HashMap<>();
    model.put("id", id);
    model.put("title", geminiDocument.getTitle());
    return new ModelAndView("/html/upload/upload-document.ftl", model);
  }

  @GetMapping(value = "documents/{id}", consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument> get(
    @ActiveUser CatalogueUser user,
    @PathVariable("id") String id,
    @RequestParam(value = "documents_page", defaultValue = "1") int documentsPage,
    @RequestParam(value = "datastore_page", defaultValue = "1") int datastorePage,
    @RequestParam(value = "supporting_documents_page", defaultValue = "1") int supportingDocumentsPage
  ) {
    val document = uploadDocumentService.get(id, documentsPage, datastorePage, supportingDocumentsPage);
    return ResponseEntity.ok(document);
  }

  @GetMapping("upload/documents/csv/{id}")
  public void exportCSV(
    @ActiveUser CatalogueUser user,
    @PathVariable("id") String id,
    HttpServletResponse response
  ) throws Exception {
    val writer = response.getWriter();
    uploadDocumentService.getCsv(writer, id);
    response.setContentType("text/csv");
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"checksums_%s.csv\"", id));	
  }

  @RequestMapping(value = "documents/{id}/add-upload-document", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<UploadDocument> addFile(@ActiveUser CatalogueUser user,
      @PathVariable("id") String id, @RequestParam("file") MultipartFile file)
  {
    userCanUpload(id);
    val filename = file.getOriginalFilename();
    uploadDocumentService.add(id, filename, file);
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
    removeUploadPermission(user, id);
    val document = uploadDocumentService.get(id);
    return ResponseEntity.ok(document);
  }


  @RequestMapping(value = "documents/{id}/schedule", method = RequestMethod.PUT,
  consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  schedule(@ActiveUser CatalogueUser user, @PathVariable("id") String id)
  {
    userCanUpload(id);
    transitionIssueToSchedule(user, id);
    val document = uploadDocumentService.get(id);
    return ResponseEntity.ok(document);
  }

  @RequestMapping(value = "documents/{id}/reschedule", method = RequestMethod.PUT,
  consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  reschedule(@ActiveUser CatalogueUser user, @PathVariable("id") String id)
  {
    userCanUpload(id);
    transitionIssueToScheduled(user, id);
    val document = uploadDocumentService.get(id);
    return ResponseEntity.ok(document);
  }

  private void transitionIssueToSchedule(CatalogueUser user, String guid) {
    transitionIssueTo(user, guid, APPROVE);
    transitionIssueTo(user, guid, SCHEDULED, "%s has scheduled, ready for uploading");
  }

  private void transitionIssueToScheduled(CatalogueUser user, String guid) {
    transitionIssueTo(user, guid, HOLD);
    transitionIssueTo(user, guid, UNHOLD);
    transitionIssueTo(user, guid, APPROVE);
    transitionIssueTo(user, guid, SCHEDULED, "%s has reschduled, ready for uploading");
  }

  private void transitionIssueToStartProgress(CatalogueUser user, String guid) {
    transitionIssueTo(user, guid, START_PROGRESS, "%s has finished uploading the documents");
  }

  private void transitionIssueTo(CatalogueUser user, String guid, String status, String message) {
    val key = transitionIssueTo(user, guid, status);
    jiraService.comment(key, String.format(message, user.getEmail()));
  }

  private String transitionIssueTo(CatalogueUser user, String guid, String status) {
    val issues = jiraService.search(jql(guid));
    if (issues.size() != 1)
      throw new NonUniqueJiraIssue();
    val issue = issues.get(0);
    val key = issue.getKey();
    jiraService.transition(key, status);
    return key;
  }

  @ResponseStatus(value = HttpStatus.BAD_REQUEST,
      reason = "Can not finish, contact admin to resolve issue clash")
  static class NonUniqueJiraIssue extends RuntimeException {
    static final long serialVersionUID = 1L;
  }

  private void removeUploadPermission(CatalogueUser user, String guid)
      throws DocumentRepositoryException {
    MetadataDocument document = documentRepository.read(guid);
    MetadataInfo info = document.getMetadata();
    info.removePermission(Permission.UPLOAD, user.getUsername());
    document.setMetadata(info);
    documentRepository.save(
        user, document, guid, String.format("Permissions of %s changed.", guid));
  }

  private String jql(String guid) {
    String jqlTemplate = "project=eidchelp and component='data transfer' and cf[13250]=%s";
    return String.format(jqlTemplate, guid);
  }

  private void userCanUpload(String id) {
    if (!permissionService.userCanUpload(id))
      throw new PermissionDeniedException("Invalid Permissions");
  }

  @RequestMapping(value = "documents/{id}/accept-upload-file", method = RequestMethod.PUT,
      consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  acceptFile(@ActiveUser CatalogueUser user, @PathVariable("id") String id,
      @RequestParam("path") String path) {
    userCanUpload(id);
    val document = uploadDocumentService.accept(id, path);
    return ResponseEntity.ok(document);
  }

  @RequestMapping(value = "documents/{id}/validate-upload-file", method = RequestMethod.PUT,
      consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  validateFile(@ActiveUser CatalogueUser user, @PathVariable("id") String id,
      @RequestParam("path") String path) {
    userCanUpload(id);
    val document = uploadDocumentService.validateFile(id, path);
    return ResponseEntity.ok(document);
  }

  @RequestMapping(value = "documents/{id}/cancel", method = RequestMethod.PUT,
      consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  cancel(@ActiveUser CatalogueUser user, @PathVariable("id") String id, @RequestParam("filename") String filename) {
    userCanUpload(id);
    val document = uploadDocumentService.cancel(id, filename);
    return ResponseEntity.ok(document);
  }

  @RequestMapping(value = "documents/{id}/move-upload-file", method = RequestMethod.PUT,
      consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  moveFile(@ActiveUser CatalogueUser user, @PathVariable("id") String id,
      @RequestParam("to") String to, @RequestParam("filename") String filename) {
    userCanUpload(id);
    val document = uploadDocumentService.move(id, filename, to);
    return ResponseEntity.ok(document);
  }

  @RequestMapping(value = "documents/{id}/move-to-datastore", method = RequestMethod.PUT,
      consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  moveToDatastore(@ActiveUser CatalogueUser user, @PathVariable("id") String id) {
    userCanUpload(id);
    val document = uploadDocumentService.moveToDataStore(id);
    return ResponseEntity.ok(document);
  }

  @RequestMapping(value = "documents/{id}/validate", method = RequestMethod.PUT,
      consumes = UPLOAD_DOCUMENT_JSON_VALUE)
  public ResponseEntity<UploadDocument>
  validate(@ActiveUser CatalogueUser user, @PathVariable("id") String id) {
    userCanUpload(id);
    val document = uploadDocumentService.validate(id);
    return ResponseEntity.ok(document);
  }
}