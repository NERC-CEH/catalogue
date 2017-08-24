package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mockito.runners.MockitoJUnitRunner;

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.FileChecksum;
import uk.ac.ceh.gateway.catalogue.model.JiraIssue;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.FileUploadService;
import uk.ac.ceh.gateway.catalogue.services.JiraService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@RunWith(MockitoJUnitRunner.class)
public class UploadControllerTest {

    @Mock
    private FileUploadService fileUploadService;

    @Mock
    private JiraService jiraService;

    @Mock
    private PermissionService permissionservice;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private InputStream inputStream;

    @Mock
    private CatalogueUser user;

    @Mock
    private MetadataDocument document;

    private MetadataInfo info;

    @InjectMocks
    private UploadController controller;

    private List<FileChecksum> checksums;
    private JiraIssue issue;

    @Before
    @SneakyThrows
    public void before() {
        FileChecksum fileChecksum = FileChecksum.fromLine("d41d8cd98f00b204e9800998ecf8427e *document-1.txt");
        checksums = new ArrayList<FileChecksum>();
        checksums.add(fileChecksum);

        doReturn(checksums).when(fileUploadService).getChecksums(anyString());

        doReturn(inputStream).when(multipartFile).getInputStream();
        doReturn("filename").when(multipartFile).getOriginalFilename();

        doReturn("email").when(user).getEmail();
        doReturn("username").when(user).getUsername();

        val issues = new ArrayList<JiraIssue>();
        issue = new JiraIssue();
        issue.setKey("key");
        issues.add(issue);
        doReturn(issues).when(jiraService).search(anyString());

        doReturn(document).when(documentRepository).read(anyString());

        Multimap<Permission, String> permissions = ArrayListMultimap.create();
        permissions.put(Permission.UPLOAD, "username");

        info = MetadataInfo.builder().permissions(permissions).build();

        doReturn(info).when(document).getMetadata();
    }

    @SneakyThrows
    private List<FileChecksum> deleteAFile() {
        return controller.deleteFile("guid", "filename");
    }

    @Test
    @SneakyThrows
    public void deletingAFile_returnsTheChecksums() {
        val actual = deleteAFile();

        assertThat(actual, equalTo(checksums));
    }

    @Test
    @SneakyThrows
    public void deletingAFile_deletesTheFileWithTheFileUploadService() {
        deleteAFile();

        verify(fileUploadService).deleteFile(eq("guid"), eq("filename"));
    }

    @SneakyThrows
    private List<FileChecksum> addAFile() {
        return controller.addFile("guid", multipartFile);
    }

    @Test
    @SneakyThrows
    public void addingAFile_uploadsDataWithTheFileUploadService() {
        addAFile();

        verify(fileUploadService).uploadData(eq(inputStream), eq("guid"), eq("filename"));
    }

    @Test
    @SneakyThrows
    public void addingAFile_returnsTheChecksums() {
        val actual = addAFile();

        assertThat(actual, equalTo(checksums));
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
        val noIssues = new ArrayList<JiraIssue>();
        doReturn(noIssues).when(jiraService).search(anyString());

        finishAnUpload();
    }

    @Test(expected = UploadController.NonUniqueJiraIssue.class)
    @SneakyThrows
    public void finishAnUpload_throwsUniqueJiraIssueWhenMoreThanOneIssueFound() {
        val noIssues = new ArrayList<JiraIssue>();
        noIssues.add(issue);
        noIssues.add(issue);
        doReturn(noIssues).when(jiraService).search(anyString());

        finishAnUpload();
    }

}
