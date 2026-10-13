package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController.DATASTORE;
import static uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController.DROPBOX;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {
    private MockRestServiceServer mockServer;
    private byte[] success;
    @TempDir File directory;
    private final String datasetId = "c5db2755-bdbb-470f-987b-da71d9489fd0";
    private final String datastore = "eidchub";
    private final String path = "dataset.csv";
    private final String username = "tester";

    private UploadService service;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        val restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        service = new UploadService(
            restTemplate,
            "https://example.com/v7",
            "hubbub-token",
            directory.getPath()
        );
        success = IOUtils.toByteArray(
            Objects.requireNonNull(
                getClass().getResource("hubbub-dropbox-response.json")
            )
        );
    }

    @Test
    void refusesToStartWithoutAToken() {
        assertThrows(IllegalArgumentException.class,
            () -> new UploadService(new RestTemplate(), "https://example.com/v7", " ", directory.getPath()));
    }

    @Test
    void accept() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/accept/c5db2755-bdbb-470f-987b-da71d9489fd0/eidchub")))
            .andExpect(method(POST))
            .andExpect(queryParam("path", path))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        //when
        service.accept(datasetId, datastore, path, username);

        //then
        mockServer.verify();
    }

    @Test
    void cancel() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/cancel/c5db2755-bdbb-470f-987b-da71d9489fd0/eidchub")))
            .andExpect(method(POST))
            .andExpect(queryParam("path", path))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        //when
        service.cancel(datasetId, datastore, path, username);

        //then
        mockServer.verify();
    }

    @Test void csv() {
        //given
        val printWriter = mock(PrintWriter.class);
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/c5db2755-bdbb-470f-987b-da71d9489fd0/eidchub")))
            .andExpect(method(GET))
            .andExpect(queryParam("page", "1"))
            .andExpect(queryParam("size", String.valueOf(UploadService.PAGE_SIZE)))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        service.csv(printWriter, datasetId);

        //then
        verify(printWriter).println("path,SHA256_checksum");
        verify(printWriter).println("c5db2755-bdbb-470f-987b-da71d9489fd0/CBESS_Eddy_Covariance_data_Cartmel_Sands.csv,aaaa1111aaaa1111aaaa1111aaaa1111aaaa1111aaaa1111aaaa1111aaaa1111");
        verify(printWriter).println("c5db2755-bdbb-470f-987b-da71d9489fd0/CBESS_data_Cartmel_Sands.csv,bbbb2222bbbb2222bbbb2222bbbb2222bbbb2222bbbb2222bbbb2222bbbb2222");
        mockServer.verify();
    }

    @Test
    void delete() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/delete/c5db2755-bdbb-470f-987b-da71d9489fd0/eidchub")))
            .andExpect(method(DELETE))
            .andExpect(queryParam("path", path))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        //when
        service.delete(datasetId, datastore, path, username);

        //then
        mockServer.verify();
    }

    @Test
    void get() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/c5db2755-bdbb-470f-987b-da71d9489fd0/dropbox")))
            .andExpect(method(GET))
            .andExpect(queryParam("page", "1"))
            .andExpect(queryParam("size", "20"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        service.get(datasetId, DROPBOX, 1, 20);

        //then
        mockServer.verify();
    }

    @Test
    void getIndividual() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/c5db2755-bdbb-470f-987b-da71d9489fd0/dropbox")))
            .andExpect(method(GET))
            .andExpect(queryParam("path", path))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        service.get(datasetId, DROPBOX, path);

        //then
        mockServer.verify();
    }

    @Test
    void hashDropbox() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/hash/c5db2755-bdbb-470f-987b-da71d9489fd0")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        //when
        service.hashDropbox(datasetId, username);

        //then
        mockServer.verify();
    }

    @Test
    void move() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/move/c5db2755-bdbb-470f-987b-da71d9489fd0/dropbox")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("path", path))
            .andExpect(queryParam("username", username))
            .andExpect(queryParam("to", DATASTORE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        //when
        service.move(datasetId, DROPBOX, Optional.of(path), username, DATASTORE);

        //then
        mockServer.verify();
    }

    @Test
    void moveAll() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/move/c5db2755-bdbb-470f-987b-da71d9489fd0/dropbox")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("username", username))
            .andExpect(queryParam("to", DATASTORE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        //when
        service.move(datasetId, DROPBOX, Optional.empty(), username, DATASTORE);

        //then
        mockServer.verify();
    }

    @Test
    void upload() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/register/c5db2755-bdbb-470f-987b-da71d9489fd0")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("path", path))
            .andExpect(queryParam("username", username))
            .andExpect(queryParam("size", "17"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        val multipartFile = new MockMultipartFile(
            "file",
            path,
            "text/csv",
            "some file content".getBytes(UTF_8)
        );

        //when
        service.upload(datasetId, username, multipartFile, path);

        //then
        mockServer.verify();
        val uploadedFile = Paths.get(directory.getPath(), datasetId, path);
        assertTrue(Files.exists(uploadedFile));
    }

    @Test
    @SneakyThrows
    void uploadWithZipFile() {
        //given
        String zipFile = "data.zip";
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/register/c5db2755-bdbb-470f-987b-da71d9489fd0")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("path", "data/sub-folder1/data2.txt"))
            .andExpect(queryParam("username", username))
            .andExpect(queryParam("size", "22"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/register/c5db2755-bdbb-470f-987b-da71d9489fd0")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("path", "data/data1.txt"))
            .andExpect(queryParam("username", username))
            .andExpect(queryParam("size", "10"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        val multipartFile = new MockMultipartFile(
            "file",
            null,
            "application/zip",
            IOUtils.toByteArray(getClass().getResource(zipFile))
        );

        //when
        service.upload(datasetId, username, multipartFile, zipFile);

        //then
        mockServer.verify();
        Path uploadedFile = Paths.get(directory.getPath(), datasetId, "data/data1.txt");
        assertTrue(Files.exists(uploadedFile));
        uploadedFile = Paths.get(directory.getPath(), datasetId, "data/sub-folder1/data2.txt");
        assertTrue(Files.exists(uploadedFile));

        Path macosxDir = Paths.get(directory.getPath(), datasetId, "data/__macosx");
        assertFalse(Files.exists(macosxDir), "__MACOSX directory should be filtered out");
    }

    @Test
    @SneakyThrows
    void uploadWithZipFileInsideAFolderRegistersWhereTheEntriesAreWritten() {
        // A zip dropped inside a folder is unpacked under that folder; register the path the file
        // is written to (previously registered as data/..., while written to sub/data/...)
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/register/c5db2755-bdbb-470f-987b-da71d9489fd0")))
            .andExpect(queryParam("path", "sub/data/sub-folder1/data2.txt"))
            .andRespond(withNoContent());
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/register/c5db2755-bdbb-470f-987b-da71d9489fd0")))
            .andExpect(queryParam("path", "sub/data/data1.txt"))
            .andRespond(withNoContent());
        val multipartFile = new MockMultipartFile(
            "file", null, "application/zip", IOUtils.toByteArray(getClass().getResource("data.zip"))
        );

        service.upload(datasetId, username, multipartFile, "sub/data.zip");

        mockServer.verify();
        assertTrue(Files.exists(Paths.get(directory.getPath(), datasetId, "sub/data/data1.txt")));
    }

    @Test
    void uploadWithoutFilename() {
        //given
        val multipartFile = new MockMultipartFile(
            "file",
            null,
            "text/csv",
            "some file content".getBytes(UTF_8)
        );

        //when
        assertThrows(UploadException.class, () ->
            service.upload(datasetId, username, multipartFile, null)
        );

        //then
    }

    @Test
    void uploadWithUppercaseAndSpacesInFilename() {
        //given
        val filename = "Dataset With Spaces And Uppercase.csv";
        val expectedFilename = "dataset-with-spaces-and-uppercase.csv";
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/register/c5db2755-bdbb-470f-987b-da71d9489fd0")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("path", expectedFilename))
            .andExpect(queryParam("username", username))
            .andExpect(queryParam("size", "17"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());
        val multipartFile = new MockMultipartFile(
            "file",
            filename,
            "text/csv",
            "some file content".getBytes(UTF_8)
        );

        //when
        service.upload(datasetId, username, multipartFile, filename);

        //then
        mockServer.verify();
        val uploadedFile = Paths.get(directory.getPath(), datasetId, expectedFilename);
        assertTrue(Files.exists(uploadedFile));
    }

    @Test
    void registerAllDropboxFiles() {
        // given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/register/c5db2755-bdbb-470f-987b-da71d9489fd0")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        // when
        service.register(datasetId, username);

        // then
        mockServer.verify();
    }

    @Test
    void unregister() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/unregister/c5db2755-bdbb-470f-987b-da71d9489fd0/eidchub")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("path", path))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        //when
        service.unregister(datasetId, datastore, path, username);

        //then
        mockServer.verify();
    }

    @Test
    void validate() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/validate/c5db2755-bdbb-470f-987b-da71d9489fd0/dropbox")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("path", path))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        //when
        service.validate(datasetId, DROPBOX, Optional.of(path), username);

        //then
        mockServer.verify();
    }

    @Test
    void validateAll() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/validate/c5db2755-bdbb-470f-987b-da71d9489fd0/dropbox")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer hubbub-token"))
            .andRespond(withNoContent());

        //when
        service.validate(datasetId, DROPBOX, Optional.empty(), username);

        //then
        mockServer.verify();
    }

    @Test
    void refusesAFilenameThatEscapesTheRecordFolder() {
        // dri-one #453: nothing may be registered, created or written outside the record's folder
        val multipartFile = new MockMultipartFile("file", "x.csv", "text/csv", "some file content".getBytes(UTF_8));

        assertThrows(UploadException.class, () ->
            service.upload(datasetId, username, multipartFile, "../other-record/evil.csv")
        );

        mockServer.verify();
        assertFalse(Files.exists(Paths.get(directory.getPath(), "other-record")));
    }

    @Test
    @SneakyThrows
    void refusesAZipEntryThatEscapesTheRecordFolder() {
        // zip-slip: the entry would otherwise be written before Hubbub's register is called
        // The unzip folder exists after an earlier upload of the same zip name
        Files.createDirectories(Paths.get(directory.getPath(), datasetId, "data"));
        val zipBytes = new ByteArrayOutputStream();
        try (val zip = new ZipOutputStream(zipBytes)) {
            zip.putNextEntry(new ZipEntry("../../evil.txt"));
            zip.write("evil".getBytes(UTF_8));
            zip.closeEntry();
        }
        val multipartFile = new MockMultipartFile("file", null, "application/zip", zipBytes.toByteArray());

        assertThrows(UploadException.class, () ->
            service.upload(datasetId, username, multipartFile, "data.zip")
        );

        mockServer.verify();
        assertFalse(Files.exists(Paths.get(directory.getPath(), "evil.txt")));
    }
}
