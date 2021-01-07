package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DatasetOffered;
import uk.ac.ceh.gateway.catalogue.model.DepositRequestDocument;
import uk.ac.ceh.gateway.catalogue.model.JiraIssueBuilder;
import uk.ac.ceh.gateway.catalogue.model.JiraIssueCreate;
import uk.ac.ceh.gateway.catalogue.model.DepositRequestDocument.Funded;
import uk.ac.ceh.gateway.catalogue.services.DepositRequestService;
import uk.ac.ceh.gateway.catalogue.services.JiraService;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import org.apache.jena.ext.com.google.common.collect.Lists;

import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(MockitoJUnitRunner.class)
public class DepositRequestControllerTest {

    @Mock
    private JiraService jiraService;

    @Mock
    private PermissionService permissionservice;

    @Mock
    private DepositRequestService depositRequestService;

    @InjectMocks
    private DepositRequestController controller;

    JiraIssueBuilder jiraIssueBuilder;

    @Before
    public void before() {
        val jiraIssueCreate = new JiraIssueCreate();
        jiraIssueCreate.setKey("key");
        doReturn(jiraIssueCreate).when(jiraService).create(any(JiraIssueBuilder.class));
    }

    private DepositRequestDocument createDepositRequestDocument () {
        DepositRequestDocument depositRequestDocument = new DepositRequestDocument();

        val datasetOffered = new DatasetOffered();
        datasetOffered.setName("name");
        depositRequestDocument.setDatasetsOffered(Lists.newArrayList(datasetOffered));

        depositRequestDocument.setDepositorEmail("email");
        depositRequestDocument.setDepositorName("username");
        depositRequestDocument.setDepositorOtherContact("depositorOtherContact");
        depositRequestDocument.setProjectName("projectName");
        depositRequestDocument.setPlanningDocsOther("planningDocsOther");
        depositRequestDocument.setNercFunded(Funded.no);
        depositRequestDocument.setPublicFunded(Funded.no);
        depositRequestDocument.setHasRelatedDatasets(true);
        depositRequestDocument.setRelatedDatasets(Lists.newArrayList("doi0", "doi1"));
        depositRequestDocument.setScienceDomainOther("scienceDomainOther");
        depositRequestDocument.setUniqueDeposit(true);
        depositRequestDocument.setModelOutput(true);
        depositRequestDocument.setPublishedPaper(true);
        depositRequestDocument.setReusable(true);

        depositRequestDocument.setTitle("title");
        depositRequestDocument.setId("id");

        return depositRequestDocument;
    }

    @Test
    public void documentUploadForm__createsAndCommentsOnANewIssue() {
        DepositRequestDocument depositRequestDocument = createDepositRequestDocument();
        val user = CatalogueUser.PUBLIC_USER;
        user.setEmail("email");
        controller.documentsUploadForm(CatalogueUser.PUBLIC_USER, depositRequestDocument);

        ArgumentCaptor<JiraIssueBuilder> argument = ArgumentCaptor.forClass(JiraIssueBuilder.class);
        verify(jiraService).create(argument.capture());

        val str = argument.getValue().build();
        assertThat(str, containsString("\"project\": {\"key\": \"EIDCHELP\"}"));
        assertThat(str, containsString("\"issuetype\": {\"name\": \"Job\"}"));
        assertThat(str, containsString("\"summary\": \"title\""));
        assertThat(str, containsString("\"customfield_11950\":\"username\""));
        assertThat(str, containsString("\"customfield_11951\":\"email\""));
        assertThat(str, containsString("\"labels\": [\"id\"]"));
        assertThat(str, containsString("\"components\": [{\"name\": \"Deposit Request\"}]"));
        assertThat(str, containsString("\"description\": \"" + depositRequestDocument.getJiraDescription()));

        verify(jiraService).comment("key", "this issue was created by email");
    }
}
