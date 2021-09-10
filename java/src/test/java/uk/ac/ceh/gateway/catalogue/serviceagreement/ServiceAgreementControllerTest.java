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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"service-agreement", "test"})
@DisplayName("ServiceAgreementController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
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
        //Given
        givenUserCanView();
        givenServiceAgreement();

        //When
        mvc.perform(get("/service-agreement/{id}", ID))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json("{\"id\":\"test\"}"));
    }

    @Test
    @SneakyThrows
    void noAccessToServiceAgreements() {
        //given
        givenUserCanNotView();

        //When
        mvc.perform(get("/service-agreement/{id}", ID))
            .andExpect(status().isForbidden());

        verifyNoInteractions(serviceAgreementService);
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void createServiceAgreement() {
        //Given
        givenUserCanEdit();
        givenMetadataRecordExists();
        val expected = new ServiceAgreement();
        expected.setId(ID);
        expected.setTitle("Test Service Agreement");

        //When
        mvc.perform(post("/service-agreement/{id}", ID)
                .content("{\"title\":\"Test Service Agreement\"}")
                .queryParam("catalogue", "eidc")
                .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
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
        //given
        givenUserCanNotEdit();

        //When
        mvc.perform(post("/service-agreement/{id}", ID)
                .content("{\"title\":\"Test Service Agreement\"}")
                .queryParam("catalogue", "eidc")
                .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

        //then
        verifyNoInteractions(serviceAgreementService);
    }

    @Test
    @SneakyThrows
    void cannotCreateServiceAgreementAsMetadataDoesNotExist() {
        //Given
        givenUserCanEdit();
        givenMedataRecordDoesNotExist();

        //When
        mvc.perform(post("/service-agreement/{id}", ID)
                .content("{\"title\":\"Test Service Agreement\"}")
                .queryParam("catalogue", "eidc")
                .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(APPLICATION_JSON));

        //Then
        verify(serviceAgreementService).metadataRecordExists(ID);
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    void deleteServiceAgreement() {
        //Given
        givenUserCanDelete();

        //When
        mvc.perform(delete("/service-agreement/{id}", ID))
                .andExpect(status().isNoContent());

        //then
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