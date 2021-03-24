package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.*;

@Slf4j
public class FileSystemStorageServiceStoreTest {
    private final String filename = "data.csv";
    private FileSystemStorageService service;
    private Path expected;

    @TempDir
    Path directory;

    @BeforeEach
    public void setup() {
        service = new FileSystemStorageService(directory.toString());
        expected = directory.resolve(ID).resolve(filename);
    }

    @Test
    @SneakyThrows
    public void successfullyStoreFile() {
        //given

        //when
        service.store(ID, dataCsv(getClass()));

        //then
        assertTrue(Files.exists(expected));
        val lines = Files.readAllLines(expected, StandardCharsets.UTF_8);
        assertTrue(lines.contains("\"A\",\"B\",\"C\""));
        assertTrue(lines.contains("1,2,3"));
        assertTrue(lines.contains("4,5,6"));
        assertTrue(lines.contains("7,8,9"));
    }

    @SneakyThrows
    @Test
    public void fileAlreadyExists() {
        Assertions.assertThrows(FileExitsException.class, () -> {
            //given
            Path newFolder = Files.createDirectory(directory.resolve(ID));
            Path file = newFolder.resolve(filename);
            Files.createFile(file);

            service.store(ID, dataCsv(getClass()));

            //then
            fail("Should throw FileExistsException");
        });
    }

    @Test
    public void createdDirectoryOnFirstUploadAndCanUploadMultipleFiles() {
        //given
        val fileWithSpaces = directory.resolve(ID).resolve("file with spaces.csv");
        assertFalse(Files.exists(directory.resolve(ID)));

        //when
        service.store(ID, dataCsv(getClass()));
        service.store(ID, fileWithSpacesCsv(getClass()));

        //then
        assertTrue(Files.exists(expected));
        assertTrue(Files.exists(fileWithSpaces));
    }

}
