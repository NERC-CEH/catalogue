package uk.ac.ceh.gateway.catalogue.util;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import lombok.SneakyThrows;
import lombok.val;
import net.lingala.zip4j.exception.ZipException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ZipFileUtilsTest {

    private final File dataDirectory = new File("../file-test-data");
    private final File directory = new File("../file-test-data-test");
    private final File zipDataDirectory = new File(directory, "zip-utils");

    @Before
    @SneakyThrows
    public void beforeEach() {
        if (directory.exists()) FileUtils.forceDelete(directory);
        FileUtils.copyDirectory(dataDirectory, directory);
    }

    @Test
    public void filenameToCompessList() {
        val filename = new File(zipDataDirectory, "_extracted-zip/_extracted-sub-zip/sz.txt").getAbsolutePath();
        val compressList = ZipFileUtils.filenameToCompessList(filename);
        assertThat(compressList, containsInAnyOrder(
            new File(zipDataDirectory, "_extracted-zip").getAbsolutePath(),
            new File(zipDataDirectory, "_extracted-zip/_extracted-sub-zip").getAbsolutePath()
        ));
    }


    @Test
    public void compressListToZipFiles() {
        val filename = new File(zipDataDirectory, "_extracted-zip/_extracted-sub-zip/sz.txt").getAbsolutePath();
        val compressList = ZipFileUtils.filenameToCompessList(filename);
        val zipFiles = ZipFileUtils.compressListToZipFiles(compressList);
        assertThat(zipFiles, containsInAnyOrder(
            new File(zipDataDirectory, "zip.zip").getAbsolutePath(),
            new File(zipDataDirectory, "_extracted-zip/sub-zip.zip").getAbsolutePath()
        ));
    }

    @Test
    public void archive () {
        val zipFile = new File(zipDataDirectory, "zip.zip");
        val beforeHash = HashUtils.hash(zipFile);

        ZipFileUtils.archive(zipDataDirectory, unarchived -> {
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
        val afterHash = HashUtils.hash(zipFile);
        assertThat(beforeHash, is(afterHash));
    }

    @Test
    @SneakyThrows
    public void archive_doesNotAddZipAfterItHasBeenRemoved () {
        ZipFileUtils.archive(zipDataDirectory, unarchived -> {
            delete("zip.zip");
        });
        assertThat("zip should not exist", new File(zipDataDirectory, "zip.zip").exists(), is(false));
    }

    @Test
    @SneakyThrows
    public void archive_willCompressList () {
        val zipFile = new File(zipDataDirectory, "zip.zip");
        val beforeHash = HashUtils.hash(zipFile);
        val beforeSize = zipFile.length();

        val compressList = ZipFileUtils.filenameToCompessList(new File(zipDataDirectory, "_extracted-zip/_extracted-sub-zip").getAbsolutePath());
        ZipFileUtils.archive(compressList, zipDataDirectory, unarchived -> {
            delete("_extracted-zip/_extracted-sub-zip/sz.txt");
        });

        val afterHash = HashUtils.hash(zipFile);
        val afterSize = zipFile.length();
        assertThat(beforeHash, not(is(afterHash)));
        System.out.println(String.format("before %s after %s", beforeSize, afterSize));
        assertThat(afterSize, lessThan(beforeSize));

        ZipFileUtils.archive(zipDataDirectory, unarchived -> {
            val fileNames = FileListUtils.relativePathsTree(unarchived);
            assertThat(fileNames, containsInAnyOrder(
                "changed.txt",
                "file.txt",
                "guid.hash",
                "_extracted-zip/z.txt",
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

    @SneakyThrows
    private void delete (String filename) {
        FileUtils.forceDelete(new File(zipDataDirectory, filename));
    }

    @Test
    public void archiveZip () {
        ZipFileUtils.archiveZip(new File(zipDataDirectory, "zip.zip"), unarchived -> {
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
