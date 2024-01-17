package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.ID;

@Slf4j
public class FileSystemStorageServiceDeleteTest {
    private FileSystemStorageService service;
    private final String filename = "data.csv";

    @TempDir Path directory;

    @BeforeEach
    public void setup() {
        service = new FileSystemStorageService(directory.toString());
    }

    @Test
    @SneakyThrows
    public void successfullyDeleteFile() {
        //given
        Path newFolder = Files.createDirectory(directory.resolve(ID));
        Files.createFile(newFolder.resolve(filename));
        val deleted = Paths.get(newFolder.toString(), filename);
        assertTrue(Files.exists(deleted));

        //when
        service.delete(ID, filename);

        //then
        assertFalse(Files.exists(deleted));
    }

    @SneakyThrows
    public void exceptionIfFileNotKnown() {
        Assertions.assertThrows(UserInputException.class, () -> {
            //given
            // No files setup

            //when
            service.delete(ID, filename);

            //then
            fail("Should have thrown exception");
        });
    }

}
