package uk.ac.ceh.gateway.catalogue.upload;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.util.HashUtils;

public class UploadFileBuilderTest {

    private final File dataDirectory = new File("../file-test-data");
    private final File directory = new File("../file-test-data-test");

    private UploadFile uploadFile;
    private File file;

    @Before
    @SneakyThrows
    public void beforeEach() {
        if (directory.exists()) FileUtils.forceDelete(directory);
        FileUtils.copyDirectory(dataDirectory, directory);
        file = new File(directory, "upload-file-builder/_extracted-folder/folder/_extracted-sub-folder/file.txt");
        uploadFile = UploadFileBuilder.create("guid", new File(directory, "upload-file-builder"), "phyisical/location", file, UploadType.DOCUMENTS);
    }

    @Test
    @SneakyThrows
    public void addHash () {
        val actualHash = HashUtils.hash(file);
        assertThat(uploadFile.getHash(), is(actualHash));
    }

    @Test
    public void nameReplaceingExtractedWithZip () {
        assertThat(uploadFile.getName(), is("folder.zip/folder/sub-folder.zip/file.txt"));
    }

    @Test
    public void absolutePath () {
        assertThat(uploadFile.getPath(), is(file.getAbsolutePath()));
    }

    @Test
    public void setDocumentType () {
        assertThat(uploadFile.getType(), is(UploadType.DOCUMENTS));
    }

    @Test
    public void idReplaceAnyNonLetterOrNumberWithHyphen () {
        assertThat(uploadFile.getId(), is("folder-zip-folder-sub-folder-zip-file-txt"));
    }

    @Test
    public void defaultsEncoding () {
        assertThat(uploadFile.getEncoding(), is("utf-8"));
    }

    @Test
    public void setFormat () {
        assertThat(uploadFile.getFormat(), is("txt"));
    }

    @Test
    public void setMediaType () {
        assertThat(uploadFile.getMediatype(), is("text/plain"));
    }

    @Test
    public void setsBytes () {
        assertThat(uploadFile.getBytes(), is(file.length()));
    }

    @Test
    public void setsPhysicalLocation () {
        assertThat(uploadFile.getPhysicalLocation(), is("phyisical\\location\\guid\\folder.zip\\folder\\sub-folder.zip\\file.txt"));
    }

}