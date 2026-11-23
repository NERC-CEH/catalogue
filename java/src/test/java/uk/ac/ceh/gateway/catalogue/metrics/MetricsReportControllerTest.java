package uk.ac.ceh.gateway.catalogue.metrics;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static uk.ac.ceh.gateway.catalogue.controllers.DocumentController.MAINTENANCE_ROLE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockCatalogueUser(grantedAuthorities = MAINTENANCE_ROLE)
@ActiveProfiles({"metrics", "test", "server-eidc", "search-basic"})
@DisplayName("MetricsController")

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class MetricsReportControllerTest extends AbstractMvcTest {

    @MockitoBean private MetricsService metricsService;
    @MockitoBean private ProfileService profileService;
    @MockitoBean private CatalogueService catalogueService;
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
    @DisplayName("The To date includes that whole day")
    void endDateIncludesTheWholeDay() {
        // dri-one #461: the To date was midnight at its start, and the report keeps rows that end
        // by then, so a report "to 13 October" left out the 13th
        givenDefaultCatalogue();

        mvc.perform(get("/{catalogue}/metrics", "testCatalogue")
            .param("startDate", "2026-10-01")
            .param("endDate", "2026-10-13")
            .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk());

        verify(metricsService).getMetricsReport(
            eq(Instant.parse("2026-10-01T00:00:00Z")),
            eq(Instant.parse("2026-10-14T00:00:00Z")),
            any(), any(), any(), any(), anyInt());
    }

    @Test
    @SneakyThrows
    @DisplayName("Test getMetricsReport with no parameters")
    void getMetricsReportNoParams() {
        givenDefaultCatalogue();

        String catalogueId = "testCatalogue";
        List<Map<String, String>> mockReport = new ArrayList<>();

        given(metricsService.getMetricsReport(null, null, null, null, null, null, MetricsService.DEFAULT_REPORT_LIMIT)).willReturn(mockReport);

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
        mockReport.add(new HashMap<>() {{
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
        String docId = "abcd0001-0000-4000-8000-000000000001";
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

        given(metricsService.getMetricsReport(null, null, null, null, null, null, MetricsService.DEFAULT_REPORT_LIMIT)).willReturn(mockReport);

        mvc.perform(get("/{catalogue}/metrics", catalogueId)
            .with(csrf())
            .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML_VALUE));
    }

    /** The principal {@code SecurityConfig}'s anonymous filter installs for a request with no Remote-User. */
    private static AnonymousAuthenticationToken anonymous() {
        return new AnonymousAuthenticationToken(
            "NotSure", CatalogueUser.PUBLIC_USER, AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    }

    /** The regression guard — the report must not be reachable without logging in. */
    @Test
    @SneakyThrows
    @DisplayName("An anonymous request is forbidden and runs no query")
    void anonymousIsForbidden() {
        givenDefaultCatalogue();

        mvc.perform(get("/{catalogue}/metrics", "testCatalogue")
                .with(authentication(anonymous()))
                .accept(MediaType.TEXT_HTML))
            .andExpect(status().isForbidden());
        mvc.perform(get("/{catalogue}/metrics", "testCatalogue")
                .with(authentication(anonymous()))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verify(metricsService, never()).getMetricsReport(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    @DisplayName("A logged-in user without the maintenance role is forbidden and runs no query")
    void userWithoutRoleIsForbidden() {
        givenDefaultCatalogue();

        mvc.perform(get("/{catalogue}/metrics", "testCatalogue").accept(MediaType.TEXT_HTML))
            .andExpect(status().isForbidden());

        verify(metricsService, never()).getMetricsReport(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("noOfRecords is capped, whatever the caller asks for")
    void noOfRecordsIsCapped() {
        givenDefaultCatalogue();

        mvc.perform(get("/{catalogue}/metrics", "testCatalogue")
                .param("noOfRecords", String.valueOf(Integer.MAX_VALUE))
                .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk());

        verify(metricsService).getMetricsReport(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
            eq(MetricsService.MAX_REPORT_LIMIT));
    }

    @Test
    @SneakyThrows
    @DisplayName("noOfRecords defaults when absent or negative")
    void noOfRecordsDefaults() {
        givenDefaultCatalogue();

        mvc.perform(get("/{catalogue}/metrics", "testCatalogue")
                .param("noOfRecords", "-5")
                .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk());

        verify(metricsService).getMetricsReport(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
            eq(MetricsService.DEFAULT_REPORT_LIMIT));
    }

    @Test
    @SneakyThrows
    @DisplayName("docId is passed on as a whole, canonical document ID")
    void docIdIsCanonicalised() {
        givenDefaultCatalogue();

        mvc.perform(get("/{catalogue}/metrics", "testCatalogue")
                .param("docId", " ABCD0001-0000-4000-8000-000000000001 ")
                .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk());

        verify(metricsService).getMetricsReport(isNull(), isNull(), isNull(), isNull(), isNull(),
            eq("abcd0001-0000-4000-8000-000000000001"), eq(MetricsService.DEFAULT_REPORT_LIMIT));
    }

    @Test
    @SneakyThrows
    @DisplayName("A partial docId gives an empty report, not an error, and runs no query")
    void partialDocIdGivesEmptyReport() {
        givenDefaultCatalogue();

        mvc.perform(get("/{catalogue}/metrics", "testCatalogue")
                .param("docId", "abcd")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metricsReport").isArray())
            .andExpect(jsonPath("$.metricsReport").isEmpty());
        mvc.perform(get("/{catalogue}/metrics", "testCatalogue")
                .param("docId", "abcd")
                .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk());

        verify(metricsService, never()).getMetricsReport(any(), any(), any(), any(), any(), any(), any());
    }

    /**
     * The app's mapper leaves out empty lists, so a report that found nothing was sent without
     * metricsReport, and the page kept showing the previous rows.
     */
    @Test
    @SneakyThrows
    @DisplayName("An empty report is sent as an empty list, not left out")
    void emptyReportIsSentAsAnEmptyList() {
        givenDefaultCatalogue();
        given(metricsService.getMetricsReport(any(), any(), any(), any(), any(), any(), any())).willReturn(List.of());

        mvc.perform(get("/{catalogue}/metrics", "testCatalogue")
                .param("docId", "abcd0001-0000-4000-8000-000000000001")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metricsReport").isArray())
            .andExpect(jsonPath("$.metricsReport").isEmpty());
    }
}
