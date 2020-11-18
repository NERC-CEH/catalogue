package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

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
    @SuppressWarnings("unchecked")
    public void successfullyGetFilenames() {
        //given
        folder.newFolder(ID);
        folder.newFile(format("%s/%s", ID, "data1.csv"));
        folder.newFile(format("%s/%s", ID, "data2.csv"));
        folder.newFile(format("%s/%s", ID, "data3.csv"));

        val data1 = new FileInfo("data1.csv");
        val data2 = new FileInfo("data1.csv");
        val data3 = new FileInfo("data2.csv");

        //when
        val filenames = service.filenames(ID);

        //then
        assertThat(filenames, hasItems(equalTo(data1), equalTo(data2), equalTo(data3)));
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