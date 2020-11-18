package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.ID;

@Slf4j
public class FileSystemStorageServiceFilenamesTest {
    private FileSystemStorageService service;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() {
        service = new FileSystemStorageService(folder.getRoot().toString());
    }

    @Test
    @SneakyThrows(IOException.class)
    public void successfullyGetFilenames() {
        //given
        folder.newFolder(ID);
        folder.newFile(format("%s/%s", ID, "data1.csv"));
        folder.newFile(format("%s/%s", ID, "data2.csv"));
        folder.newFile(format("%s/%s", ID, "data3.csv"));

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

    @Test(expected = UserInputException.class)
    public void exceptionIfIdNotKnown() {
        //given

        //when
        service.filenames(ID);

        //then
        fail("Should have thrown exception");
    }

}