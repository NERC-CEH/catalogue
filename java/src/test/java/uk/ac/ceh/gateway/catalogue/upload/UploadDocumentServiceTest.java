package uk.ac.ceh.gateway.catalogue.upload;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.mockito.MockitoAnnotations.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UploadDocumentServiceTest {
    private final File dataDirectory = new File("../file-test-data");
    private final File directory = new File("../file-test-data-test");

    private final File dropboxFolder = new File(directory, "upload-service/dropbox");
    private final File ploneFolder = new File(directory, "upload-service/plone");
    private final File datastoreFolder = new File(directory, "upload-service/eidchub-rw");

    private UploadDocumentService service;

    @Mock
    private DocumentRepository documentRepository;

    private UploadDocument uploadDocument;

    @Before
    @SneakyThrows
    public void beforeEach() {
        initMocks(this);
        
        val folders = new HashMap<String, File>();

        if (directory.exists()) FileUtils.forceDelete(directory);
        FileUtils.copyDirectory(dataDirectory, directory);

        folders.put("documents", dropboxFolder);
        folders.put("datastore", datastoreFolder);
        folders.put("plone", ploneFolder);
        
        service = new UploadDocumentService(documentRepository, folders);
        val geminiDocument = new GeminiDocument();
        geminiDocument.setId("guid");
        uploadDocument = service.create(CatalogueUser.PUBLIC_USER, geminiDocument);
        
        val metadata = MetadataInfo.builder().build();
        uploadDocument.setId("id");
        uploadDocument.setMetadata(metadata);
        doReturn(uploadDocument).when(documentRepository).read(anyString());
    }

    @Test
    public void create_addsTopLevelFile() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "upload-service/dropbox/guid/file.txt").getAbsolutePath())));
    }

    @Test
    public void create_addsFileInFolderUsingHashFromParentFolder() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "upload-service/dropbox/guid/folder/sub-a.txt").getAbsolutePath())));
    }

    @Test
    public void create_addsFileInFolderUsingHashFromContainingFolder() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "upload-service/dropbox/guid/folder/sub-b.txt").getAbsolutePath())));
    }

    @Test
    public void create_addsZipFiles() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "upload-service/dropbox/guid/zip.zip").getAbsolutePath())));
    }

    @Test
    public void create_addsFilesInZipFileUsingKeyword_extracted() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "upload-service/dropbox/guid/_extracted-zip/z.txt").getAbsolutePath())));
    }

    @Test
    public void create_addsSubZipFilesUsingKeyword_extracted() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "upload-service/dropbox/guid/_extracted-zip/sub-zip.zip").getAbsolutePath())));
    }

    @Test
    public void create_addsFilesFromSubZipFileUsingKeyword_extracted() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "upload-service/dropbox/guid/_extracted-zip/_extracted-sub-zip/sz.txt").getAbsolutePath())));
    }

    @Test
    public void create_emptyListOfNoFolder () {
        val datastoreDocuments = uploadDocument.getUploadFiles().get("datastore").getDocuments();
        assertThat(datastoreDocuments.size(), is(0));
        val datastoreInvalid = uploadDocument.getUploadFiles().get("datastore").getInvalid();
        assertThat(datastoreInvalid.size(), is(0));
        val ploneDocuments = uploadDocument.getUploadFiles().get("plone").getDocuments();
        assertThat(ploneDocuments.size(), is(0));
        val ploneInvalid = uploadDocument.getUploadFiles().get("plone").getInvalid();
        assertThat(ploneInvalid.size(), is(0));
    }

    @Test
    public void create_isNotZipped_unlessSeeNextText() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        assertThat(dropbox.isZipped(), is(false));
    }

    @Test
    public void create_isZipped_ifThereIsAZipFileAndAHashFileOnly() {
        val geminiDocument = new GeminiDocument();
        geminiDocument.setId("guid-zipped");

        val uploadDocument = service.create(CatalogueUser.PUBLIC_USER, geminiDocument);
        val dropbox = uploadDocument.getUploadFiles().get("documents");

        assertThat(dropbox.isZipped(), is(true));
    }

    @Test
    @SneakyThrows
    public void add_onlyAddsTheFileToDocuments () {
        val in = new FileInputStream(new File(directory, "upload-service/new-file.txt"));
        service.add(CatalogueUser.PUBLIC_USER, uploadDocument, "new-file.txt", in);

        val documents = uploadDocument.getUploadFiles().get("documents").getDocuments();

        assertThat(documents.keySet(), hasItem(is(new File(dropboxFolder, "guid/new-file.txt").getAbsolutePath())));
    }

    @Test
    @SneakyThrows
    public void add_getAllZipFilesFromAZip () {
        val in = new FileInputStream(new File(directory, "upload-service/zippy.zip"));
        service.add(CatalogueUser.PUBLIC_USER, uploadDocument, "zippy.zip", in);

        val documents = uploadDocument.getUploadFiles().get("documents").getDocuments();

        assertThat(documents.keySet(), hasItem(is(new File(dropboxFolder, "guid/zippy.zip").getAbsolutePath())));
        assertThat(documents.keySet(), hasItem(is(new File(dropboxFolder, "guid/_extracted-zippy/zip-1.txt").getAbsolutePath())));
        assertThat(documents.keySet(), hasItem(is(new File(dropboxFolder, "guid/_extracted-zippy/zip-2.txt").getAbsolutePath())));
    }
}