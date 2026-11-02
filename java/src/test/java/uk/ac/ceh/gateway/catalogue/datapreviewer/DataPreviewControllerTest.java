package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;
import uk.ac.ceh.gateway.catalogue.datapreviewer.DocumentPreview.Kind;

@ActiveProfiles({"test", "server-eidc", "search-basic", "upload-hubbub"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = "catalogue.data-preview.enabled=true")
class DataPreviewControllerTest extends AbstractMvcTest {

    private static final String BASE = "/documents/abc/data-preview";

    @MockitoBean
    private DataPreviewService service;

    private static CsvSample sample() {
        return new CsvSample(List.of("a", "b"), List.of(List.of("1", "2")), false, true);
    }

    @Test
    void manifestReturnsJson() throws Exception {
        var file = new PreviewManifest.PreviewFile("data/x.csv", "x.csv", 12L, "abcd");
        when(service.manifest("abc"))
            .thenReturn(new PreviewManifest(List.of(file), 1, false, 3, List.of(), 0));

        mvc.perform(get(BASE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dataFiles[0].path", is("data/x.csv")))
            .andExpect(jsonPath("$.dataFiles[0].sha256", is("abcd")))
            .andExpect(jsonPath("$.dataCount", is(1)))
            .andExpect(jsonPath("$.dataCountIsMinimum", is(false)))
            .andExpect(jsonPath("$.totalFiles", is(3)));
    }

    @Test
    void dataReturnsTable() throws Exception {
        when(service.data("abc", "data/x.csv")).thenReturn(Optional.of(DocumentPreview.table(sample())));

        mvc.perform(get(BASE + "/data").param("path", "data/x.csv"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kind", is("TABLE")))
            .andExpect(jsonPath("$.table.header[1]", is("b")))
            .andExpect(jsonPath("$.table.rows[0][0]", is("1")))
            .andExpect(jsonPath("$.table.moreRows", is(false)))
            .andExpect(jsonPath("$.table.previewable", is(true)))
            .andExpect(jsonPath("$.table.sheet").doesNotExist());
    }

    @Test
    void dataReturnsWorkbookSheetNames() throws Exception {
        CsvSample sheet = sample().inSheet("Sites", List.of("Notes", "Sites"));
        when(service.data("abc", "data/x.xlsx")).thenReturn(Optional.of(DocumentPreview.table(sheet)));

        mvc.perform(get(BASE + "/data").param("path", "data/x.xlsx"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.table.sheet", is("Sites")))
            .andExpect(jsonPath("$.table.sheets[0]", is("Notes")));
    }

    @Test
    void dataReturnsNetcdfSummary() throws Exception {
        var summary = new NetcdfSummary("NetCDF-4 (HDF5)",
            List.of(new NetcdfSummary.Attribute("title", "Rain")), false,
            List.of(new NetcdfSummary.Dimension("time", 12, true)),
            List.of(new NetcdfSummary.Variable("rain", "Rainfall", null, "mm", "time", "float", false)), false,
            new NetcdfSummary.TimeCoverage("time", "2000-01-01T00:00:00Z", "2000-12-01T00:00:00Z", "standard"),
            List.of(new NetcdfSummary.Extent("lat", "latitude", "degrees_north", 50, 52)));
        when(service.data("abc", "met/x.nc")).thenReturn(Optional.of(DocumentPreview.summary(summary)));

        mvc.perform(get(BASE + "/data").param("path", "met/x.nc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kind", is("SUMMARY")))
            .andExpect(jsonPath("$.summary.format", is("NetCDF-4 (HDF5)")))
            .andExpect(jsonPath("$.summary.dimensions[0].unlimited", is(true)))
            .andExpect(jsonPath("$.summary.variables[0].units", is("mm")))
            .andExpect(jsonPath("$.summary.time.start", is("2000-01-01T00:00:00Z")))
            .andExpect(jsonPath("$.summary.extents[0].max", is(52.0)));
    }

    @Test
    void manifestReportsNetcdfCount() throws Exception {
        var file = new PreviewManifest.PreviewFile("met/2000.nc", "2000.nc", 12L, "abcd");
        when(service.manifest("abc")).thenReturn(new PreviewManifest(List.of(file), 1, false, 20, List.of(), 20));

        mvc.perform(get(BASE)).andExpect(jsonPath("$.netcdfCount", is(20)));
    }

    @Test
    void documentReturnsPreview() throws Exception {
        when(service.document("abc", "docs/a.pdf"))
            .thenReturn(Optional.of(new DocumentPreview(Kind.TEXT, "hello", null, false)));

        mvc.perform(get(BASE + "/document").param("path", "docs/a.pdf"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kind", is("TEXT")))
            .andExpect(jsonPath("$.text", is("hello")))
            .andExpect(jsonPath("$.truncated", is(false)));
    }

    @Test
    void unlistedPathIs404() throws Exception {
        when(service.data(anyString(), anyString())).thenReturn(Optional.empty());
        when(service.document(anyString(), anyString())).thenReturn(Optional.empty());

        for (var path : List.of("nope.csv", "../../etc/passwd")) {
            mvc.perform(get(BASE + "/data").param("path", path)).andExpect(status().isNotFound());
            mvc.perform(get(BASE + "/document").param("path", path)).andExpect(status().isNotFound());
        }
    }

    @Test
    void missingPathIs400() throws Exception {
        mvc.perform(get(BASE + "/data")).andExpect(status().isBadRequest());
        mvc.perform(get(BASE + "/document")).andExpect(status().isBadRequest());
    }

    @Test
    void closedRecordIs403OnAllThree() throws Exception {
        var closed = new PreviewNotOpenException("abc");
        when(service.manifest("abc")).thenThrow(closed);
        when(service.data(anyString(), anyString())).thenThrow(closed);
        when(service.document(anyString(), anyString())).thenThrow(closed);

        mvc.perform(get(BASE)).andExpect(status().isForbidden());
        mvc.perform(get(BASE + "/data").param("path", "x.csv")).andExpect(status().isForbidden());
        mvc.perform(get(BASE + "/document").param("path", "x.pdf")).andExpect(status().isForbidden());
    }

    @Test
    void unknownRecordIs404() throws Exception {
        when(service.manifest("abc")).thenThrow(new NoSuchElementException("abc"));

        mvc.perform(get(BASE))
            .andExpect(status().isNotFound())
            .andExpect(content().string(not(containsString("abc\"}"))));
    }

    @Test
    void busyIs503() throws Exception {
        when(service.document("abc", "a.pdf")).thenThrow(new PreviewBusyException());

        mvc.perform(get(BASE + "/document").param("path", "a.pdf"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.error", is("Preview unavailable")));
    }

    @Test
    void unexpectedFailureHidesCause() throws Exception {
        when(service.manifest("abc"))
            .thenThrow(new RuntimeException("https://hubbub.ceh.ac.uk/secret"));
        when(service.data("abc", "x.csv"))
            .thenThrow(new IllegalStateException("repo", new RuntimeException("hubbub")));

        mvc.perform(get(BASE))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error", is("Preview unavailable")))
            .andExpect(content().string(not(containsString("hubbub"))));
        mvc.perform(get(BASE + "/data").param("path", "x.csv"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(not(containsString("hubbub"))));
    }

    @Test
    void hubbubTimeoutIsGenericFailure() throws Exception {
        when(service.manifest("abc")).thenThrow(new ResourceAccessException(
            "I/O error on GET request for \"https://hubbub.ceh.ac.uk/v7/abc/eidchub\": Read timed out",
            new SocketTimeoutException("Read timed out")));

        mvc.perform(get(BASE))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error", is("Preview unavailable")))
            .andExpect(content().string(not(containsString("hubbub"))));
    }
}
