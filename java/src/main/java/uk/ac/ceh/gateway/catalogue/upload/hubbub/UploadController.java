package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import javax.servlet.http.HttpServletResponse;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.UPLOAD_DOCUMENT_JSON_VALUE;

@Controller
@Profile("upload:hubbub")
@Slf4j
@ToString
public class UploadController {
    private static final String START_PROGRESS = "751";
    private static final String HOLD = "831";
    private static final String UNHOLD = "811";
    private static final String APPROVE = "711";
    private static final String SCHEDULED = "741";

    private final UploadDocumentService uploadDocumentService;
    private final DocumentRepository documentRepository;
    private final JiraService jiraService;
    private final PermissionService permissionService;

    public UploadController(
            UploadDocumentService uploadDocumentService,
            DocumentRepository documentRepository,
            JiraService jiraService,
            PermissionService permissionService
    ) {
        this.uploadDocumentService = uploadDocumentService;
        this.documentRepository = documentRepository;
        this.jiraService = jiraService;
        this.permissionService = permissionService;
        log.info("Creating");
    }

    @SneakyThrows
    @PreAuthorize("@permission.userCanUpload(#id)")
    @GetMapping("upload/{id}")
    public String getUploadDocumentPage(
            @PathVariable("id") String id,
            Model model
    ) {
        log.info("Requesting UploadDocument page for {}", id);
        model.addAttribute("id", id);

        val geminiDocument = (GeminiDocument) documentRepository.read(id);
        model.addAttribute("title", geminiDocument.getTitle());

        model.addAttribute("isAdmin", permissionService.userIsAdmin());

        val possibleDataTransfer = jiraService.retrieveDataTransferIssue(id);
        model.addAttribute("hasDataTransfer", possibleDataTransfer.isPresent());
        val dataTransfer = possibleDataTransfer.orElseGet(JiraIssue::new);
        model.addAttribute("isOpen", dataTransfer.isOpen());
        model.addAttribute("isScheduled", dataTransfer.isScheduled());
        model.addAttribute("isInProgress", dataTransfer.isInProgress());

        log.debug("Model is {}", model);
        return "html/upload/hubbub/upload-document";
    }

    @GetMapping(value = "documents/{id}", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> get(
            @PathVariable("id") String id,
            @RequestParam(value = "documents_page", defaultValue = "1") int documentsPage,
            @RequestParam(value = "datastore_page", defaultValue = "1") int datastorePage,
            @RequestParam(value = "supporting_documents_page", defaultValue = "1") int supportingDocumentsPage
    ) {
        log.debug("GETing {} (documentsPage={}, datastorePage={}, supportingDocumentsPage={})", id, documentsPage, datastorePage, supportingDocumentsPage);
        return ResponseEntity.ok(
                uploadDocumentService.get(
                        id,
                        documentsPage,
                        datastorePage,
                        supportingDocumentsPage
                )
        );
    }

    @SneakyThrows
    @GetMapping("upload/documents/csv/{id}")
    public void exportCSV(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id,
            HttpServletResponse response
    ) {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, format("attachment; filename=\"checksums_%s.csv\"", id));
        uploadDocumentService.getCsv(response.getWriter(), id);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PostMapping("documents/{id}/add-upload-document")
    public ResponseEntity<UploadDocument> addFile(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                uploadDocumentService.add(
                        id,
                        file.getOriginalFilename(),
                        file
                )
        );
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PutMapping(value = "documents/{id}/delete-upload-file", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> deleteFile(
            @PathVariable("id") String id,
            @RequestParam("filename") String filename
    ) {
        return ResponseEntity.ok(uploadDocumentService.delete(id, filename));
    }

    @SneakyThrows
    @PreAuthorize("@permission.userCanUpload(#id)")
    @PutMapping(value = "documents/{id}/finish", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> finish(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id
    ) {
        transitionIssueToStartProgress(user, id);
        removeUploadPermission(user, id);
        return ResponseEntity.ok(uploadDocumentService.get(id));
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PutMapping(value = "documents/{id}/schedule", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> schedule(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id
    ) {
        transitionIssueToSchedule(user, id);
        return ResponseEntity.ok(uploadDocumentService.get(id));
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PutMapping(value = "documents/{id}/reschedule", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> reschedule(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id
    ) {
        transitionIssueToScheduled(user, id);
        return ResponseEntity.ok(uploadDocumentService.get(id));
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PutMapping(value = "documents/{id}/accept-upload-file", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> acceptFile(
            @PathVariable("id") String id,
            @RequestParam("path") String path
    ) {
        return ResponseEntity.ok(uploadDocumentService.accept(id, path));
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PutMapping(value = "documents/{id}/validate-upload-file", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> validateFile(
            @PathVariable("id") String id,
            @RequestParam("path") String path
    ) {
        return ResponseEntity.ok(uploadDocumentService.validateFile(id, path));
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PutMapping(value = "documents/{id}/cancel", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> cancel(
            @PathVariable("id") String id,
            @RequestParam("filename") String filename
    ) {
        return ResponseEntity.ok(uploadDocumentService.cancel(id, filename));
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PutMapping(value = "documents/{id}/move-upload-file", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> moveFile(
            @PathVariable("id") String id,
            @RequestParam("to") String to,
            @RequestParam("filename") String filename
    ) {
        return ResponseEntity.ok(uploadDocumentService.move(id, filename, to));
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PutMapping(value = "documents/{id}/move-to-datastore", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> moveToDatastore(
            @PathVariable("id") String id
    ) {
        return ResponseEntity.ok(uploadDocumentService.moveToDataStore(id));
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PutMapping(value = "documents/{id}/validate", produces = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<UploadDocument> validate(
            @PathVariable("id") String id
    ) {
        return ResponseEntity.ok(uploadDocumentService.validate(id));
    }

    private void transitionIssueToSchedule(CatalogueUser user,String guid) {
        transitionIssueTo(guid, APPROVE);
        transitionIssueTo(user, guid, SCHEDULED, "%s has scheduled, ready for uploading");
    }

    private void transitionIssueToScheduled(CatalogueUser user,String guid) {
        transitionIssueTo(guid, HOLD);
        transitionIssueTo(guid, UNHOLD);
        transitionIssueTo(guid, APPROVE);
        transitionIssueTo(user, guid, SCHEDULED, "%s has rescheduled, ready for uploading");
    }

    private void transitionIssueToStartProgress(CatalogueUser user,String guid) {
        transitionIssueTo(user, guid, START_PROGRESS, "%s has finished uploading the documents");
    }

    private void transitionIssueTo(CatalogueUser user, String guid, String status, String message) {
        val key = transitionIssueTo(guid, status);
        jiraService.comment(key, format(message, user.getEmail()));
    }

    private String transitionIssueTo(String guid, String status) {
        val possibleIssue = jiraService.retrieveDataTransferIssue(guid);
        if (possibleIssue.isPresent()) {
            val key = possibleIssue.get().getKey();
            jiraService.transition(key, status);
            return key;
        } else {
            throw new RuntimeException(format("Cannot transition %s to %s", guid, status));
        }
    }

    private void removeUploadPermission(CatalogueUser user, String guid)
            throws DocumentRepositoryException {
        MetadataDocument document = documentRepository.read(guid);
        MetadataInfo info = document.getMetadata();
        info.removePermission(Permission.UPLOAD, user.getUsername());
        document.setMetadata(info);
        documentRepository.save(
                user,
                document,
                guid,
                format("Permissions of %s changed.", guid)
        );
    }
}
