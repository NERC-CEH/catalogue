package uk.ac.ceh.gateway.catalogue.controllers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DatasetFilesController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("upload:hubbub")
class DatasetFilesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UploadService uploadService;

    @MockitoBean
    private DocumentRepository documentRepository;

    private GeminiDocument document;
    private MetadataInfo metadata;

    @BeforeEach
    void setUp() {
        document = Mockito.mock(GeminiDocument.class);
        metadata = Mockito.mock(MetadataInfo.class);
        Mockito.when(document.getMetadata()).thenReturn(metadata);
    }

    @Test
    void shouldReturnListOfFilesWhenPubliclyAccessible() throws Exception {
        // given
        Multimap<Permission, String> permissions = ArrayListMultimap.create();
        permissions.put(Permission.VIEW, "public");

        Mockito.when(metadata.getPermissions()).thenReturn(permissions);
        Mockito.when(document.getResourceStatus()).thenReturn("Available");
        Mockito.when(document.getAccessLimitation())
            .thenReturn(new AccessLimitation("Open", "", "", "http://purl.org/coar/access_right/c_abf2"));
        Mockito.when(documentRepository.read("abc123")).thenReturn(document);

        var fileInfo = new HubbubResponse.FileInfo(
            2048L,
            "abc123",
            "eidchub",
            "csv",
            "dummyhash",
            0.1,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "data/sample.csv",
            "VALID",
            "sha256value",
            "text/csv"
        );

        var hubbubResponse = new HubbubResponse(
            List.of(fileInfo),
            null,
            null
        );

        Mockito.when(uploadService.get(eq("abc123"), eq("eidchub"), anyInt(), anyInt()))
            .thenReturn(hubbubResponse);

        // when / then
        mockMvc.perform(get("/documents/abc123/files"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].name", is("sample.csv")))
            .andExpect(jsonPath("$[0].path", is("data/sample.csv")))
            .andExpect(jsonPath("$[0].size", is(2048)))
            .andExpect(jsonPath("$[0].mimeType", is("text/csv")));
    }

    @Test
    void shouldReturnForbiddenWhenDatasetNotPublic() throws Exception {
        Multimap<Permission, String> permissions = ArrayListMultimap.create();
        Mockito.when(metadata.getPermissions()).thenReturn(permissions);
        Mockito.when(document.getResourceStatus()).thenReturn("Available");
        Mockito.when(document.getAccessLimitation())
            .thenReturn(new AccessLimitation("Restricted", "", "", "http://purl.org/coar/access_right/c_16ec"));
        Mockito.when(documentRepository.read("abc123")).thenReturn(document);

        mockMvc.perform(get("/documents/abc123/files"))
            .andExpect(status().isForbidden())
            .andExpect(content().string(containsString("Dataset not publicly accessible")));
    }

    @Test
    void shouldReturnNotFoundWhenDatasetMissing() throws Exception {
        Mockito.when(documentRepository.read("missing")).thenReturn(null);

        mockMvc.perform(get("/documents/missing/files"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Dataset not found")));
    }

    @Test
    void shouldReturnNoContentWhenNoFiles() throws Exception {
        Multimap<Permission, String> permissions = ArrayListMultimap.create();
        permissions.put(Permission.VIEW, "public");

        Mockito.when(metadata.getPermissions()).thenReturn(permissions);
        Mockito.when(document.getResourceStatus()).thenReturn("Available");
        Mockito.when(document.getAccessLimitation())
            .thenReturn(new AccessLimitation("Open", "", "", "http://purl.org/coar/access_right/c_abf2"));
        Mockito.when(documentRepository.read("abc123")).thenReturn(document);

        // Return HubbubResponse with null data
        var hubbubResponse = new HubbubResponse(null, null, null);
        Mockito.when(uploadService.get(eq("abc123"), eq("eidchub"), anyInt(), anyInt()))
            .thenReturn(hubbubResponse);

        mockMvc.perform(get("/documents/abc123/files"))
            .andExpect(status().isNoContent());
    }
}
