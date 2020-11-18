package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.*;

@Slf4j
public class FileSystemStorageServiceStoreTest {
    private final String filename = "data.csv";
    private FileSystemStorageService service;
    private Path expected;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() {
        service = new FileSystemStorageService(folder.getRoot().toString());
        expected = folder.getRoot().toPath().resolve(ID).resolve(filename);
    }

    @Test
    @SneakyThrows
    public void successfullyStoreFile() {
        //given
        val expectedLines = new String[]{
                "\"A\",\"B\",\"C\"",
                "1,2,3",
                "4,5,6",
                "7,8,9"
        };

        //when
        service.store(ID, dataCsv(getClass()));

        //then
        assertTrue(Files.exists(expected));
        val lines = Files.readAllLines(expected, UTF_8);
        assertThat(lines, contains(expectedLines));
    }

    @SneakyThrows
    @Test(expected = FileExitsException.class)
    public void fileAlreadyExists() {
        //given
        folder.newFolder(ID);
        folder.newFile(format("/%s/%s", ID, filename));

        //when
        service.store(ID, dataCsv(getClass()));

        //then
        fail("Should throw FileExistsException");
    }

    @Test
    public void createdDirectoryOnFirstUploadAndCanUploadMultipleFiles() {
        //given
        val fileWithSpaces = folder.getRoot().toPath().resolve(ID).resolve("file with spaces.csv");
        assertFalse(Files.exists(folder.getRoot().toPath().resolve(ID)));

        //when
        service.store(ID, dataCsv(getClass()));
        service.store(ID, fileWithSpacesCsv(getClass()));

        //then
        assertTrue(Files.exists(expected));
        assertTrue(Files.exists(fileWithSpaces));
    }

}