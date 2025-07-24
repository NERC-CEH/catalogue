package uk.ac.ceh.gateway.catalogue.depositRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("DepositRequestService")
class DepositRequestServiceTest {

    private RestTemplate restTemplate;
    private DepositRequestService service;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        service = new DepositRequestService(
            restTemplate,
            "username",
            "password",
            "https://mock-jira.local/rest/api/2",
            "EIDCHELP"
        );

        // Stub restTemplate to prevent actual HTTP call
        when(restTemplate.exchange(
            any(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(new ResponseEntity<>("{\"key\": \"EIDCHELP-12345\"}", HttpStatus.CREATED));
    }

    @Test
    @DisplayName("Submits request with resolved 'Other' fields")
    void testSubmissionWithOtherFields() {
        DepositRequestModel form = new DepositRequestModel(
            "Alice Smith", "alice@example.com", "UKCEH", true, true, true, true,
            "Other", "NERC", "NE123 NE456", "Yes", "Model",
            true, true, false, false,
            List.of(new DataResourceModel(
                "Dataset X", "Some description", "Satellite", "", true,
                "GeoTIFF", "", "100MB", false
            )),
            "Extra notes"
        );

        service.handleSubmission(form);

        verify(restTemplate, times(1)).exchange(
            any(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("Submits request with normal funder and single ref")
    void testSubmissionWithNormalFields() {
        DepositRequestModel form = new DepositRequestModel(
            "Bob Jones", "bob@example.com", "CEH", false, false, false, false,
            "NERC", "", "NE789", "No", "Omics",
            false, false, false, false,
            List.of(new DataResourceModel(
                "Omics Dataset", "DNA Sequences", "Text", "", true,
                "FASTA", "", "250MB", false
            )),
            ""
        );

        service.handleSubmission(form);

        verify(restTemplate, times(1)).exchange(
            any(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("Get Jira Component Name Ingestion Management")
    void testGetJiraComponentNameIngestionManagement() {
        DepositRequestModel form = new DepositRequestModel(
            "Bob Jones", "bob@example.com", "CEH", false, false, false, false,
            "NERC", "", "NE789", "Yes", "No",
            true, true, false, false,
            List.of(
                new DataResourceModel(
                    "Omics Dataset", "DNA Sequences", "Experimental data", "", null,
                    "Comma separated values (csv)", "", "250MB", false)
                ,new DataResourceModel(
                    "Omics Dataset", "DNA Sequences", "Model output", "", false,
                    "Excel spreadsheet", "", "250MB", false)
                ,new DataResourceModel(
                    "Omics Dataset", "DNA Sequences", "Monitoring data", "", null,
                    "NetCDF", "", "250MB", false)
                ,new DataResourceModel(
                    "Omics Dataset", "DNA Sequences", "Monitoring data", "", null,
                    "Shapefile", "", "250MB", false)
            ),
            ""
        );

        String componentName = service.getJiraComponentName(form);

        assertEquals("Ingestion Management", componentName, "Component name should be 'Ingestion Management'");
    }

    @Test
    @DisplayName("Get Jira Component Name Deposit Request")
    void testGetJiraComponentNameDepositRequest() {
        DepositRequestModel form = new DepositRequestModel(
            "Bob Jones", "bob@example.com", "CEH", false, false, false, false,
            "STFC", "", "NE789", "No", "Yes",
            false, false, true, false,
            List.of(
                new DataResourceModel(
                    "Omics Dataset", "DNA Sequences", "Interview/survey", "", null,
                    "Other", "txt", "2GB", true)
                ,new DataResourceModel(
                    "Omics Dataset", "DNA Sequences", "Images", "", null,
                    "Other", "txt", "2GB", true)
                ,new DataResourceModel(
                    "Omics Dataset", "DNA Sequences", "Other", "Test", true,
                    "Other", "txt", "2GB", true)
            ),
            ""
        );

        String componentName = service.getJiraComponentName(form);

        assertEquals("Deposit Request", componentName, "Component name should be 'Deposit Request'");
    }
}
