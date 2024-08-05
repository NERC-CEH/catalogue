package uk.ac.ceh.gateway.catalogue.metrics;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockCatalogueUser
@ActiveProfiles({"metrics", "test"})
@DisplayName("MetricsController")
@WebMvcTest(
    controllers = MetricsReportController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class MetricsReportControllerTest {

    @MockBean private MetricsService metricsService;
    @MockBean private ProfileService profileService;
    @MockBean private CatalogueService catalogueService;

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    @BeforeEach
    @SneakyThrows
    void setup() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(Catalogue.builder()
                .id("eidc")
                .title("Foo")
                .url("https://example.com")
                .logo("eidc.png")
                .contactUrl("")
                .build());
    }

    @Test
    @SneakyThrows
    @DisplayName("Ensure model is rendered correctly")
    void showMetricsReportPage() {
        givenDefaultCatalogue();
        String testCatalogue = "testCatalogue";

        mvc.perform(get("/{catalogue}/metrics", testCatalogue))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE));
    }

    @Test
    @SneakyThrows
    @DisplayName("Test getMetricsReport with no parameters")
    void getMetricsReportNoParams() {
        givenDefaultCatalogue();

        String catalogueId = "testCatalogue";
        List<Map<String, String>> mockReport = new ArrayList<Map<String, String>>();

        given(metricsService.getMetricsReport(null, null, null, null, null, null, null)).willReturn(mockReport);;

        mvc.perform(get("/{catalogue}/metrics", catalogueId)
            .with(csrf())
            .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE));
    }

    @Test
    @SneakyThrows
    @DisplayName("Test getMetricsReport with parameters")
    void getMetricsReportWithParams() {
        givenDefaultCatalogue();

        String catalogueId = "testCatalogue";
        List<Map<String, String>> mockReport = new ArrayList<>();
        mockReport.add(new HashMap<String, String>() {{
            put("document", "document");
            put("doc_title", "doc_title");
            put("record_type", "record_type");
            put("views", "1");
            put("downloads", "100");
        }});

        Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant endDate = Instant.now();
        String orderBy = "views";
        String ordering = "desc";
        List<String> recordType = List.of("dataset");
        String docId = "123";
        Integer noOfRecords = 10;

        given(metricsService.getMetricsReport(startDate, endDate, orderBy, ordering, recordType, docId, noOfRecords)).willReturn(mockReport);

        mvc.perform(get("/{catalogue}/metrics", catalogueId)
            .param("startDate", "2024-01-01")
            .param("endDate", "2024-12-31")
            .param("orderBy", orderBy)
            .param("ordering", ordering)
            .param("recordType", "dataset")
            .param("docId", docId)
            .param("noOfRecords", String.valueOf(noOfRecords))
            .with(csrf())
            .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE));
    }


    @Test
    @SneakyThrows
    @DisplayName("Test getMetricsReport with missing parameters")
    void getMetricsReportWithMissingParams() {
        givenDefaultCatalogue();

        String catalogueId = "testCatalogue";
        List<Map<String, String>> mockReport = new ArrayList<>();

        given(metricsService.getMetricsReport(null, null, null, null, null, null, null)).willReturn(mockReport);

        mvc.perform(get("/{catalogue}/metrics", catalogueId)
            .with(csrf())
            .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE));
    }
}
