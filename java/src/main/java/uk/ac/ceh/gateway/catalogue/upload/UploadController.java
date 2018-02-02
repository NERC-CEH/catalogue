package uk.ac.ceh.gateway.catalogue.upload;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import lombok.AllArgsConstructor;
import lombok.val;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.PermissionDeniedException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.JiraService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;
import java.io.IOException;
import java.io.InputStream;
import uk.ac.ceh.gateway.catalogue.services.PloneDataDepositService;
import uk.ac.ceh.gateway.catalogue.upload.UploadDocumentService;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Controller
@AllArgsConstructor
public class UploadController  {

    private static final String START_PROGRESS = "751";

    private final UploadDocumentService uploadDocumentService;
    private final PermissionService permissionService;
    private final DocumentRepository documentRepository;
    private final JiraService jiraService;
    private final PloneDataDepositService ploneDataDepositService;

    @RequestMapping(value = "upload/{id}", method = RequestMethod.GET)
    @ResponseBody
    public RedirectView createOrGetUploadDocument(
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id
    ) throws DocumentRepositoryException {
        val geminiDocument = (GeminiDocument) documentRepository.read(id);
        val canUpload = permissionService.userCanUpload(id);
        val canView = permissionService.userCanView(id);
        val uploadId = geminiDocument.getUploadId();
        val exists = uploadId != null;

        if (canUpload && !exists) {
            val uploadDocument = uploadDocumentService.create(user, geminiDocument);
            return new RedirectView(String.format("/documents/%s", uploadDocument.getId()));
        } else if ((canView || canUpload) && exists) {
            val uploadDocument = documentRepository.read(uploadId);
            return new RedirectView(String.format("/documents/%s", uploadDocument.getId()));
        } else {
            throw new PermissionDeniedException("Permissions denied");
        }
    }
    
    @RequestMapping(value = "documents/{id}/add-upload-document", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<UploadDocument> addFile(
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id,
        @RequestParam("file") MultipartFile file
    ) throws IOException, DocumentRepositoryException {
        val document = (UploadDocument) documentRepository.read(id);
        userCanUpload(document);
        try (InputStream in = file.getInputStream()) {        
            val filename = file.getOriginalFilename();
            uploadDocumentService.add(user, document, filename, in);
        }
        return ResponseEntity.ok(document);
    }

    @RequestMapping(value = "documents/{id}/delete-upload-file", method = RequestMethod.PUT, consumes = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<MetadataDocument> deleteFile(
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id,
        @RequestParam("filename") String filename,
        @RequestBody UploadDocument document
    ) throws DocumentRepositoryException {
        userCanUpload(document);
        uploadDocumentService.delete(user, document, filename);
        return ResponseEntity.ok(document);
    }

    @RequestMapping(value = "documents/{id}/finish", method = RequestMethod.PUT, consumes = UPLOAD_DOCUMENT_JSON_VALUE)
    public ResponseEntity<MetadataDocument> finish(
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id,
        @RequestBody UploadDocument document
    ) throws DocumentRepositoryException {
        userCanUpload(document);
        val parentId = document.getParentId();
        transitionIssueToStartProgress(user, parentId);
        removeUploadPermission(user, document.getParentId());
        // updatePlone(id);
        return ResponseEntity.ok(document);
    }

    private void transitionIssueToStartProgress(CatalogueUser user, String guid) {
        val issues = jiraService.search(jql(guid));
        if (issues.size() != 1) throw new NonUniqueJiraIssue();
        val issue = issues.get(0);
        val key = issue.getKey();
        jiraService.transition(key, START_PROGRESS);
        jiraService.comment(key, String.format("%s has finished uploading the documents to dropbox", user.getEmail()));
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Can not finish, contact admin to resolve issue clash")
    class NonUniqueJiraIssue extends RuntimeException {
        static final long serialVersionUID = 1L;
    }

    private void removeUploadPermission(CatalogueUser user, String guid) throws DocumentRepositoryException {
        MetadataDocument document = documentRepository.read(guid);
        MetadataInfo info = document.getMetadata();
        info.removePermission(Permission.UPLOAD, user.getUsername());
        document.setMetadata(info);
        documentRepository.save(user, document, guid, String.format("Permissions of %s changed.", guid));
    }

    private String jql(String guid) {
        String jqlTemplate = "project=eidchelp and component='data transfer' and labels=%s";
        return String.format(jqlTemplate, guid);
    }

    private void userCanUpload (UploadDocument document) {
        if (!permissionService.userCanUpload(document.getParentId())) throw new PermissionDeniedException("Invalid Permissions");
    }
}