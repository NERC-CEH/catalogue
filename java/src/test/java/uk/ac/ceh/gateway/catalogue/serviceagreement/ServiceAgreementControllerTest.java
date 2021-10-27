package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"service-agreement", "test"})
@DisplayName("ServiceAgreementController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class, ServiceAgreementModelAssembler.class})
@WebMvcTest(ServiceAgreementController.class)
class ServiceAgreementControllerTest {

    private static final String ID = "test";
    private static final String QUERY = "queryTest";
    private static CatalogueUser USER;

    @MockBean
    private ServiceAgreementSearch search;
    @MockBean
    private ServiceAgreementService serviceAgreementService;

    private @MockBean(name="permission")
    PermissionService permissionService;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    private static void setup() {
        USER = new CatalogueUser();
        USER.setUsername("test");
        USER.setEmail("test@example.com");
    }

    @Test
    @SneakyThrows
    void getServiceAgreement() {
        // given
        givenUserCanView();
        givenServiceAgreement();
        val expectedResponse = """
            {
                "id": "test",
                "title": "Test service agreement",
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
        val expected = new ServiceAgreement();
        expected.setId(ID);
        expected.setTitle("Test Service Agreement");
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
                .queryParam("catalogue", "eidc")
                .contentType(HAL_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(HAL_JSON))
            .andExpect(content().json("{\"id\":\"test\",\"title\":\"Test Service Agreement\"}"));

        //then
        verify(serviceAgreementService).save(
            USER,
            ID,
            "eidc",
            expected
        );
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void userCannotCreateServiceAgreement() {
        // given
        givenUserCanNotEdit();

        // when
        mvc.perform(put("/service-agreement/{id}", ID)
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
        mvc.perform(put("/service-agreement/{id}", ID)
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
    void deleteServiceAgreement() {
        // given
        givenUserCanDelete();

        // when
        mvc.perform(delete("/service-agreement/{id}", ID))
                .andExpect(status().isNoContent());

        // then
        verify(serviceAgreementService).delete(USER, ID);
    }

    @Test
    @SneakyThrows
    void userCannotDeleteServiceAgreement() {
        //given
        givenUserCanNotDelete();

        //When
        mvc.perform(delete("/service-agreement/{id}",ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void populateGeminiDocument() {
        //Given
        givenUserCanEdit();
        givenMetadataRecordExists();
        val expected = new ServiceAgreement();
        expected.setId(ID);
        expected.setTitle("Test Service Agreement");

        //When
        mvc.perform(post("/service-agreement/{id}/populate", ID)
                        .queryParam("catalogue", "eidc")
                )
                .andExpect(status().isFound());
        //then
        verify(serviceAgreementService).populateGeminiDocument(
                USER,
                ID,
                "eidc");
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void userCannotPopulateGeminiDocumentAsUserCanNotEdit() {
        //given
        givenUserCanNotEdit();
        givenMetadataRecordExists();

        //When
        mvc.perform(post("/service-agreement/{id}/populate", ID)
                        .queryParam("catalogue", "eidc")
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        //then
        verifyNoInteractions(serviceAgreementService);
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void userCannotPopulateGeminiDocumentAsRecordDoesNotExist() {
        //given
        givenUserCanEdit();
        givenMedataRecordDoesNotExist();

        //When
        mvc.perform(post("/service-agreement/{id}/populate", ID)
                        .queryParam("catalogue", "eidc")
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));

        //then
        verify(serviceAgreementService).metadataRecordExists(ID);
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
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);
        serviceAgreement.setTitle("Test service agreement");
        given(serviceAgreementService.get(ID))
            .willReturn(serviceAgreement);
    }

    private void givenMetadataRecordExists() {
        given(serviceAgreementService.metadataRecordExists(ID))
            .willReturn(true);
    }

    private void givenMedataRecordDoesNotExist() {
        given(serviceAgreementService.metadataRecordExists(QUERY))
            .willReturn(false);
    }
}