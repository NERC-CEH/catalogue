package uk.ac.ceh.gateway.catalogue.file;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FilesystemRepositoryTest {
    private FileRepository fileRepository;

    @Before
    public void setUp() {
        fileRepository = new FilesystemRepository(
            new ClassRelativeResourceLoader(getClass()),
            "datastore"
        );
    }

    @Test
    public void create() {
    }

    @Test
    @SneakyThrows
    public void successfullyRead() {
        //given
        val expected =StreamUtils.copyToString(
            getClass().getResourceAsStream("datastore/dataset0.json"), Charset.forName("UTF-8")
        );

        //when
        val actual = fileRepository.read("dataset0");

        //then
        assertThat(actual, equalTo(expected));
    }

    @SneakyThrows
    @Test(expected = UnknownFileException.class)
    public void unknownRead() {
        //when
        fileRepository.read("unknown");

        //then
        fail("UnknownFileException expected");
    }

    @Test
    public void update() {
    }

    @Test
    public void delete() {
    }
}