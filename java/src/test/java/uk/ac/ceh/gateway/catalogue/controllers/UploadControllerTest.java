package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.mockito.junit.MockitoJUnitRunner;
import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.model.DocumentUploadFile;
import uk.ac.ceh.gateway.catalogue.model.JiraIssue;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.DocumentUploadService;
import uk.ac.ceh.gateway.catalogue.services.JiraService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import org.springframework.web.multipart.MultipartFile;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import uk.ac.ceh.gateway.catalogue.services.PloneDataDepositService;

@RunWith(MockitoJUnitRunner.class)
public class UploadControllerTest {

    @Mock
    private JiraService jiraService;

    @Mock
    private PermissionService permissionservice;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentUploadService documentUploadService;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private InputStream inputStream;

    @Mock
    private CatalogueUser user;

    @Mock
    private MetadataDocument document;
    
    @Mock
    private PloneDataDepositService ploneDataDepositService;

    private MetadataInfo info;

    @InjectMocks
    private UploadController controller;

    private DocumentUpload documentUpload;
    private JiraIssue issue;

    private JiraIssue createJiraIssue(String statusName) {
        issue = new JiraIssue();
        issue.setKey("key");
        val fields = new HashMap<String, Object>();
        val status = new HashMap<String, String>();
        status.put("name", statusName);
        fields.put("status", status);
        issue.setFields(fields);
        return issue;
    }

    @Before
    @SneakyThrows
    public void before() {
        documentUpload = new DocumentUpload("title", "type", "guid", "path");
        doReturn(documentUpload).when(documentUploadService).get(anyString());

        doReturn("uploaded").when(ploneDataDepositService).addOrUpdate(documentUpload);

        doReturn(inputStream).when(multipartFile).getInputStream();
        doReturn("filename").when(multipartFile).getOriginalFilename();

        doReturn("email").when(user).getEmail();
        doReturn("username").when(user).getUsername();

        issue = createJiraIssue("status-name");
        doReturn(Collections.singletonList(issue)).when(jiraService).search(anyString());

        doReturn(document).when(documentRepository).read(anyString());

        Multimap<Permission, String> permissions = ArrayListMultimap.create();
        permissions.put(Permission.UPLOAD, "username");
        info = MetadataInfo.builder().permissions(permissions).build();
        doReturn(info).when(document).getMetadata();

    }

    @SneakyThrows
    private DocumentUpload deleteAFile() {
        return controller.deleteFile("guid", "filename");
    }

    @Test
    @SneakyThrows
    public void deletingAFile_returnsTheDocumentUpload() {
        val actual = deleteAFile();

        assertThat(actual, equalTo(documentUpload));
    }

    @Test
    @SneakyThrows
    public void deletingAFile_deletesTheFileWithTheFileUploadService() {
        deleteAFile();

        verify(documentUploadService).delete(eq("guid"), eq("filename"));
    }

    @SneakyThrows
    private DocumentUpload addAFile() {
        return controller.addFile("guid", multipartFile);
    }

    @Test
    @SneakyThrows
    public void addingAFile_uploadsDataWithTheFileUploadService() {
        addAFile();

        verify(documentUploadService).add(eq("guid"), eq("filename"), eq(inputStream));
    }

    @Test
    @SneakyThrows
    public void addingAFile_returnsTheDocumentUpload() {
        val actual = addAFile();

        assertThat(actual, equalTo(documentUpload));
    }

    @SneakyThrows
    private Map<String, String> finishAnUpload() {
        return controller.finish(user, "guid");
    }

    @Test
    @SneakyThrows
    public void finishingAnUpload_removesTheUploadPermission() {
        finishAnUpload();

        val idenities = info.getIdentities(Permission.UPLOAD);
        assertThat(idenities.size(), equalTo(0));

        verify(document).setMetadata(eq(info));
        verify(documentRepository).save(eq(user), eq(document), eq("guid"), eq("Permissions of guid changed."));
    }

    @Test
    @SneakyThrows
    public void finishingAnUpload_updatesJiraIssue() {
        finishAnUpload();

        verify(jiraService).transition(eq("key"), eq("751"));
        verify(jiraService).comment(eq("key"), eq("email has finished uploading the documents to dropbox"));
    }

    @Test
    @SneakyThrows
    public void finishingAnUpload_returnsAMessage() {
        val actual = finishAnUpload();
        assertThat(actual.get("message"), equalTo("awaiting approval from admin"));
    }

    @Test(expected = UploadController.NonUniqueJiraIssue.class)
    @SneakyThrows
    public void finishAnUpload_throwsUniqueJiraIssueWhenNoIssuesNotFound() {
        val issues = new ArrayList<JiraIssue>();
        doReturn(issues).when(jiraService).search(anyString());
        finishAnUpload();
    }

    @Test(expected = UploadController.NonUniqueJiraIssue.class)
    @SneakyThrows
    public void finishAnUpload_throwsUniqueJiraIssueWhenMoreThanOneIssueFound() {
        val issues = Arrays.asList(issue, issue);
        doReturn(issues).when(jiraService).search(anyString());
        finishAnUpload();
    }

    @Test
    @SneakyThrows
    public void documentUploadView_usesDocumentUploadTemplate() {
        val actual = controller.documentsUpload("guid");
        assertThat(actual.getViewName(), equalTo("/html/documents-upload.html.tpl"));
    }

    @SneakyThrows
    private Map<String, Object> documentUploadModel() {
        return controller.documentsUpload("guid").getModel();
    }

    @Test
    @SneakyThrows
    public void documentUploadView_addsAllTheDocumentUpload() {
        val model = documentUploadModel();

        assertThat(model.get("documentUpload"), equalTo(documentUpload));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_addsAllFiles() {
        val model = documentUploadModel();

        assertThat(model.get("files"), equalTo(documentUpload.getFiles()));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_hasStatusOfNoIssueIfNoMatchingJiraIssue() {
        val issues = new ArrayList<JiraIssue>();
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("status"), equalTo("No issue exists for this document. Contact an admin to create one."));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_hasStatusOfNoIssueIfMoreThanOneJiraIssueIsFound() {
        val issues = Arrays.asList(issue, issue);
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("status"),
                equalTo("There is an issue clash for this document. Contact an admin to resolve."));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_hasCorrectStatusForEachAvailableJiraStatus() {
        val statuses = new HashMap<String, String>();
        statuses.put("open", "Awaiting scheduling from admin. Try again later.");
        statuses.put("approved", "Awaiting scheduling from admin. Try again later.");
        statuses.put("in progress", "Currently being checked. Awaiting approval from admin.");
        statuses.put("resolved", "This is finsihed. No further action required.");
        statuses.put("closed", "This is finsihed. No further action required.");
        statuses.put("on hold", "This issue is blocked. Contact an admin to resolve.");
        statuses.put("scheduled", "You can now upload your files for this document.");

        for (Map.Entry<String, String> entry : statuses.entrySet()) {
            val issues = Arrays.asList(createJiraIssue(entry.getKey()));
            doReturn(issues).when(jiraService).search(anyString());
            val model = documentUploadModel();

            assertThat(String.format("when using status %s", entry.getKey()), model.get("status"),
                    equalTo(entry.getValue()));
        }
    }

    @Test
    @SneakyThrows
    public void documentUploadView_isScheduledIfHasOneIssueWhichIsScheduled() {
        val issues = Arrays.asList(createJiraIssue("scheduled"));
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("isScheduled"), equalTo(true));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_isInProgresIfHasOneIssueWhichIsScheduled() {
        val issues = Arrays.asList(createJiraIssue("in progress"));
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("isInProgress"), equalTo(true));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_isNotScheduledIfHasOneIssueWhichIsNotScheduled() {
        val issues = Arrays.asList(createJiraIssue("in progress"));
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("isScheduled"), equalTo(false));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_isNotScheduledIfHasNoIssues() {
        val issues = Arrays.asList();
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("isScheduled"), equalTo(false));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_isNotInProgressIfHasOneIssueWhichisNotInProgress() {
        val issues = Arrays.asList(createJiraIssue("closed"));
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("isInProgress"), equalTo(false));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_isNotInProgressIfHasNoIssues() {
        val issues = Arrays.asList();
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("isScheduled"), equalTo(false));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_isNotScheduledIfHasMoreThanOneIssue() {
        val issues = Arrays.asList(createJiraIssue("scheduled"), createJiraIssue("scheduled"));
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("isScheduled"), equalTo(false));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_canUploadIseUserCanUploadAndIsScheduled() {
        doReturn(true).when(permissionservice).userCanUpload(anyString());
        val issues = Arrays.asList(createJiraIssue("scheduled"));
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("canUpload"), equalTo(true));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_canNotUploadIseUserCanUploadAndIsNotScheduledOrInProgress() {
        doReturn(true).when(permissionservice).userCanUpload(anyString());
        val issues = Arrays.asList(createJiraIssue("closed"));
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("canUpload"), equalTo(false));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_canUploadIseUserCannotUploadAndIsScheduled() {
        doReturn(false).when(permissionservice).userCanUpload(anyString());
        val issues = Arrays.asList(createJiraIssue("scheduled"));
        doReturn(issues).when(jiraService).search(anyString());
        val model = documentUploadModel();

        assertThat(model.get("canUpload"), equalTo(false));
    }

    @Test
    @SneakyThrows
    public void documentUploadView_canUploadIfUserCanUpload() {
        doReturn(true).when(permissionservice).userCanUpload(anyString());
        val model = documentUploadModel();
        assertThat(model.get("userCanUpload"), equalTo(true));
    }

    @Test
    @SneakyThrows
    public void change_willChangeTheDocumentType() {
        controller.change("guid", "file", "META");
        verify(documentUploadService).changeFileType(eq("guid"), eq("file"), eq(DocumentUpload.Type.META));
    }

    @Test
    @SneakyThrows
    public void change_returnsDocumentUpload() {
        val actual = controller.change("guid", "file", "META");
        assertThat(actual, equalTo(documentUpload));
    }

    @Test
    @SneakyThrows
    public void acceptInvalid_willAcceptInvalidFile() {
        controller.acceptInvalid("guid", "file");
        verify(documentUploadService).acceptInvalid(eq("guid"), eq("file"));
    }

    @Test
    @SneakyThrows
    public void acceptInvalid_returnsDocumentUpload() {
        val actual = controller.acceptInvalid("guid", "file");
        assertThat(actual, equalTo(documentUpload));
    }
    
}
