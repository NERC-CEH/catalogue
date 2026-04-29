package uk.ac.ceh.gateway.catalogue.datapreviewer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles({"test", "server-eidc", "search-basic", "upload-hubbub"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class DataPreviewerControllerTest extends AbstractMvcTest {

    @MockitoBean
    private DataPreviewerService dataPreviewerService;

    @Test
    void preview_dataset_returns200AndBody() throws Exception {
        DatasetPreviewResponse response =
            new DatasetPreviewResponse(
                "dataset",
                "dataset-id",
                "Test Dataset",
                List.of(TimePeriod.builder()
                    .begin("2020-01-01")
                    .end("2020-12-31")
                    .build()),
                Map.of("TEMP", "Temperature"),
                List.of(
                    new PreviewDatasetFile(
                        "file.csv",
                        "/mock/file.csv",
                        123L,
                        "text/csv"
                    )
                )
            );

        when(dataPreviewerService.preview("dataset-id"))
            .thenReturn(response);

        mvc.perform(
                get("/documents/dataset-id/preview")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("dataset"))
            .andExpect(jsonPath("$.id").value("dataset-id"))
            .andExpect(jsonPath("$.files").isArray());
    }

    @Test
    void preview_notFound_returns404() throws Exception {
        when(dataPreviewerService.preview("missing-id"))
            .thenThrow(new IllegalArgumentException("Document not found"));

        mvc.perform(
                get("/documents/missing-id/preview")
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void preview_forbidden_returns403() throws Exception {
        when(dataPreviewerService.preview("private-id"))
            .thenThrow(new SecurityException("Dataset not publicly accessible"));

        mvc.perform(
                get("/documents/private-id/preview")
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void preview_error_returns500() throws Exception {
        when(dataPreviewerService.preview("boom"))
            .thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/documents/boom/preview"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Failed to generate preview"));
    }
}
