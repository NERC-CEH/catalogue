package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MaintenanceResponse;
import uk.ac.ceh.gateway.catalogue.services.DataRepositoryOptimizingService;
import uk.ac.ceh.gateway.catalogue.services.TerraCatalogImporterService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.TerraCatalogImporter;

/**
 *
 * @author jcoop, cjohn
 */
public class MaintenanceControllerTest {
    @Mock(answer=RETURNS_DEEP_STUBS) DataRepositoryOptimizingService repoService;
    @Mock DocumentIndexingService indexService;
    @Mock DocumentLinkService linkingService;
    @Mock TerraCatalogImporterService terraCatalogImporterService;
    
    private MaintenanceController controller;
    
    @Before
    public void createMaintenanceController() {
        MockitoAnnotations.initMocks(this);
        controller = new MaintenanceController(repoService, indexService, linkingService, terraCatalogImporterService);
    }
    
    @Test
    public void checkThatReindexingDelegatesToIndexService() throws DocumentIndexingException {
        //Given
        //Nothing
        //When
        HttpEntity<MaintenanceResponse> reindexDocuments = controller.reindexDocuments();
        
        //Then
        verify(indexService).rebuildIndex();
    }
    
    @Test
    public void checkThatReindexingDelegatesToLinkingService() throws DocumentLinkingException {
        //Given
        //Nothing
        
        //When
        controller.reindexLinks();
        
        //Then
        verify(linkingService).rebuildLinks();
    }
    
    @Test
    public void chechThatCanOptimizeGitRepository() throws DataRepositoryException {
        //Given
        //Nothing
        
        //When
        controller.optimizeRepository();
        
        //Then
        verify(repoService).performOptimization();
    }
    
    @Test
    public void checkThatCanLoadMaintenancePageWhenThereRepoIsBroken() throws DataRepositoryException {
        //Given
        String errorMessage = "Something has gone wrong";
        when(repoService.getRepo().getLatestRevision()).thenThrow(new DataRepositoryException(errorMessage));
        
        //When
        MaintenanceResponse response = controller.loadMaintenancePage();
        
        //Then
        assertThat("Expected one message", response.getMessages().size(), equalTo(1));
        assertThat("Expected message to exist", response.getMessages(), contains(errorMessage));
    }

    @Test
    public void checkThatCanLoadMaintenancePageWhenIndexingIsBroken() throws DocumentIndexingException {
        //Given
        String errorMessage = "Something has gone wrong";
        when(indexService.isIndexEmpty()).thenThrow(new DocumentIndexingException(errorMessage));
        
        //When
        MaintenanceResponse response = controller.loadMaintenancePage();
        
        //Then
        assertThat("Expected one message", response.getMessages().size(), equalTo(1));
        assertThat("Expected message to exist", response.getMessages(), contains(errorMessage));
    }
    
    @Test
    public void checkThatUploadsZipAsGivenUser() throws IOException, UnknownContentTypeException {
        //Given
        CatalogueUser currentUser = mock(CatalogueUser.class);
        MultipartFile file = emptyZip();
        TerraCatalogImporter importer = mock(TerraCatalogImporter.class);
        when(terraCatalogImporterService.getImporter(currentUser)).thenReturn(importer);
        
        //When
        MaintenanceResponse response = controller.importTerraCatalogZip(currentUser, file).getBody();
        
        //Then
        verify(importer).importFile(any(ZipFile.class));
        assertThat("Expected one message", response.getMessages().size(), equalTo(1));
        assertThat("Expected message to exist", response.getMessages(), contains("Zip has been imported succcessfully"));
    }
    
    @Test
    public void checkThatErrorMessageIsReturnedWhenNoFileIsUploaded() throws DataRepositoryException {
        //Given
        CatalogueUser currentUser = mock(CatalogueUser.class);
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        
        //When
        MaintenanceResponse response = controller.importTerraCatalogZip(currentUser, file).getBody();
        
        //Then
        assertThat("Expected one message", response.getMessages().size(), equalTo(1));
        assertThat("Expected message to exist", response.getMessages(), contains("No file has been uploaded"));
    }
    
    @Test
    public void checkThatErrorMessageIsReturnedOnFailure() throws DataRepositoryException, IOException, UnknownContentTypeException {
        //Given
        CatalogueUser currentUser = mock(CatalogueUser.class);
        MultipartFile file = emptyZip();
        TerraCatalogImporter importer = mock(TerraCatalogImporter.class);
        when(terraCatalogImporterService.getImporter(currentUser)).thenReturn(importer);
        doThrow(new DataRepositoryException("Failed")).when(importer).importFile(any(ZipFile.class));
        
        //When
        MaintenanceResponse response = controller.importTerraCatalogZip(currentUser, file).getBody();
        
        //Then
        assertThat("Expected one message", response.getMessages().size(), equalTo(1));
        assertThat("Expected message to exist", response.getMessages(), contains("Failed"));
    }
    
    private MultipartFile emptyZip() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        doAnswer((Answer) (InvocationOnMock invocation) -> { //Create an empty zip file to import
            File arg = (File)invocation.getArguments()[0];
            try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(arg))) {
                output.closeEntry();
                output.flush();
            }
            return null;
        }).when(file).transferTo(any(File.class));
        return file;
    }
}
