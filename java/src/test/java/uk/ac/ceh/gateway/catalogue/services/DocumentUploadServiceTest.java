package uk.ac.ceh.gateway.catalogue.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.model.DocumentUploadFile;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

@RunWith(MockitoJUnitRunner.class)
public class DocumentUploadServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MetadataDocument document;

    private static final File dropbox = new File("../dropbox");
    private static final File file = new File(dropbox, "file.txt");
    private static final File invalid = new File(dropbox, "guid/invalid.txt");

    private DocumentUploadService dus;

    @Before
    @SneakyThrows
    public void before() {
        FileUtils.forceMkdir(dropbox);
        FileUtils.write(file, "something");
        FileUtils.write(invalid, "invalid content");

        initMocks(this);

        doReturn(document).when(documentRepository).read(anyString());
        doReturn("title").when(document).getTitle();
        doReturn("type").when(document).getType();

        dus = new DocumentUploadService(dropbox, documentRepository);
    }

    @After
    @SneakyThrows
    public void after() {
        FileUtils.forceDelete(new File(dropbox, "guid"));
        FileUtils.forceDelete(file);
    }

    @Test
    @SneakyThrows
    public void invalidFile_ifNotInDataOrMetaButInFolder() {
        val actual = dus.get("guid");
        
        assertThat(actual.getInvalid().get("invalid.txt").getType(), equalTo(DocumentUpload.Type.NOT_META_OR_DATA.name()));
        assertThat(actual.getInvalid().size(), equalTo(1));
        assertThat(actual.getData().size(), equalTo(0));
        assertThat(actual.getMeta().size(), equalTo(0));
    }

    @Test
    @SneakyThrows
    public void invalidFile_ifInBothDataOrMeta() {
        dus.add("guid", "invalid.txt", new FileInputStream(invalid));
        val documentUpload = dus.get("guid");
        DocumentUploadFile documentUploadFile = documentUpload.getData().get("invalid.txt");
        documentUpload.getMeta().put("invalid.txt", documentUploadFile);
        val file = new File(documentUpload.getPath(), "_data.json");
        val mapper = new ObjectMapper();
        mapper.writeValue(file, documentUpload);

        val actual = dus.get("guid");
        assertThat(actual.getInvalid().get("invalid.txt").getType(), equalTo(DocumentUpload.Type.BOTH_META_AND_DATA.name()));
        assertThat(actual.getInvalid().size(), equalTo(1));
        assertThat(actual.getData().size(), equalTo(0));
        assertThat(actual.getMeta().size(), equalTo(0));
    }

    @Test
    @SneakyThrows
    public void invalidFile_ifFileHasChanged() {
        dus.add("guid", "invalid.txt", new FileInputStream(invalid));
        dus.changeFileType("guid", "invalid.txt", DocumentUpload.Type.META);

        FileUtils.write(invalid, "invalid content again");

        val actual = dus.get("guid");
        assertThat(actual.getInvalid().get("invalid.txt").getType(), equalTo(DocumentUpload.Type.INVALID_HASH.name()));
        assertThat(actual.getInvalid().size(), equalTo(1));
        assertThat(actual.getData().size(), equalTo(0));
        assertThat(actual.getMeta().size(), equalTo(0));
    }

    @Test
    @SneakyThrows
    public void invalidFile_ifFileDoesNotExistInMeta() {
        dus.add("guid", "invalid.txt", new FileInputStream(invalid));
        dus.changeFileType("guid", "invalid.txt", DocumentUpload.Type.META);

        FileUtils.forceDelete(invalid);

        val actual = dus.get("guid");
        assertThat(actual.getInvalid().get("invalid.txt").getType(), equalTo(DocumentUpload.Type.FILE_DOES_NOT_EXISTS.name()));
        assertThat(actual.getInvalid().size(), equalTo(1));
        assertThat(actual.getData().size(), equalTo(0));
        assertThat(actual.getMeta().size(), equalTo(0));
    }

    @Test
    @SneakyThrows
    public void invalidFile_ifFileDoesNotExistInData() {
        dus.add("guid", "invalid.txt", new FileInputStream(invalid));

        FileUtils.forceDelete(invalid);

        val actual = dus.get("guid");
        assertThat(actual.getInvalid().get("invalid.txt").getType(), equalTo(DocumentUpload.Type.FILE_DOES_NOT_EXISTS.name()));
        assertThat(actual.getInvalid().size(), equalTo(1));
        assertThat(actual.getData().size(), equalTo(0));
        assertThat(actual.getMeta().size(), equalTo(0));
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

        assertThat(actual.getPath(), equalTo(new File(dropbox, "guid").getAbsolutePath()));
    }

    @Test
    @SneakyThrows
    public void delete_doesNothingIfNothingHasBeenAdded() {
        dus.delete("guid", "file.txt");

        val actual = dus.get("guid");
        assertThat(actual.getData().size(), equalTo(0));
        assertThat(actual.getMeta().size(), equalTo(0));
    }

    @Test
    @SneakyThrows
    public void delete_removesAddedFile() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        dus.delete("guid", "file.txt");

        val actual = dus.get("guid");
        assertThat(actual.getData().size(), equalTo(0));
    }

    @Test
    @SneakyThrows
    public void delete_removeMetaFile() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();
        dus.changeFileType("guid", "file.txt", DocumentUpload.Type.META);

        dus.delete("guid", "file.txt");

        val actual = dus.get("guid");
        assertThat(actual.getMeta().size(), equalTo(0));
    }

    @Test
    @SneakyThrows
    public void delete_removeTheFileFromDisk() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();
        dus.delete("guid", "file.txt");
        assertThat("file exists", !new File(dropbox, "guid/file.txt").exists());
    }

    @Test
    @SneakyThrows
    public void add_addsToData() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        val actual = dus.get("guid");
        assertThat(actual.getData().size(), equalTo(1));
    }

    @Test
    @SneakyThrows
    public void add_saveFileToDisk() {
        assertThat("file exists", !new File(dropbox, "guid/file.txt").exists());
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();
        assertThat("file does not exists", new File(dropbox, "guid/file.txt").exists());
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasBytes() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        val actual = dus.get("guid");
        val documentUploadFile = actual.getData().get("file.txt");
        assertThat(documentUploadFile.getBytes(), equalTo(9L));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasEncodingUtf8() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        val actual = dus.get("guid");
        val documentUploadFile = actual.getData().get("file.txt");
        assertThat(documentUploadFile.getEncoding(), equalTo("utf-8"));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasFormat() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        val actual = dus.get("guid");
        val documentUploadFile = actual.getData().get("file.txt");
        assertThat(documentUploadFile.getFormat(), equalTo("txt"));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasHash() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        val actual = dus.get("guid");
        val documentUploadFile = actual.getData().get("file.txt");
        assertThat(documentUploadFile.getHash(), equalTo("437b930db84b8079c2dd804a71936b5f"));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasMediatype() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        val actual = dus.get("guid");
        val documentUploadFile = actual.getData().get("file.txt");
        assertThat(documentUploadFile.getMediatype(), equalTo("text/plain"));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasName() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        val actual = dus.get("guid");
        val documentUploadFile = actual.getData().get("file.txt");
        assertThat(documentUploadFile.getName(), equalTo("file.txt"));
    }

    @Test
    @SneakyThrows
    public void add_documentUploadFile_hasAbsolutePath() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        val actual = dus.get("guid");
        val documentUploadFile = actual.getData().get("file.txt");
        assertThat(documentUploadFile.getPath(), equalTo(new File(dropbox, "guid/file.txt").getAbsolutePath()));
    }

    @Test
    @SneakyThrows
    public void add_onlyAddsOnce() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();
        in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        val actual = dus.get("guid");
        assertThat(actual.getData().size(), equalTo(1));
    }

    @Test
    @SneakyThrows
    public void changeType_canChangeFromDataToMeta() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        dus.changeFileType("guid", "file.txt", DocumentUpload.Type.META);

        val actual = dus.get("guid");
        assertThat(actual.getData().size(), equalTo(0));
        assertThat(actual.getMeta().size(), equalTo(1));
    }

    @Test
    @SneakyThrows
    public void changeType_canChangeFromMetaToData() {
        InputStream in = new FileInputStream(file);
        dus.add("guid", "file.txt", in);
        in.close();

        dus.changeFileType("guid", "file.txt", DocumentUpload.Type.META);
        dus.changeFileType("guid", "file.txt", DocumentUpload.Type.DATA);

        val actual = dus.get("guid");
        assertThat(actual.getData().size(), equalTo(1));
        assertThat(actual.getMeta().size(), equalTo(0));
    }

    @Test
    @SneakyThrows
    public void changeType_doesNothingIfNoMatchingFile() {
        dus.changeFileType("guid", "file.txt", DocumentUpload.Type.META);
        dus.changeFileType("guid", "file.txt", DocumentUpload.Type.DATA);

        val actual = dus.get("guid");
        assertThat(actual.getData().size(), equalTo(0));
        assertThat(actual.getMeta().size(), equalTo(0));
    }
}