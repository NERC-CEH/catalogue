package uk.ac.ceh.gateway.catalogue.util;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import lombok.SneakyThrows;
import lombok.val;

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
        ZipFileUtils.archive(new File(directory, "dropbox/guid"), unarchived -> {
            val fileNames = FileListUtils.relativePathsTree(unarchived);
            System.out.println(String.format("fileNames %s", fileNames));
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
}
