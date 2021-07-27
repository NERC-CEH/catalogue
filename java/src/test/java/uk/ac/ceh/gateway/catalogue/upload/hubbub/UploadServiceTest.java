package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController.DROPBOX;
import static uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService.BIG_PAGE_SIZE;
import static uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService.VISIBLE_STATUS;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {
    @TempDir File directory;
    private final String id = "02068a9e-8e29-42e7-938f-146aee390c98";
    private final String filename = "dataset.csv";
    private final String path = "/dropbox/" + id + "/" + filename;
    private final String badPath = "something-bad";

    @Mock
    private HubbubService hubbubService;
    private UploadService service;

    private void givenHubbubResponseForData() {
        val fileInfos = Arrays.asList(
            new FileInfo(
                456L,
                "2ac4def6d4",
                filename,
                path,
                "VALID",
                34L
            ),
            new FileInfo(
                83948L,
                "7df5e2c6",
                "data2.csv",
                "/dropbox/" + id + "/data2.csv",
                "VALID",
                543L
            )
        );
        given(hubbubService.get("/eidchub/" + id, 1, BIG_PAGE_SIZE, "VALID"))
            .willReturn(fileInfos);
    }

    private void givenHubbubResponseForDropbox() {
        val fileInfos = Arrays.asList(
            new FileInfo(
                456L,
                "2ac4def6d4",
                filename,
                path,
                "VALID",
                34L
            ),
            new FileInfo(
                83948L,
                "7df5e2c6",
                "data2.csv",
                "/dropbox/" + id + "/data2.csv",
                "VALID",
                543L
            )
        );
        given(hubbubService.get("/dropbox/" + id, 1, 20, VISIBLE_STATUS))
            .willReturn(fileInfos);
    }

    private void givenHubbubResponseForFileInfo() {
        val fileInfos = Collections.singletonList(
            new FileInfo(
                83948L,
                "7df5e2c6",
                "data2.csv",
                path,
                "VALID",
                543L
            )
        );
        given(hubbubService.get(path, 1, 1, VISIBLE_STATUS))
            .willReturn(fileInfos);
    }

    @BeforeEach
    void setup() {
        service = new UploadService(
            hubbubService,
            directory.getPath(),
            1L
        );
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

        //when
        assertThrows(UploadException.class, () ->
            service.accept(badPath)
        );

        //then
        verifyNoInteractions(hubbubService);
    }

    @Test
    void cancel() {
        //given

        //when
        service.cancel(path);

        //then
        verify(hubbubService).post("/cancel", path);
    }

    @Test
    void cancelWithBadPath() {
        //given

        //when
        assertThrows(UploadException.class, () ->
            service.accept(badPath)
        );

        //then
        verifyNoInteractions(hubbubService);
    }

    @Test void csv() {
        //given
        val printWriter = mock(PrintWriter.class);
        givenHubbubResponseForData();

        //when
        service.csv(printWriter, id);

        //then
        verify(printWriter, times(2)).println(any(String.class));
    }

    @Test
    void delete() {
        //given

        //when
        service.delete(path);

        //then
        verify(hubbubService).delete(path);
    }

    @Test
    void deleteWithBadPath() {
        //given

        //when
        assertThrows(UploadException.class, () ->
            service.delete(badPath)
        );

        //then
        verifyNoInteractions(hubbubService);
    }

    @Test
    void getForFileInfo() {
        //given
        givenHubbubResponseForFileInfo();

        //when
        service.get(id, path);
    }

    @Test
    void getForStorage() {
        //given
        givenHubbubResponseForDropbox();

        //when
        service.get(id, DROPBOX, 1, 20);
    }

    @Test
    void move() {
        //given
        val destination = "eidchub";

        //when
        service.move(path, destination);

        //then
        verify(hubbubService).postQuery("/move", path, "to", destination);
    }

    @Test
    void moveWithBadDestination() {
        //given
        val badDestination = "another";

        //when
        assertThrows(UploadException.class, () ->
            service.move(path, badDestination)
        );

        //then
        verifyNoInteractions(hubbubService);
    }

    @Test
    void moveAll() {
        //given

        //when
        service.moveAllToDataStore(id);

        //then
        verify(hubbubService).post("/move_all", id);
    }

    @Test
    void upload() {
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
    void validate() {
        //given

        //when
        service.validate(path);

        //then
        verify(hubbubService).postQuery("/validate", path, "force", "true");
    }

}