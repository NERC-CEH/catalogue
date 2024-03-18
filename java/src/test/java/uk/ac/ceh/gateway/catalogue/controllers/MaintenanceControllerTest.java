package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.jena.JenaIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.validation.ValidationIndexingService;
import uk.ac.ceh.gateway.catalogue.model.MaintenanceResponse;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.services.DataRepositoryOptimizingService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.ADMIN;
import static uk.ac.ceh.gateway.catalogue.controllers.DocumentController.MAINTENANCE_ROLE;

@WithMockCatalogueUser(
    username=ADMIN,
    grantedAuthorities=MAINTENANCE_ROLE
)
@ActiveProfiles("test")
@DisplayName("MaintenanceController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=MaintenanceController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
public class MaintenanceControllerTest {
    @MockBean DataRepositoryOptimizingService repoService;
    @MockBean @Qualifier("solr-index") SolrIndexingService indexService;
    @MockBean @Qualifier("jena-index") JenaIndexingService linkingService;
    @MockBean @Qualifier("validation-index") ValidationIndexingService validationService;
    @MockBean @Qualifier("mapserver-index") MapServerIndexingService mapserverService;
    @MockBean CatalogueService catalogueService;
    @MockBean ProfileService profileService;

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    private MaintenanceController controller;
    private final String catalogueKey = "eidc";

    @BeforeEach
    public void createMaintenanceController() {
        controller = new MaintenanceController(repoService, indexService, linkingService, validationService, mapserverService);
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(
                Catalogue.builder()
                    .id(catalogueKey)
                    .title("Env Data Centre")
                    .url("https://example.com")
                    .contactUrl("")
                    .logo("eidc.png")
                    .build()
            );
    }

    @Test
    @SneakyThrows
    void getMaintenancePage() {
        //given
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();

        //when
        mvc.perform(
            get("/maintenance")
            .header("remote-user", ADMIN)
            .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML));
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
    public void checkThatRecreatingMapfilesDelegatesToMapServerService() throws DocumentIndexingException {
        //Given
        //Nothing
        //When
        HttpEntity<MaintenanceResponse> reindexDocuments = controller.recreateMapFiles();

        //Then
        verify(mapserverService).rebuildIndex();
    }

    @Test
    public void checkThatReindexingDelegatesToLinkingService() throws DocumentIndexingException {
        //Given
        //Nothing

        //When
        controller.reindexLinks();

        //Then
        verify(linkingService).rebuildIndex();
    }

    @Test
    public void checkThatCanOptimizeGitRepository() throws DataRepositoryException {
        //Given
        //Nothing

        //When
        controller.optimizeRepository();

        //Then
        verify(repoService).performOptimization();
    }

    @Test
    public void checkThatReindexingDelegatesToValidationService() throws DocumentIndexingException {
        //Given
        //Nothing

        //When
        controller.validateRepository();

        //Then
        verify(validationService).rebuildIndex();
    }

    @Test
    public void checkThatCanLoadMaintenancePageWhenThereRepoIsBroken() throws DataRepositoryException {
        //Given
        String errorMessage = "Something has gone wrong";
        when(repoService.getLatestRevision()).thenThrow(new DataRepositoryException(errorMessage));

        //When
        MaintenanceResponse response = controller.loadMaintenancePage();

        //Then
        assertThat("Expected one message", response.getMessages().size(), equalTo(1));
        assertThat("Expected message to exist", response.getMessages().contains(errorMessage));
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
        assertThat("Expected message to exist", response.getMessages().contains(errorMessage));
    }
}
