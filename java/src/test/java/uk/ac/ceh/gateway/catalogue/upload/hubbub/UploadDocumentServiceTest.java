package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.google.common.util.concurrent.MoreExecutors;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UploadDocumentServiceTest {
    @TempDir File directory;
    private final String id = "02068a9e-8e29-42e7-938f-146aee390c98";
    private final String filename = "dataset.csv";
    private final String path = "/dropbox/" + id + "/" + filename;

    @Mock
    private HubbubService hubbubService;
    private UploadDocumentService service;

    @BeforeEach
    void setup() {
        service = new UploadDocumentService(
            hubbubService,
            directory.getPath(),
            MoreExecutors.newDirectExecutorService()
        );
    }

    @Test
    void add() {
        //given
        val multipartFile = new MockMultipartFile(
            "file",
            filename,
            "text/csv",
            "some file content".getBytes(UTF_8)
        );

        //when
        service.upload(id, multipartFile);

        //then
        verify(hubbubService).postQuery("/writing", path, "size", "17");
        verify(hubbubService).post("/accept", path);
        verify(hubbubService).postQuery("/validate", path, "force", "true");
        val uploadedFile = Paths.get(directory.getPath(), id, filename);
        assertTrue(Files.exists(uploadedFile));
    }

    @Test
    void accept() {
        //given

        //when
        service.accept(path);

        //then
        verify(hubbubService).post("/accept", path);
    }

    @Test
    void acceptWithBadPath() {
        //given
        val badPath = "something-bad";

        //when
        assertThrows(UploadException.class, () ->
            service.accept(badPath)
        );

        //then
        verifyNoInteractions(hubbubService);
    }

    @Test
    void delete() {
        //given

        //when
        service.delete(path);

        //then
        verify(hubbubService).delete(path);
    }

}