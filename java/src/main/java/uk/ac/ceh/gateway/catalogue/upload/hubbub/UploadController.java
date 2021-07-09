package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.TEXT_CSV_VALUE;

@Controller
@Profile("upload:hubbub")
@Slf4j
@ToString
@RequestMapping("upload/{id}")
public class UploadController {
    // These transition ids are specific to the CT & EIDCHELP Jira project
    static final String START_PROGRESS = "751";
    static final String HOLD = "831";
    static final String UNHOLD = "811";
    static final String APPROVE = "711";
    static final String SCHEDULED = "741";
    static final String METADATA = "supporting-documents";
    static final String DATASTORE = "eidchub";
    static final String DROPBOX = "dropbox";
    private static final String PATH_TEMPLATE = "/%s/%s";

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
    @GetMapping
    public String getPage(
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
        //noinspection SpringMVCViewInspection
        return "html/upload/hubbub/upload-document";
    }

    @ResponseBody
    @PreAuthorize("@permission.toAccess(#user, #id, 'VIEW')")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public UploadDocument get(
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id,
        @RequestParam(value = "documents_page", defaultValue = "1") int documentsPage,
        @RequestParam(value = "datastore_page", defaultValue = "1") int datastorePage,
        @RequestParam(value = "supporting_documents_page", defaultValue = "1") int supportingDocumentsPage
    ) {
        log.debug("GETing {} (documentsPage={}, datastorePage={}, supportingDocumentsPage={})", id, documentsPage, datastorePage, supportingDocumentsPage);
        return uploadDocumentService.get(
            id,
            documentsPage,
            datastorePage,
            supportingDocumentsPage
        );
    }

    @SneakyThrows
    @PreAuthorize("@permission.toAccess(#user, #id, 'VIEW')")
    @GetMapping(produces = TEXT_CSV_VALUE)
    public void csv(
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id,
        HttpServletResponse response
    ) {
        response.setContentType(TEXT_CSV_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, format("attachment; filename=\"checksums_%s.csv\"", id));
        uploadDocumentService.csv(response.getWriter(), id);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping
    public void upload(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile multipartFile
    ) {
        uploadDocumentService.upload(id, multipartFile);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping
    public void delete(
            @PathVariable("id") String id,
            @RequestParam("path") String path
    ) {
        uploadDocumentService.delete(path);
    }

    @SneakyThrows
    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("finish")
    public void finish(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id
    ) {
        transitionIssueToStartProgress(user, id);
        removeUploadPermission(user, id);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("schedule")
    public void schedule(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id
    ) {
        transitionIssueToSchedule(user, id);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("reschedule")
    public void reschedule(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id
    ) {
        transitionIssueToScheduled(user, id);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("accept")
    public void accept(
            @PathVariable("id") String id,
            @RequestParam("path") String path
    ) {
        uploadDocumentService.accept(path);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("cancel")
    public void cancel(
        @PathVariable("id") String id,
        @RequestParam("path") String path
    ) {
        uploadDocumentService.cancel(path);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("validate")
    public void validate(
            @PathVariable("id") String id,
            @RequestParam(value = "path", required = false) Optional<String> possiblePath
    ) {
        possiblePath.ifPresentOrElse(
            uploadDocumentService::validate,
            () -> uploadDocumentService.validate(id)
        );
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("move-datastore")
    public void moveDatastore(
        @PathVariable("id") String id,
        @RequestParam("path") String path
    ) {
        uploadDocumentService.move(path, DATASTORE);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("move-metadata")
    public void moveMetadata(
            @PathVariable("id") String id,
            @RequestParam("path") String path
    ) {
        uploadDocumentService.move(path, METADATA);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("move-all-datastore")
    public void moveAllDatastore(
            @PathVariable("id") String id
    ) {
        uploadDocumentService.moveAllToDataStore(id);
    }

    private void transitionIssueToSchedule(CatalogueUser user, String guid) {
        transitionIssueTo(guid, APPROVE);
        transitionIssueTo(user, guid, SCHEDULED, "%s has scheduled, ready for uploading");
    }

    private void transitionIssueToScheduled(CatalogueUser user, String guid) {
        transitionIssueTo(guid, HOLD);
        transitionIssueTo(guid, UNHOLD);
        transitionIssueTo(guid, APPROVE);
        transitionIssueTo(user, guid, SCHEDULED, "%s has rescheduled, ready for uploading");
    }

    private void transitionIssueToStartProgress(CatalogueUser user, String guid) {
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
        val document = documentRepository.read(guid);
        val info = document.getMetadata();
        info.removePermission(Permission.UPLOAD, user.getUsername());
        document.setMetadata(info);
        documentRepository.save(
                user,
                document,
                guid,
                format("Permissions of %s changed. Removing upload permissions", guid)
        );
    }
}
