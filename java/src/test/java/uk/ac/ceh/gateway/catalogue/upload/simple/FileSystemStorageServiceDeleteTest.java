package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.ID;

@Slf4j
public class FileSystemStorageServiceDeleteTest {
    private FileSystemStorageService service;
    private final String filename = "data.csv";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() {
        service = new FileSystemStorageService(folder.getRoot().toString());
    }

    @Test
    @SneakyThrows
    public void successfullyDeleteFile() {
        //given
        folder.newFolder(ID);
        folder.newFile(format("%s/%s", ID, filename));
        val deleted = Paths.get(folder.getRoot().toString(), ID, filename);
        assertTrue(Files.exists(deleted));

        //when
        service.delete(ID, filename);

        //then
        assertFalse(Files.exists(deleted));
    }

    @Test(expected = NoSuchFileException.class)
    @SneakyThrows
    public void notFoundExceptionIfFileNotKnown() {
        //given

        //when
        service.delete(ID, filename);

        //then
        fail("Should have thrown FileNotFoundException");
    }

}