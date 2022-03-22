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

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
            "hubbub",
            "password01234",
            directory.getPath()
        );
        success = IOUtils.toByteArray(
            Objects.requireNonNull(
                getClass().getResource("hubbub-dropbox-response.json")
            )
        );
    }

    @Test
    void accept() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/accept/c5db2755-bdbb-470f-987b-da71d9489fd0/eidchub")))
            .andExpect(method(POST))
            .andExpect(queryParam("path", path))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
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
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
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
            .andExpect(queryParam("size", "1000000"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
            .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        service.csv(printWriter, datasetId);

        //then
        verify(printWriter, times(3)).println(any(String.class));
        mockServer.verify();
    }

    @Test
    void delete() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/c5db2755-bdbb-470f-987b-da71d9489fd0/eidchub")))
            .andExpect(method(DELETE))
            .andExpect(queryParam("path", path))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
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
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
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
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
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
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
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
            .andExpect(queryParam("destination", DATASTORE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
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
            .andExpect(queryParam("destination", DATASTORE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
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
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
            .andRespond(withNoContent());

        val multipartFile = new MockMultipartFile(
            "file",
            path,
            "text/csv",
            "some file content".getBytes(UTF_8)
        );

        //when
        service.upload(datasetId, username, multipartFile);

        //then
        mockServer.verify();
        val uploadedFile = Paths.get(directory.getPath(), datasetId, path);
        assertTrue(Files.exists(uploadedFile));
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
            service.upload(datasetId, username, multipartFile)
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
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
            .andRespond(withNoContent());
        val multipartFile = new MockMultipartFile(
            "file",
            filename,
            "text/csv",
            "some file content".getBytes(UTF_8)
        );

        //when
        service.upload(datasetId, username, multipartFile);

        //then
        mockServer.verify();
        val uploadedFile = Paths.get(directory.getPath(), datasetId, expectedFilename);
        assertTrue(Files.exists(uploadedFile));
    }

    @Test
    void validate() {
        //given
        mockServer
            .expect(requestTo(startsWith("https://example.com/v7/validate/c5db2755-bdbb-470f-987b-da71d9489fd0/dropbox")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(queryParam("path", path))
            .andExpect(queryParam("username", username))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
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
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
            .andRespond(withNoContent());

        //when
        service.validate(datasetId, DROPBOX, Optional.empty(), username);

        //then
        mockServer.verify();
    }
}
