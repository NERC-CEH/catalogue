package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import lombok.val;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.model.JiraIssue;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.DocumentUploadService;
import uk.ac.ceh.gateway.catalogue.services.JiraService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.ceh.gateway.catalogue.services.PloneDataDepositService;

@Controller
public class UploadController {
    private final DocumentUploadService documentUploadService;
    private final JiraService jiraService;
    private final PermissionService permissionService;
    private final DocumentRepository documentRepository;
    private final PloneDataDepositService ploneDataDepositService;

    private static final String START_PROGRESS = "751";

    @Autowired
    public UploadController(DocumentUploadService documentUploadService, JiraService jiraService,
            PermissionService permissionService, DocumentRepository documentRepository, PloneDataDepositService ploneDataDepositService) {
        this.documentUploadService = documentUploadService;
        this.jiraService = jiraService;
        this.permissionService = permissionService;
        this.documentRepository = documentRepository;
        this.ploneDataDepositService = ploneDataDepositService;
    }

    private String getStatus(List<JiraIssue> issues) {
        String message = "";
        if (issues.size() == 0)
            message = "No issue exists for this document. Contact an admin to create one.";
        else if (issues.size() > 1)
            message = "There is an issue clash for this document. Contact an admin to resolve.";
        else {
            val issue = issues.get(0);
            val status = issue.getStatus();

            if (status.equals("open") || status.equals("approved"))
                message = "Awaiting scheduling from admin. Try again later.";
            else if (status.equals("in progress"))
                message = "Currently being checked. Awaiting approval from admin.";
            else if (status.equals("resolved") || status.equals("closed"))
                message = "This is finsihed. No further action required.";
            else if (status.equals("on hold"))
                message = "This issue is blocked. Contact an admin to resolve.";
            else if (status.equals("scheduled"))
                message = "You can now upload your files for this document.";
        }
        return message;
    }

    private String jql(String guid) {
        String jqlTemplate = "project=eidchelp and component='data transfer' and labels=%s";
        return String.format(jqlTemplate, guid);
    }

    @RequestMapping(value = "upload/{guid}", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView documentsUpload(@PathVariable("guid") String guid)
            throws IOException, DocumentRepositoryException {
        Map<String, Object> model = new HashMap<>();

        val issues = jiraService.search(jql(guid));
        val isScheduled = issues.size() == 1 && issues.get(0).getStatus().equals("scheduled");
        model.put("isScheduled", isScheduled);
        val isInProgress = issues.size() == 1 && issues.get(0).getStatus().equals("in progress");
        model.put("isInProgress", isInProgress);
        model.put("status", getStatus(issues));

        val documentUpload = documentUploadService.get(guid);
        model.put("documentUpload", documentUpload);
        val files = documentUpload.getFiles();
        model.put("files", files);

        boolean userCanUpload = permissionService.userCanUpload(guid);
        boolean canUpload = userCanUpload && (isScheduled || isInProgress);
        model.put("userCanUpload", userCanUpload);
        model.put("canUpload", canUpload);

        return new ModelAndView("/html/documents-upload.html.tpl", model);
    }

    private void transitionIssueToStartProgress(CatalogueUser user, String guid) {
        val issues = jiraService.search(jql(guid));
        if (issues.size() != 1)
            throw new NonUniqueJiraIssue();
        val issue = issues.get(0);
        val key = issue.getKey();
        jiraService.transition(key, START_PROGRESS);
        jiraService.comment(key, String.format("%s has finished uploading the documents to dropbox", user.getEmail()));
    }

    private void removeUploadPermission(CatalogueUser user, String guid) throws DocumentRepositoryException {
        MetadataDocument document = documentRepository.read(guid);
        MetadataInfo info = document.getMetadata();
        info.removePermission(Permission.UPLOAD, user.getUsername());
        document.setMetadata(info);
        documentRepository.save(user, document, guid, String.format("Permissions of %s changed.", guid));
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/finish", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> finish(@ActiveUser CatalogueUser user, @PathVariable("guid") String guid)
            throws DocumentRepositoryException, IOException {
        transitionIssueToStartProgress(user, guid);
        removeUploadPermission(user, guid);
        ploneDataDepositService.processDataDeposit(documentUploadService.get(guid));
        val response = new HashMap<String, String>();
        response.put("message", "awaiting approval from admin");
        return response;
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/add", method = RequestMethod.POST)
    @ResponseBody
    public DocumentUpload addFile(@PathVariable("guid") String guid, @RequestParam("file") MultipartFile file)
            throws IOException, DocumentRepositoryException {
        try (InputStream in = file.getInputStream()) {
            documentUploadService.add(guid, file.getOriginalFilename(), in);
            return documentUploadService.get(guid);
        }
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/delete", method = RequestMethod.POST)
    @ResponseBody
    public DocumentUpload deleteFile(@PathVariable("guid") String guid, @RequestParam("file") String file)
            throws IOException, DocumentRepositoryException {
        documentUploadService.delete(guid, file);
        return documentUploadService.get(guid);
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/change", method = RequestMethod.POST)
    @ResponseBody
    public DocumentUpload change(@PathVariable("guid") String guid, @RequestParam("file") String file,
            @RequestParam("type") String type) throws IOException, DocumentRepositoryException {
        documentUploadService.changeFileType(guid, file, DocumentUpload.Type.valueOf(type));
        return documentUploadService.get(guid);
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/accept-invalid", method = RequestMethod.POST)
    @ResponseBody
    public DocumentUpload acceptInvalid(@PathVariable("guid") String guid, @RequestParam("file") String file) throws IOException, DocumentRepositoryException {
        documentUploadService.acceptInvalid(guid, file);
        return documentUploadService.get(guid);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Can not finish, contact admin to resolve issue clash")
    class NonUniqueJiraIssue extends RuntimeException {
        static final long serialVersionUID = 1L;
    }
}