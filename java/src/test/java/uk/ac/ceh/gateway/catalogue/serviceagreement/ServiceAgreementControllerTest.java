package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"service-agreement", "test"})
@DisplayName("ServiceAgreementController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(ServiceAgreementController.class)
public class ServiceAgreementControllerTest {

    private static final String ID = "test";

    public static final String QUERY = "queryTest";

    @MockBean
    private ServiceAgreementSearch search;
    @MockBean
    private ServiceAgreementService serviceAgreementService;

    private @MockBean(name="permission")
    PermissionService permissionService;

    @Autowired
    private MockMvc mvc;

    @Test
    @SneakyThrows
    public void canGet() {
        //Given
        givenUserCanAccess();

        //When
        mvc.perform(get("/service-agreement")
                        .queryParam("id", QUERY))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @SneakyThrows
    public void getNoAccess() {

        //When
        mvc.perform(get("/service-agreement")
                        .queryParam("id", QUERY))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    public void canCreate() {
        //Given
        givenUserCanAccess();
        given(serviceAgreementService.metadataRecordExists(QUERY)).willReturn(true);

        //When
        mvc.perform(post("/service-agreement/" + QUERY)
                        .queryParam("catalogue", "elter"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    @SneakyThrows
    public void createNoAccess() {

        //When
        mvc.perform(post("/service-agreement/" + QUERY)
                .queryParam("catalogue", "elter")).andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    public void createDocumentDoesNotExist() {
        //Given
        givenUserCanAccess();
        given(serviceAgreementService.metadataRecordExists(QUERY)).willReturn(false);

        //When
        mvc.perform(post("/service-agreement/" + QUERY).contentType(MediaType.APPLICATION_JSON)
                        .queryParam("catalogue", "elter"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    @SneakyThrows
    public void canDelete() {
        //Given
        givenUserCanAccess();

        //When
        mvc.perform(delete("/service-agreement/" + QUERY))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    public void DeleteNoAccess() {

        //When
        mvc.perform(delete("/service-agreement/" + QUERY))
                .andExpect(status().isForbidden());
    }

    private void givenUserCanAccess() {
        given(this.permissionService.toAccess(any(), any(), any()))
                .willReturn(true);
    }

}