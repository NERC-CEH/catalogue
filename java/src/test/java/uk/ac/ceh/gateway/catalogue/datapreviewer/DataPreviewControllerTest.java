package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
            .thenReturn(new PreviewManifest(List.of(file), 1, false, 3, List.of()));

        mvc.perform(get(BASE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.csvFiles[0].path", is("data/x.csv")))
            .andExpect(jsonPath("$.csvFiles[0].sha256", is("abcd")))
            .andExpect(jsonPath("$.csvCount", is(1)))
            .andExpect(jsonPath("$.csvCountIsMinimum", is(false)))
            .andExpect(jsonPath("$.totalFiles", is(3)));
    }

    @Test
    void csvReturnsSample() throws Exception {
        when(service.csv("abc", "data/x.csv")).thenReturn(Optional.of(sample()));

        mvc.perform(get(BASE + "/csv").param("path", "data/x.csv"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.header[1]", is("b")))
            .andExpect(jsonPath("$.rows[0][0]", is("1")))
            .andExpect(jsonPath("$.moreRows", is(false)))
            .andExpect(jsonPath("$.previewable", is(true)));
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
        when(service.csv(anyString(), anyString())).thenReturn(Optional.empty());
        when(service.document(anyString(), anyString())).thenReturn(Optional.empty());

        for (var path : List.of("nope.csv", "../../etc/passwd")) {
            mvc.perform(get(BASE + "/csv").param("path", path)).andExpect(status().isNotFound());
            mvc.perform(get(BASE + "/document").param("path", path)).andExpect(status().isNotFound());
        }
    }

    @Test
    void missingPathIs400() throws Exception {
        mvc.perform(get(BASE + "/csv")).andExpect(status().isBadRequest());
        mvc.perform(get(BASE + "/document")).andExpect(status().isBadRequest());
    }

    @Test
    void closedRecordIs403OnAllThree() throws Exception {
        var closed = new PreviewNotOpenException("abc");
        when(service.manifest("abc")).thenThrow(closed);
        when(service.csv(anyString(), anyString())).thenThrow(closed);
        when(service.document(anyString(), anyString())).thenThrow(closed);

        mvc.perform(get(BASE)).andExpect(status().isForbidden());
        mvc.perform(get(BASE + "/csv").param("path", "x.csv")).andExpect(status().isForbidden());
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
        when(service.csv("abc", "x.csv"))
            .thenThrow(new IllegalStateException("repo", new RuntimeException("hubbub")));

        mvc.perform(get(BASE))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error", is("Preview unavailable")))
            .andExpect(content().string(not(containsString("hubbub"))));
        mvc.perform(get(BASE + "/csv").param("path", "x.csv"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(not(containsString("hubbub"))));
    }
}
