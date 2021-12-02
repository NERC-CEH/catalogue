package uk.ac.ceh.gateway.catalogue.serviceagreement;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.Link;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WithMockCatalogueUser
@ActiveProfiles({"service-agreement", "test"})
@DisplayName("ServiceAgreementController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers = ServiceAgreementController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class ServiceAgreementControllerTest {
    @MockBean private ServiceAgreementSearch search;
    @MockBean private ServiceAgreementService serviceAgreementService;
    @MockBean private ServiceAgreementModelAssembler assembler;
    @MockBean private CatalogueService catalogueService;
    private @MockBean(name="permission") PermissionService permissionService;

    private static ServiceAgreement serviceAgreement;
    private static final String ID = "test";
    private static final String VERSION = "version";

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    @BeforeAll
    static void init() {
        serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);
        serviceAgreement.setTitle("Test Service Agreement");
    }

    @BeforeEach
    @SneakyThrows
    void setup() {
        configuration.setSharedVariable("catalogues", catalogueService);
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(Catalogue.builder()
                .id("eidc")
                .title("Foo")
                .url("https://example.com")
                .contactUrl("")
                .build());
    }

    @Test
    @SneakyThrows
    void search() {
        //given
        givenUserIsAdmin();
        given(search.query("chess"))
            .willReturn(Collections.emptyList());

        //when
        mvc.perform(get("/service-agreement")
            .queryParam("query", "chess"))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getServiceAgreement() {
        // given
        givenUserCanView();
        givenServiceAgreement();
        givenServiceAgreementModel();

        val expectedResponse = """
            {
                "id": "test",
                "title": "Test Service Agreement",
                "_links": {
                    "self": {
                        "href": "https://catalogue/service-agreement/test"
                    }
                }
            }
            """;

        // when
        mvc.perform(get("/service-agreement/{id}", ID)
                .accept(HAL_JSON)
                .header("Forwarded", "proto=https;host=catalogue"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(HAL_JSON))
            .andExpect(content().json(expectedResponse));
    }

    @Test
    @SneakyThrows
    void noAccessToServiceAgreements() {
        // given
        givenUserCanNotView();
        givenDefaultCatalogue();

        // when
        mvc.perform(get("/service-agreement/{id}", ID))
            .andExpect(status().isForbidden());

        verifyNoInteractions(serviceAgreementService);
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void createServiceAgreement() {
        // given
        givenUserCanEdit();
        givenMetadataRecordExists();
        givenCreateServiceAgreement();
        givenServiceAgreementModel();

        val requestBody = """
            {
                "id": "123",
                "title": "Test Service Agreement",
                "_links": {
                    "self": {
                        "href": "https://catalogue/service-agreement/123"
                    }
                }
            }
            """;

        // when
        mvc.perform(post("/service-agreement/{id}", ID)
                .content(requestBody)
                .queryParam("catalogue", "eidc")
                .contentType(HAL_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(HAL_JSON))
            .andExpect(content().json("{\"id\":\"test\",\"title\":\"Test Service Agreement\"}"));

    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void userCannotCreateServiceAgreement() {
        // given
        givenUserCanNotEdit();
        givenDefaultCatalogue();

        // when
        mvc.perform(post("/service-agreement/{id}", ID)
                .content("{\"title\":\"Test Service Agreement\"}")
                .queryParam("catalogue", "eidc")
                .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        // then
        verifyNoInteractions(serviceAgreementService);
    }

    @Test
    @SneakyThrows
    void cannotCreateServiceAgreementAsMetadataDoesNotExist() {
        // given
        givenUserCanEdit();
        givenMedataRecordDoesNotExist();

        // when
        mvc.perform(post("/service-agreement/{id}", ID)
                .content("{\"title\":\"Test Service Agreement\"}")
                .queryParam("catalogue", "eidc")
                .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(APPLICATION_JSON));

        // then
        verify(serviceAgreementService).metadataRecordExists(ID);
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void updateServiceAgreement() {
        // given
        givenUserCanEdit();
        givenMetadataRecordExists();
        givenUpdateServiceAgreement();
        givenServiceAgreementModel();

        val requestBody = """
            {
                "id": "123",
                "title": "Test Service Agreement",
                "_links": {
                    "self": {
                        "href": "https://catalogue/service-agreement/123"
                    }
                }
            }
            """;

        // when
        mvc.perform(put("/service-agreement/{id}", ID)
                .content(requestBody)
                .contentType(HAL_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(HAL_JSON))
            .andExpect(content().json("{\"id\":\"test\",\"title\":\"Test Service Agreement\"}"));

    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void userCannotUpdateServiceAgreement() {
        // given
        givenUserCanNotEdit();
        givenDefaultCatalogue();

        // when
        mvc.perform(put("/service-agreement/{id}", ID)
                .content("{\"title\":\"Test Service Agreement\"}")
                .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        // then
        verifyNoInteractions(serviceAgreementService);
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void deleteServiceAgreement() {
        // given
        givenUserCanDelete();

        // when
        mvc.perform(delete("/service-agreement/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    void userCannotDeleteServiceAgreement() {
        //given
        givenUserCanNotDelete();
        givenDefaultCatalogue();


        //When
        mvc.perform(delete("/service-agreement/{id}",ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void userCannotPublishServiceAgreementAsRecordDoesNotExist() {
        //given
        givenUserCanEdit();
        givenMedataRecordDoesNotExist();

        //When
        mvc.perform(post("/service-agreement/{id}/publish", ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));

        //then
        verify(serviceAgreementService).metadataRecordExists(ID);
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void submitServiceAgreement() {
        //Given
        givenUserCanEdit();
        givenMetadataRecordExists();

        //When
        mvc.perform(post("/service-agreement/{id}/submit", ID))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/service-agreement/" + ID));
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void userCannotSubmitServiceAgreementAsUserCanNotEdit() {
        //given
        givenUserCanNotEdit();
        givenMetadataRecordExists();
        givenDefaultCatalogue();

        //When
        mvc.perform(post("/service-agreement/{id}/submit", ID))
                .andExpect(status().isForbidden());

        //then
        verifyNoInteractions(serviceAgreementService);
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void publishServiceAgreement() {
        //Given
        givenUserCanEdit();
        givenMetadataRecordExists();

        //When
        mvc.perform(post("/service-agreement/{id}/publish", ID))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/service-agreement/" + ID));
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void userCannotPublishServiceAgreementAsUserCanNotEdit() {
        //given
        givenUserCanNotEdit();
        givenMetadataRecordExists();
        givenDefaultCatalogue();

        //When
        mvc.perform(post("/service-agreement/{id}/publish", ID))
                .andExpect(status().isForbidden());

        //then
        verifyNoInteractions(serviceAgreementService);
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void giveDepositorEditPermission() {
        //Given
        givenUserCanEdit();
        givenMetadataRecordExists();

        //When
        mvc.perform(post("/service-agreement/{id}/add-editor", ID))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/service-agreement/" + ID));
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void userCannotGiveDepositorEditPermissionAsUserCanNotEdit() {
        //given
        givenUserCanNotEdit();
        givenMetadataRecordExists();
        givenDefaultCatalogue();

        //When
        mvc.perform(post("/service-agreement/{id}/add-editor", ID))
                .andExpect(status().isForbidden());

        //then
        verifyNoInteractions(serviceAgreementService);
    }


    @Test
    @SneakyThrows
    void getHistory() {
        // given
        givenUserCanEdit();
        givenMetadataRecordExists();
        givenHistory();

        val expectedResponse = """
            {
            "historyOf":"test",
            "revisions":[
                {
                "version":"1",
                "href":"test/service-agreement/test/version/revision1"
                }
            ]
            }""";

        // when
        mvc.perform(get("/service-agreement/{id}/history", ID)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(expectedResponse));
    }


    @Test
    @SneakyThrows
    void getPreviousServiceAgreement() {
        // given
        givenUserCanEdit();
        givenMetadataRecordExists();
        givenPreviousServiceAgreement();
        givenServiceAgreementModel();

        val expectedResponse = """
            {
                "id": "test",
                "title": "Test Service Agreement",
                "_links": {
                    "self": {
                        "href": "https://catalogue/service-agreement/test"
                    }
                }
            }
            """;

        // when
        mvc.perform(get("/service-agreement/{id}/version/{version}", ID, VERSION)
                        .accept(HAL_JSON)
                        .header("Forwarded", "proto=https;host=catalogue"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(HAL_JSON))
                .andExpect(content().json(expectedResponse));
    }

    private void givenServiceAgreementModel() {
        val self = Link.of("https://catalogue/service-agreement/test", "self");
        val model = new ServiceAgreementModel(serviceAgreement);
        model.add(self);
        log.info(model.toString());
        given(assembler.toModel(any(ServiceAgreement.class)))
            .willReturn(model);
    }

    @SneakyThrows
    private void givenCreateServiceAgreement() {
        given(serviceAgreementService.create(
            any(CatalogueUser.class),
            eq(ID),
            eq("eidc"),
            any(ServiceAgreement.class)
        ))
            .willReturn(serviceAgreement);
    }

    @SneakyThrows
    private void givenUpdateServiceAgreement() {
        given(serviceAgreementService.update(
            any(CatalogueUser.class),
            eq(ID),
            any(ServiceAgreement.class)
        ))
            .willReturn(serviceAgreement);
    }

    private void givenUserIsAdmin() {
        given(permissionService.userIsAdmin())
            .willReturn(true);
    }

    private void givenUserCanView() {
        given(permissionService.userCanView(ID))
                .willReturn(true);
    }

    private void givenUserCanNotView() {
        given(permissionService.userCanView(ID))
            .willReturn(false);
    }

    private void givenUserCanEdit() {
        given(permissionService.userCanEdit(ID))
            .willReturn(true);
    }

    private void givenUserCanNotEdit() {
        given(permissionService.userCanEdit(ID))
            .willReturn(false);
    }

    private void givenUserCanDelete() {
        given(permissionService.userCanDelete(ID))
            .willReturn(true);
    }

    private void givenUserCanNotDelete() {
        given(permissionService.userCanDelete(ID))
            .willReturn(false);
    }

    private void givenServiceAgreement() {
        given(serviceAgreementService.get(ID))
            .willReturn(serviceAgreement);
    }

    private void givenPreviousServiceAgreement() {
        given(serviceAgreementService.getPreviousVersion(ID, VERSION))
                .willReturn(serviceAgreement);
    }

    private void givenMetadataRecordExists() {
        given(serviceAgreementService.metadataRecordExists(ID))
            .willReturn(true);
    }

    private void givenMedataRecordDoesNotExist() {
        given(serviceAgreementService.metadataRecordExists(ID))
            .willReturn(false);
    }
    private void givenHistory() {
        List<DataRevision<CatalogueUser>> revisions = List.of(
            new TestRevision("currentRevision"),
            new TestRevision("revision1")
        );
        History history = new History("test", ID, revisions);
        given(serviceAgreementService.getHistory(ID))
                .willReturn(history);
    }

    @Value
    public static class TestRevision implements DataRevision<CatalogueUser> {
        String revision;

        @Override
        public String getRevisionID() {
            return revision;
        }

        @Override
        public String getMessage() {
            return null;
        }

        @Override
        public String getShortMessage() {
            return null;
        }

        @Override
        public CatalogueUser getAuthor() {
            return null;
        }
    }
}
