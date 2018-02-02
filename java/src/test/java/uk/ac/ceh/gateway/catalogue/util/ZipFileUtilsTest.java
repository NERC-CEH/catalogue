package uk.ac.ceh.gateway.catalogue.util;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import lombok.SneakyThrows;
import lombok.val;
import net.lingala.zip4j.exception.ZipException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ZipFileUtilsTest {

    private final File dataDirectory = new File("../file-test-data");
    private final File directory = new File("../file-test-data-test");

    @Before
    @SneakyThrows
    public void beforeEach() {
        if (directory.exists()) FileUtils.forceDelete(directory);
        FileUtils.copyDirectory(dataDirectory, directory);
    }

    @Test
    public void archive () {
        ZipFileUtils.archive(new File(directory, "zip-utils"), unarchived -> {
            val fileNames = FileListUtils.relativePathsTree(unarchived);
            assertThat(fileNames, containsInAnyOrder(
                "changed.txt",
                "file.txt",
                "guid.hash",
                "_extracted-zip/z.txt",
                "_extracted-zip/_extracted-sub-zip/sz.txt",
                "_extracted-zip/_extracted-sub-zip/sub-zip.hash",
                "_extracted-zip/zip.hash",
                "_extracted-zip/sub-zip.zip",
                "zip.zip",
                "folder/sub-a.txt",
                "folder/sub-b.txt",
                "folder/folder.hash",
                "unknown.txt"
            ));
        });
    }

    @Test
    public void archiveZip () {
        ZipFileUtils.archiveZip(new File(directory, "zip-utils/zip.zip"), unarchived -> {
            val fileNames = FileListUtils.relativePathsTree(unarchived);
            assertThat(fileNames, containsInAnyOrder(
                "z.txt",
                "_extracted-sub-zip/sz.txt",
                "_extracted-sub-zip/sub-zip.hash",
                "zip.hash",
                "sub-zip.zip"
            ));
        });
    }

    @Test(expected = ZipException.class)
    public void archiveZip_doesNotConsumeAndCompressAfterException () {
        ZipFileUtils.archiveZip(new File(directory, "zip-utils-corrupt/corrupt.zip"), unarchived -> {
            fail("should not be consumed");
        });
        val extracted = new File(directory, "zip-utils-corrupt/_extracted-corrupt");
        assertThat("Should have removed the extracted file", extracted.exists(), is(false));
    }

    @Test(expected = ZipException.class)
    public void archive_doesNotConsumeAndCompressAfterException () {
        ZipFileUtils.archive(new File(directory, "zip-utils-corrupt"), unarchived -> {
            fail("should not be consumed");
        });
        val extracted = new File(directory, "zip-utils-corrupt/_extracted-corrupt");
        assertThat("Should have removed the extracted file", extracted.exists(), is(false));
    }
}
