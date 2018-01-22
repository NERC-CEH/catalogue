package uk.ac.ceh.gateway.catalogue.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload.Type;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class DocumentUploadServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MetadataDocument document;

    private static final File documentUploadTestFolder = new File("../document-upload-test-main");
    private static final File checksums = new File(documentUploadTestFolder, "guid/checksums.hash");
    
    private static final File documentUploadTestFolderSecond = new File("../document-upload-test-second");
    
    private static final List<String> checksumLines = Lists.newArrayList(
            "4e87705c2a9abae1f29425c87a6749ed *missing-checksum.txt",
            "e729936bf5360b37a15365fc295a1901 *incorrect-checksum.txt",
            "437b930db84b8079c2dd804a71936b5f *valid-checksum.txt");

    private static final File file = new File(documentUploadTestFolder, "file.txt");
    private static final File incorrectChecksumFile = new File(documentUploadTestFolder, "guid/incorrect-checksum.txt");
    private static final File checksumFile = new File(documentUploadTestFolder, "guid/valid-checksum.txt");
    private static final File invalid = new File(documentUploadTestFolder, "guid/invalid.txt");

    private DocumentUploadService dus, dusSecond;

    @Before
    @SneakyThrows
    public void before() {
        FileUtils.forceMkdir(documentUploadTestFolder);
        FileUtils.forceMkdir(documentUploadTestFolderSecond);
        FileUtils.write(file, "something", Charset.defaultCharset());
        FileUtils.write(checksumFile, "something", Charset.defaultCharset());
        FileUtils.write(incorrectChecksumFile, "something else", Charset.defaultCharset());
        FileUtils.write(invalid, "invalid content", Charset.defaultCharset());

        FileUtils.writeLines(checksums, checksumLines);

        initMocks(this);

        doReturn(document).when(documentRepository).read(anyString());
        doReturn("title").when(document).getTitle();
        doReturn("type").when(document).getType();

        dus = new DocumentUploadService(documentUploadTestFolder, documentRepository);
        dusSecond = new DocumentUploadService(documentUploadTestFolderSecond, documentRepository);
    }

    @After
    @SneakyThrows
    public void after() {
        FileUtils.forceDelete(documentUploadTestFolder);
        FileUtils.forceDelete(documentUploadTestFolderSecond);
    }

    @Test
    @SneakyThrows
    public void readsFromChecksum() {
        val actual = dus.get("guid");
        hasData(actual, "valid-checksum.txt");
    }

    @Test
    @SneakyThrows
    public void invalidIfDoesNotExistFromChecksum() {
        val actual = dus.get("guid");
        val type = actual.getInvalid().get("missing-checksum.txt").getType();
        assertThat(type, equalTo(Type.MISSING_FILE.name()));
    }

    @Test
    @SneakyThrows
    public void invalidIfChecksumIsInvalid() {
        val actual = dus.get("guid");
        val type = actual.getInvalid().get("incorrect-checksum.txt").getType();
        assertThat(type, equalTo(Type.INVALID_HASH.name()));
    }

    @Test
    @SneakyThrows
    public void invalidFile_ifNotInInFolder() {
        val actual = dus.get("guid");
        hasInvalid(actual, "invalid.txt");
    }

    @Test
    @SneakyThrows
    public void updatesChecksumsWithFilesWhichHaveBeenAdded() {
        dus.add("guid", "file.txt", new FileInputStream(file));
        val lines = FileUtils.readLines(checksums);
        assertThat(lines, hasItem(equalTo("437b930db84b8079c2dd804a71936b5f *file.txt")));
    }

    @Test
    @SneakyThrows
    public void doesNotRemoveOldChecksusms() {
        dus.add("guid", "file.txt", new FileInputStream(file));
        val lines = FileUtils.readLines(checksums);
        assertThat(lines, hasItem(equalTo("437b930db84b8079c2dd804a71936b5f *valid-checksum.txt")));
    }

    @Test
    @SneakyThrows
    public void invalidFile_ifNotInDataOrMetaButInFolder_addComment() {
        val actual = dus.get("guid");
        val comments = actual.getInvalid().get("invalid.txt").getCommentsAsString();
        assertThat(comments, equalTo("Unknown file"));
    }

    @Test
    @SneakyThrows
    public void invalidFile_ifFileDoesNotExistInData() {
        dus.add("guid", "invalid.txt", new FileInputStream(invalid));

        FileUtils.forceDelete(invalid);

        val actual = dus.get("guid");
        assertThat(actual.getInvalid().get("invalid.txt").getType(), equalTo(Type.MISSING_FILE.name()));
    }

    @Test
    @SneakyThrows
    public void invalidFile_ifFileDoesNotExist_addAComment() {
        dus.add("guid", "invalid.txt", new FileInputStream(invalid));

        FileUtils.forceDelete(invalid);

        val actual = dus.get("guid");
        val comments = actual.getInvalid().get("invalid.txt").getLatestComment();
        assertThat(comments, equalTo("File is missing"));
    }

    @Test
    @SneakyThrows
    public void acceptInvalid_movesTheInvalidFileToData() {
        dus.acceptInvalid("guid", "invalid.txt");
        val actual = dus.get("guid");
        hasData(actual, "invalid.txt");
    }

    @Test
    @SneakyThrows
    public void get_hasTitleFromDocument() {
        val actual = dus.get("guid");

        assertThat(actual.getTitle(), equalTo("title"));
    }

    @Test
    @SneakyThrows
    public void get_hasTypeFromDocument() {
        val actual = dus.get("guid");

        assertThat(actual.getType(), equalTo("type"));
    }

    @Test
    @SneakyThrows
    public void get_hasGuid() {
        val actual = dus.get("guid");

        assertThat(actual.getGuid(), equalTo("guid"));
    }

    @Test
    @SneakyThrows
    public void get_hasPath() {
        val actual = dus.get("guid");

        assertThat(actual.getPath(), equalTo(new File(documentUploadTestFolder, "guid").getAbsolutePath()));
    }

    @Test
    @SneakyThrows
    public void get_autoFixesData() {
        val in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        val documentUpload = dus.get("guid");
        val documentUploadFile = documentUpload.getDocuments().get("file.txt");
        documentUploadFile.setBytes(999999);
        documentUploadFile.setEncoding("encoding");
        documentUploadFile.setFormat("format");
        documentUploadFile.setMediatype("mediatype");
        documentUploadFile.setName("name");
        documentUploadFile.setPath("path");
        documentUploadFile.setType("BACON");
        documentUpload.getDocuments().put("file.txt", documentUploadFile);
        val dataFile = new File(documentUpload.getPath(), "_data.json");
        val mapper = new ObjectMapper();
        mapper.writeValue(dataFile, documentUpload);

        val actual = dus.get("guid").getDocuments().get("file.txt");
        assertThat(actual.getName(), equalTo("file.txt"));
        assertThat(actual.getBytes(), equalTo(9L));
        assertThat(actual.getEncoding(), equalTo("utf-8"));
        assertThat(actual.getFormat(), equalTo("txt"));
        assertThat(actual.getMediatype(), equalTo("text/plain"));
        assertThat(actual.getPath(), equalTo(new File(documentUploadTestFolder, "guid/file.txt").getAbsolutePath()));
        assertThat(actual.getType(), equalTo("DOCUMENTS"));
    }

    @Test
    @SneakyThrows
    public void delete_doesNothingIfNothingHasBeenAdded() {
        dus.delete("guid", "file.txt");

        val actual = dus.get("guid");
        doesNotExists(actual, "file.txt");
    }

    @Test
    @SneakyThrows
    public void delete_removesAddedFile() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        dus.delete("guid", "file.txt");

        val actual = dus.get("guid");
        doesNotExists(actual, "file.txt");
    }

    @Test
    @SneakyThrows
    public void delete_removeTheFileFromDisk() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        dus.delete("guid", "file.txt");
        assertThat("file exists", !new File(documentUploadTestFolder, "guid/file.txt").exists());
    }

    @Test
    @SneakyThrows
    public void add_addsToData() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        val actual = dus.get("guid");
        hasData(actual, "file.txt");
    }

    @Test
    @SneakyThrows
    public void add_saveFileToDisk() {
        assertThat("file exists", !new File(documentUploadTestFolder, "guid/file.txt").exists());
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        assertThat("file does not exist", new File(documentUploadTestFolder, "guid/file.txt").exists());
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasBytes() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        val actual = dus.get("guid");
        val documentUploadFile = actual.getDocuments().get("file.txt");
        assertThat(documentUploadFile.getBytes(), equalTo(9L));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasEncodingUtf8() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        val actual = dus.get("guid");
        val documentUploadFile = actual.getDocuments().get("file.txt");
        assertThat(documentUploadFile.getEncoding(), equalTo("utf-8"));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasFormat() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        val actual = dus.get("guid");
        val documentUploadFile = actual.getDocuments().get("file.txt");
        assertThat(documentUploadFile.getFormat(), equalTo("txt"));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasHash() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        val actual = dus.get("guid");
        val documentUploadFile = actual.getDocuments().get("file.txt");
        assertThat(documentUploadFile.getHash(), equalTo("437b930db84b8079c2dd804a71936b5f"));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasMediatype() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        val actual = dus.get("guid");
        val documentUploadFile = actual.getDocuments().get("file.txt");
        assertThat(documentUploadFile.getMediatype(), equalTo("text/plain"));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasName() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        val actual = dus.get("guid");
        val documentUploadFile = actual.getDocuments().get("file.txt");
        assertThat(documentUploadFile.getName(), equalTo("file.txt"));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasAbsolutePath() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        val actual = dus.get("guid");
        val documentUploadFile = actual.getDocuments().get("file.txt");
        assertThat(documentUploadFile.getPath(),
                equalTo(new File(documentUploadTestFolder, "guid/file.txt").getAbsolutePath()));
    }

    @Test
    @SneakyThrows
    public void add_onlyAddsOnce() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        val actual = dus.get("guid");
        hasData(actual, "file.txt");
    }

    @Test
    @SneakyThrows
    public void zip_createsAZipFile() {
        dus.zip("guid");
        val file = new File(documentUploadTestFolder, "guid/guid.zip");
        assertThat(file.exists(), is(true));
    }

    @Test
    @SneakyThrows
    public void zip_removeFilesWhichWereZipped() {
        dus.zip("guid");
        val file = new File(documentUploadTestFolder, "guid/valid-checksum.txt");
        assertThat(file.exists(), is(false));
    }

    @Test
    @SneakyThrows
    public void zip_updatesChecksumWithZipChecksum() {
        dus.zip("guid");
        val file = new File(documentUploadTestFolder, "guid/checksums.hash");
        val lines = FileUtils.readLines(file, Charset.defaultCharset());

        assertThat(lines.size(), is(1));
        assertThat(lines.get(0), containsString("*guid.zip"));
    }

    @Test
    @SneakyThrows
    public void unzip_removesZip() {
        dus.zip("guid");
        dus.unzip("guid");

        val file = new File(documentUploadTestFolder, "guid/guid.zip");
        assertThat(file.exists(), is(false));
    }

    @Test
    @SneakyThrows
    public void unzip_putsFilesWhichWereZippedBack() {
        dus.zip("guid");
        dus.unzip("guid");

        val file = new File(documentUploadTestFolder, "guid/valid-checksum.txt");
        assertThat(file.exists(), is(true));
    }

    @Test
    @SneakyThrows
    public void unzip_putsChecksumBack() {
        dus.zip("guid");
        dus.unzip("guid");

        val file = new File(documentUploadTestFolder, "guid/checksums.hash");
        val lines = FileUtils.readLines(file, Charset.defaultCharset());

        assertThat(lines.size(), is(3));
        assertThat(lines.get(2), containsString("*valid-checksum.txt"));
    }

    @Test
    @SneakyThrows
    public void zip_addsNewFilesToZip() {
        dus.zip("guid");

        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);

        val file = new File(documentUploadTestFolder, "guid/file.txt");
        assertThat(file.exists(), is(false));

        val actual = dus.get("guid");
        hasData(actual, "file.txt");
    }

    @Test
    @SneakyThrows
    public void zip_removesFilesFromZip() {
        dus.zip("guid");

        dus.delete("guid", "valid-checksum.txt");

        val actual = dus.get("guid");
        doesNotHaveData(actual, "valid-checksum.txt");
    }

    @Test
    @SneakyThrows
    public void zip_putsInvalidFilesIntoTheZip() {
        dus.zip("guid");

        dus.acceptInvalid("guid", "invalid.txt");

        val file = new File(documentUploadTestFolder, "guid/invalid.txt");
        assertThat(file.exists(), is(false));

        val actual = dus.get("guid");
        hasData(actual, "invalid.txt");
    }

    @Test
    @SneakyThrows
    public void move_takesOneFileFromOnePlaceToAnother() {
        dus.move("guid", "valid-checksum.txt", dusSecond);
        doesNotExists(dus.get("guid"), "valid-checksum.txt");
        hasData(dusSecond.get("guid"), "valid-checksum.txt");
    }

    @Test
    @SneakyThrows
    public void canBeAnyNamedChecksumFile () {
       checksums.renameTo(new File(documentUploadTestFolder, "guid/anything.hash"));

        val actual = dus.get("guid");
        hasData(actual, "valid-checksum.txt");
    }

    private void hasData(DocumentUpload actual, String name) {
        assertThat(actual.getDocuments().keySet(), hasItem(equalTo(name)));
        doesNotHaveInvalid(actual, name);
    }

    private void hasInvalid(DocumentUpload actual, String name) {
        assertThat(actual.getInvalid().keySet(), hasItem(equalTo(name)));
        doesNotHaveData(actual, name);
    }

    private void doesNotHaveData(DocumentUpload actual, String name) {
        assertThat(actual.getDocuments().keySet(), not(hasItem(equalTo(name))));
    }

    private void doesNotHaveInvalid(DocumentUpload actual, String name) {
        assertThat(actual.getInvalid().keySet(), not(hasItem(equalTo(name))));
    }

    private void doesNotExists(DocumentUpload actual, String name) {
        doesNotHaveData(actual, name);
        doesNotHaveInvalid(actual, name);
    }
}