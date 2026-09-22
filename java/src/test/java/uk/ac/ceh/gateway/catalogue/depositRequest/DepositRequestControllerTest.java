package uk.ac.ceh.gateway.catalogue.depositRequest;

import lombok.val;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.net.URI;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})

@DisplayName("DepositRequestController")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class DepositRequestControllerTest extends AbstractMvcTest {
    @Autowired JsonMapper mapper;

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
            "Alice Smith", "alice@example.com", "UKCEH", "2025_Biodiversity and Land Use", true, true, true, true,
            "NERC", "", "NE123", "Yes", "Model",
            true, true, false, false,
            List.of(new DataResourceModel(
                "Title","Description","Images","",true,"NetCDF","",
                "1000", false)),
            "Some notes");

        ObjectNode mockResponse = JsonMapper.builder().build().createObjectNode();
        mockResponse.put("key", "TEST-123");
        mockResponse.put("componentName", "Deposit Request");

        when(service.handleSubmission(any(DepositRequestModel.class))).thenReturn(mockResponse);

        mvc.perform(post("/deposit-request")
                .with(csrf())
                .header("Remote-User", "alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(header().string(
                "Location", "/deposit-request/success?reference=TEST-123&component=Deposit%20Request"));
    }

    @Test
    @DisplayName("Rejects an invalid deposit submission")
    void postInvalidDepositRequest() throws Exception {

        DepositRequestModel bad = new DepositRequestModel(
            "", "", "", "", false, false, false, false,
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
    @DisplayName("Carries the JIRA reference number from submission through to the success page")
    void successPageShowsReferenceNumberFromSubmission() throws Exception {
        DepositRequestModel body = new DepositRequestModel(
            "Alice Smith", "alice@example.com", "UKCEH", "2025_Biodiversity and Land Use", true, true, true, true,
            "NERC", "", "NE123", "Yes", "Model",
            true, true, false, false,
            List.of(new DataResourceModel(
                "Title","Description","Images","",true,"NetCDF","",
                "1000", false)),
            "Some notes");

        ObjectNode mockResponse = JsonMapper.builder().build().createObjectNode();
        mockResponse.put("key", "TEST-123");
        mockResponse.put("componentName", "Ingestion Management");

        when(service.handleSubmission(any(DepositRequestModel.class))).thenReturn(mockResponse);

        val location = mvc.perform(post("/deposit-request")
                .with(csrf())
                .header("Remote-User", "alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getHeader("Location");

        // Following the redirect the browser is given, rather than asserting on a URL built here
        mvc.perform(get(URI.create(location))
                .header("Remote-User", "alice"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("referenceNumber", "TEST-123"))
            .andExpect(model().attribute("componentName", "Ingestion Management"));
    }

    @Test
    @DisplayName("Renders the success page")
    void getSuccessPage() throws Exception {
        mvc.perform(get("/deposit-request/success")
                .header("Remote-User", "alice"))
            .andExpect(status().isOk())
            .andExpect(view().name("html/deposit_request/deposit_success"));
    }

    @Test
    @DisplayName("Renders the success page without a reference when asked for one bare")
    void getSuccessPageWithNoParameters() throws Exception {
        mvc.perform(get("/deposit-request/success")
                .header("Remote-User", "alice"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("referenceNumber", ""))
            .andExpect(model().attribute("componentName", "Deposit Request"));
    }

    @ParameterizedTest
    @DisplayName("Ignores anything in the URL that is not a JIRA issue key")
    @ValueSource(strings = {
        "not-a-key",
        "<script>alert(1)</script>",
        "TEST-123 OR 1=1",
        "lower-123",
        "TEST-"
    })
    void getSuccessPageWithBogusReference(String reference) throws Exception {
        mvc.perform(get("/deposit-request/success")
                .queryParam("reference", reference)
                .header("Remote-User", "alice"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("referenceNumber", ""));
    }

    @Test
    @DisplayName("Falls back to the standard message for an unrecognised component")
    void getSuccessPageWithUnknownComponent() throws Exception {
        mvc.perform(get("/deposit-request/success")
                .queryParam("reference", "TEST-123")
                .queryParam("component", "Something Else")
                .header("Remote-User", "alice"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("referenceNumber", "TEST-123"))
            .andExpect(model().attribute("componentName", "Deposit Request"));
    }
}
