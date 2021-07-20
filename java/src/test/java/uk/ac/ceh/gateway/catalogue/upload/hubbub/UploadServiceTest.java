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
            new HubbubResponse.FileInfo(
                456L,
                "csv",
                "2ac4def6d4",
                id,
                "text/csv",
                "10239",
                filename,
                path,
                "/mnt" + path,
                "VALID",
                34L
            ),
            new HubbubResponse.FileInfo(
                83948L,
                "csv",
                "7df5e2c6",
                id,
                "text/csv",
                "209912",
                "data2.csv",
                "/dropbox/" + id + "/data2.csv",
                "/mnt/dropbox/" + id + "/data2.csv",
                "VALID",
                543L
            )
        );
        val pagination = new HubbubResponse.Pagination(1, 20, 43);
        val hubbubResponse = new HubbubResponse(fileInfos, pagination);
        given(hubbubService.get("/eidchub/" + id, 1, 43))
            .willReturn(hubbubResponse);
    }

    private void givenHubbubResponseForGet() {
        val dropbox = new HubbubResponse(Collections.emptyList(), new HubbubResponse.Pagination(1, 10, 26));
        val datastore = new HubbubResponse(Collections.emptyList(), new HubbubResponse.Pagination(2, 10, 26));
        val supportingDocs = new HubbubResponse(Collections.emptyList(), new HubbubResponse.Pagination(3, 10, 26));
        given(hubbubService.get("/dropbox/" + id, 1, VISIBLE_STATUS))
            .willReturn(dropbox);
        given(hubbubService.get("/eidchub/" + id, 2, VISIBLE_STATUS))
            .willReturn(datastore);
        given(hubbubService.get("/supporting-documents/" + id, 3, VISIBLE_STATUS))
            .willReturn(supportingDocs);
    }

    private void givenHubbubResponseForTotal() {
        val pagination = new HubbubResponse.Pagination(1, 20, 43);
        val hubbubResponse = new HubbubResponse(Collections.emptyList(), pagination);
        given(hubbubService.get("/eidchub/" + id))
            .willReturn(hubbubResponse);
    }

    @BeforeEach
    void setup() {
        service = new UploadService(
            hubbubService,
            directory.getPath()
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
        givenHubbubResponseForTotal();
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
    void get() {
        //given
        givenHubbubResponseForGet();

        //when
        service.get(id, 1, 2, 3);

        //then
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