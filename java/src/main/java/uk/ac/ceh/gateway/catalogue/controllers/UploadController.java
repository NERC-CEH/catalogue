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

import com.google.common.collect.Maps;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.ceh.gateway.catalogue.services.PloneDataDepositService;

@Slf4j
@Controller
public class UploadController {
    private final DocumentUploadService documentsUploadService;
    private final DocumentUploadService datastoreUploadService;
    private final DocumentUploadService ploneUploadService;
    private final JiraService jiraService;
    private final PermissionService permissionService;
    private final DocumentRepository documentRepository;
    private final PloneDataDepositService ploneDataDepositService;

    private static final String START_PROGRESS = "751";

    private static final Map<String, DocumentUploadService> services = Maps.newConcurrentMap();

    @Autowired
    public UploadController(DocumentUploadService documentsUploadService, DocumentUploadService datastoreUploadService,
            DocumentUploadService ploneUploadService, JiraService jiraService, PermissionService permissionService,
            DocumentRepository documentRepository, PloneDataDepositService ploneDataDepositService) {
        this.documentsUploadService = documentsUploadService;
        this.datastoreUploadService = datastoreUploadService;
        this.ploneUploadService = ploneUploadService;
        this.jiraService = jiraService;
        this.permissionService = permissionService;
        this.documentRepository = documentRepository;
        this.ploneDataDepositService = ploneDataDepositService;

        services.put("documents", documentsUploadService);
        services.put("datastore", datastoreUploadService);
        services.put("plone", ploneUploadService);
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
            else if (status.equals("scheduled"))
                message = "This has been scheduled. You can now upload your files for this document.";
            else if (status.equals("in progress"))
                message = "This is now in progress. Awaiting resolution from admin.";
        }
        return message;
    }

    private String jql(String guid) {
        String jqlTemplate = "project=eidchelp and component='data transfer' and labels=%s";
        return String.format(jqlTemplate, guid);
    }

    private boolean isJiraStatus(List<JiraIssue> issues, String status) {
        return issues.size() == 1 && issues.get(0).getStatus().equals(status);
    }

    private Map<String, DocumentUpload> get(String guid) throws IOException, DocumentRepositoryException {
        Map<String, DocumentUpload> value = Maps.newHashMap();
        value.put("documents", documentsUploadService.get(guid));
        value.put("datastore", datastoreUploadService.get(guid));
        value.put("plone", ploneUploadService.get(guid));
        return value;
    }

    @RequestMapping(value = "upload/{guid}", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView documentsUpload(@PathVariable("guid") String guid)
            throws IOException, DocumentRepositoryException {
        Map<String, Object> model = new HashMap<>();

        val issues = jiraService.search(jql(guid));

        val isScheduled = isJiraStatus(issues, "scheduled");
        model.put("isScheduled", isScheduled);

        val isInProgress = isJiraStatus(issues, "in progress");
        model.put("isInProgress", isInProgress);

        val isResolved = isJiraStatus(issues, "resolved");
        model.put("isResolved", isResolved);

        val isClosed = isJiraStatus(issues, "closed");
        model.put("isClosed", isClosed);

        model.put("status", getStatus(issues));

        model.put("documents", documentsUploadService.get(guid));
        model.put("datastore", datastoreUploadService.get(guid));
        model.put("plone", ploneUploadService.get(guid));

        boolean userCanUpload = permissionService.userCanUpload(guid);
        boolean userCanView = permissionService.userCanView(guid);
        boolean canUpload = userCanUpload && (isScheduled || isInProgress);
        model.put("userCanView", userCanView);
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
        try {
            ploneDataDepositService.addOrUpdate(documentsUploadService.get(guid));
        } catch (Exception ignoreError) {

        }
        val response = new HashMap<String, String>();
        response.put("message", "awaiting approval from admin");
        return response;
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/add/{name}", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, DocumentUpload> addFile(@PathVariable("guid") String guid,
            @RequestParam("file") MultipartFile file, @PathVariable("name") String name)
            throws IOException, DocumentRepositoryException {
        try (InputStream in = file.getInputStream()) {
            services.get(name).add(guid, file.getOriginalFilename(), in);
            return get(guid);
        }
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/delete/{name}", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, DocumentUpload> deleteFile(@PathVariable("guid") String guid, @RequestParam("file") String file,
            @PathVariable("name") String name) throws IOException, DocumentRepositoryException {
        services.get(name).delete(guid, file);
        return get(guid);
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/accept-invalid/{name}", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, DocumentUpload> acceptInvalid(@PathVariable("guid") String guid,
            @RequestParam("file") String file, @PathVariable("name") String name)
            throws IOException, DocumentRepositoryException {
        services.get(name).acceptInvalid(guid, file);
        return get(guid);
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/move", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, DocumentUpload> move(@PathVariable("guid") String guid, @RequestParam("file") String file,
            @RequestParam("from") String from, @RequestParam("to") String to)
            throws IOException, DocumentRepositoryException {
        services.get(from).move(guid, file, services.get(to));
        return get(guid);
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/move-all", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, DocumentUpload> moveAll(@PathVariable("guid") String guid, @RequestParam("files[]") String[] files,
            @RequestParam("from") String from, @RequestParam("to") String to)
            throws IOException, DocumentRepositoryException {
        val fromService = services.get(from);
        val toService = services.get(to);
        for(val file : files) fromService.move(guid, file, toService);
        return get(guid);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Can not finish, contact admin to resolve issue clash")
    class NonUniqueJiraIssue extends RuntimeException {
        static final long serialVersionUID = 1L;
    }
}