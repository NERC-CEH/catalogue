package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.FileNotFoundException;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
    @SneakyThrows
    public void successfullyGetFilenames() {
        //given
        folder.newFolder(ID);
        folder.newFile(format("%s/%s", ID, "data1.csv"));
        folder.newFile(format("%s/%s", ID, "data2.csv"));
        folder.newFile(format("%s/%s", ID, "data3.csv"));

        //when
        val filenames = service.filenames(ID);

        //then
        assertThat(filenames.collect(Collectors.toList()), containsInAnyOrder("data1.csv", "data2.csv", "data3.csv"));
    }

    @Test(expected = FileNotFoundException.class)
    public void notFoundExceptionIfIdNotKnown() {
        //given

        //when
        service.filenames(ID);

        //then
        fail("Should have thrown FileNotFoundException");
    }

}