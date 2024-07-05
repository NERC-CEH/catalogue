package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.TEXT_CSV_VALUE;

@SuppressWarnings("SpringMVCViewInspection")
@Controller
@Profile("upload:hubbub")
@Slf4j
@ToString(onlyExplicitlyIncluded = true)
@RequestMapping("upload/{datasetId}")
public class UploadController {
    // These transition ids are specific to the CT & EIDCHELP Jira project
    static final String START_PROGRESS = "751";
    static final String HOLD = "831";
    static final String UNHOLD = "811";
    static final String APPROVE = "711";
    static final String SCHEDULED = "741";

    static final String DATASTORE = "eidchub";
    static final String DROPBOX = "dropbox";
    static final String METADATA = "supporting-documents";

    private final UploadService uploadService;
    private final DocumentRepository documentRepository;
    private final JiraService jiraService;
    private final PermissionService permissionService;
    private final ObjectMapper mapper;
    @ToString.Include
    private final String maxFileSize;

    public UploadController(
        UploadService uploadService,
        DocumentRepository documentRepository,
        JiraService jiraService,
        PermissionService permissionService,
        ObjectMapper mapper,
        @Value("${spring.servlet.multipart.max-file-size}") String maxFileSize
    ) {
        this.uploadService = uploadService;
        this.documentRepository = documentRepository;
        this.jiraService = jiraService;
        this.permissionService = permissionService;
        this.mapper = mapper;
        this.maxFileSize = maxFileSize;
        log.info("Creating {}", this);
    }

    @SneakyThrows
    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @GetMapping
    public String getPage(
        @PathVariable("datasetId") String datasetId,
        Model model
    ) {
        log.info("Requesting upload page for {}", datasetId);
        model.addAttribute("id", datasetId);

        val geminiDocument = (GeminiDocument) documentRepository.read(datasetId);
        model.addAttribute("title", geminiDocument.getTitle());

        val isAdmin = permissionService.userIsAdmin();
        model.addAttribute("isAdmin", isAdmin);

        val possibleDataTransfer = jiraService.retrieveDataTransferIssue(datasetId);
        model.addAttribute("hasDataTransfer", possibleDataTransfer.isPresent());
        val dataTransfer = possibleDataTransfer.orElseGet(JiraIssue::new);
        model.addAttribute("isOpen", dataTransfer.isOpen());
        model.addAttribute("isScheduled", dataTransfer.isScheduled());
        model.addAttribute("isInProgress", dataTransfer.isInProgress());
        model.addAttribute("maxFileSize", maxFileSize);

        if (dataTransfer.isScheduled()) {
            addHubbubResponse(model, DROPBOX, datasetId, DROPBOX);
            if (isAdmin) {
                addHubbubResponse(model, "datastore", datasetId, DATASTORE);
                addHubbubResponse(model, "metadata", datasetId, METADATA);
            }
        } else if (dataTransfer.isInProgress() || isAdmin) {
            addHubbubResponse(model, DROPBOX, datasetId, DROPBOX);
            addHubbubResponse(model, "datastore", datasetId, DATASTORE);
            addHubbubResponse(model, "metadata", datasetId, METADATA);
        }

        log.debug("Model keys: {}", model.asMap().keySet());
        return "html/upload/hubbub/upload";
    }

    @SneakyThrows
    private void addHubbubResponse(Model model, String attribute, String datasetId, String datastore) {
        try {
            val response = uploadService.get(datasetId, datastore, 1, 20);
            model.addAttribute(attribute, mapper.writeValueAsString(response));
        } catch (RestClientException ex) {
            log.debug("{}, {} not added to {}.\n{}", datasetId, datastore, attribute, ex.getMessage());
        }
    }

    @ResponseBody
    @PreAuthorize("@permission.toAccess(#user, #datasetId, 'VIEW')")
    @GetMapping("{datastore}")
    public HubbubResponse get(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId,
        @PathVariable("datastore") String datastore,
        @RequestParam(value = "path", required = false) Optional<String> possiblePath,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        if (possiblePath.isPresent()) {
            return uploadService.get(datasetId, datastore, possiblePath.get());
        } else {
            return uploadService.get(datasetId, datastore, page, size);
        }
    }

    @SneakyThrows
    @PreAuthorize("@permission.toAccess(#user, #datasetId, 'VIEW')")
    @GetMapping(produces = TEXT_CSV_VALUE)
    public void csv(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId,
        HttpServletResponse response
    ) {
        response.setContentType(TEXT_CSV_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, format("attachment; filename=\"checksums_%s.csv\"", datasetId));
        uploadService.csv(response.getWriter(), datasetId);
    }

    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping
    public void upload(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId,
        @RequestParam("file") MultipartFile multipartFile
    ) {
        uploadService.upload(datasetId, user.getUsername(), multipartFile);
    }

    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("{datastore}")
    public void delete(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId,
        @PathVariable("datastore") String datastore,
        @RequestParam("path") String path
    ) {
        uploadService.delete(datasetId, datastore, path, user.getUsername());
    }

    @SneakyThrows
    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("finish")
    public void finish(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId
    ) {
        transitionIssueToStartProgress(user, datasetId);
        removeUploadPermission(user, datasetId);
    }

    @SneakyThrows
    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("hash")
    public void hashDropbox(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId
    ) {
        uploadService.hashDropbox(datasetId, user.getUsername());
    }

    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("register")
    public void register(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId
    ) {
        uploadService.register(datasetId, user.getUsername());
    }

    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("{datastore}/unregister")
    public void unregister(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId,
        @PathVariable("datastore") String datastore,
        @RequestParam("path") String path
    ) {
        uploadService.unregister(datasetId, datastore, path, user.getUsername());
    }

    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("schedule")
    public void schedule(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId
    ) {
        transitionIssueToSchedule(user, datasetId);
    }

    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("reschedule")
    public void reschedule(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId
    ) {
        transitionIssueToScheduled(user, datasetId);
    }

    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("{datastore}/accept")
    public void accept(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId,
        @PathVariable("datastore") String datastore,
        @RequestParam("path") String path
    ) {
        uploadService.accept(datasetId, datastore, path, user.getUsername());
    }

    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("{datastore}/cancel")
    public void cancel(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId,
        @PathVariable("datastore") String datastore,
        @RequestParam("path") String path
    ) {
        uploadService.cancel(datasetId, datastore, path, user.getUsername());
    }

    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("{datastore}/validate")
    public void validate(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId,
        @PathVariable("datastore") String datastore,
        @RequestParam(value="path", required=false) Optional<String> possiblePath
    ) {
        uploadService.validate(datasetId, datastore, possiblePath, user.getUsername());
    }

    @PreAuthorize("@permission.userCanUpload(#datasetId)")
    @ResponseStatus(NO_CONTENT)
    @PostMapping("{datastore}/move")
    public void move(
        @ActiveUser CatalogueUser user,
        @PathVariable("datasetId") String datasetId,
        @PathVariable("datastore") String datastore,
        @RequestParam(value="path", required=false) Optional<String> possiblePath,
        @RequestParam("to") String to
    ) {
        uploadService.move(datasetId, datastore, possiblePath, user.getUsername(), to);
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
