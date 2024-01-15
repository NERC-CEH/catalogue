package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.ID;

@Slf4j
public class FileSystemStorageServiceFilenamesTest {
    private FileSystemStorageService service;

    @TempDir
    Path directory;

    @BeforeEach
    public void setup() {
        service = new FileSystemStorageService(directory.toString());
    }

    @Test
    @SneakyThrows(IOException.class)
    public void successfullyGetFilenames() {
        //given
        Path newFolder = Files.createDirectory(directory.resolve(ID));
        Files.createFile(newFolder.resolve("data1.csv"));
        Files.createFile(newFolder.resolve("data2.csv"));
        Files.createFile(newFolder.resolve("data3.csv"));

        val expected = Arrays.asList(
                new FileInfo("data1.csv"),
                new FileInfo("data2.csv"),
                new FileInfo("data3.csv")
        );

        //when
        val filenames = service.filenames(ID);

        //then
        assertThat(filenames, equalTo(expected));
    }

    public void exceptionIfIdNotKnown() {
        Assertions.assertThrows(UserInputException.class, () -> {
            //given

            //when
            service.filenames(ID);

            //then
            fail("Should have thrown exception");
        });
    }

}
