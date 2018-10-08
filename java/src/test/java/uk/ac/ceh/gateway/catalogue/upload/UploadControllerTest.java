package uk.ac.ceh.gateway.catalogue.upload;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.JiraService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;

@RunWith(MockitoJUnitRunner.class)
public class UploadControllerTest {
    @Mock
    private UploadDocumentService documentService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    JiraService jiraService;

    private UploadController uploadController;

    @Before
    public void setUp() {
        uploadController = new UploadController(documentService, permissionService, documentRepository, jiraService);
    }

    @Test
    @SneakyThrows
    public void deleteFile() {
        //given
        val user = CatalogueUser.PUBLIC_USER;
        val id = "test";
        val name = "name";
        val filename = "filename";
        val document = new UploadDocument();


        //when
        uploadController.deleteFile(user, id, name, filename, document);

        //then

    }
}