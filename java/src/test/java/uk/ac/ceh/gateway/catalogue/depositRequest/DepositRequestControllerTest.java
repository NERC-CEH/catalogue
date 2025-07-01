package uk.ac.ceh.gateway.catalogue.depositRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockCatalogueUser
@ActiveProfiles("test")
@WebMvcTest(controllers = DepositRequestController.class)
@DisplayName("DepositRequestController")
class DepositRequestControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean
    DepositRequestService service;

    @Test
    @DisplayName("Returns deposit form page for an authenticated user")
    void getDepositFormPage() throws Exception {
        mvc.perform(get("/deposit-request"))
            .andExpect(status().isOk())
            .andExpect(view().name("html/deposit_request/deposit_form")); // enough
    }

    @Test
    @DisplayName("Handles a valid deposit submission")
    void postValidDepositRequest() throws Exception {

        DepositRequestModel body = new DepositRequestModel(
            "Alice Smith", "alice@example.com", "UKCEH", true,
            "NERC", "", "NE123", "Yes", "Model",
            true, true, false, false,
            List.of(new DataResourceModel(
                "Title","Description","Images","",true,"NetCDF","",
                "1000")),
            "Some notes");

        doNothing().when(service).handleSubmission(body);

        mvc.perform(post("/deposit-request")
                .with(csrf())
                .header("Remote-User", "alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/deposit-request/success"));
    }

    @Test
    @DisplayName("Rejects an invalid deposit submission")
    void postInvalidDepositRequest() throws Exception {

        DepositRequestModel bad = new DepositRequestModel(
            "", "", "", false,
            "", "", "", "", "",
            null, null, null, null,
            List.of(), "");

        mvc.perform(post("/deposit-request")
                .with(csrf())
                .header("Remote-User", "alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(bad)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid request"));
    }

    @Test
    @DisplayName("Renders the success page")
    void getSuccessPage() throws Exception {
        mvc.perform(get("/deposit-request/success")
                .header("Remote-User", "alice"))
            .andExpect(status().isOk())
            .andExpect(view().name("html/deposit_request/deposit_success"));
    }
}
