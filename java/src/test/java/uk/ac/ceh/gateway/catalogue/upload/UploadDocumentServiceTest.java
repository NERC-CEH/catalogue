package uk.ac.ceh.gateway.catalogue.upload;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.File;
import java.util.HashMap;
import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UploadDocumentServiceTest {
    private final File dataDirectory = new File("../file-test-data");
    private final File directory = new File("../file-test-data-test");

    private final File dropboxFolder = new File(directory, "dropbox");
    private final File ploneFolder = new File(directory, "plone");
    private final File datastoreFolder = new File(directory, "eidchub-rw");

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
    }

    @Test
    public void create_addsTopLevelFile() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "dropbox/guid/file.txt").getAbsolutePath())));
    }

    @Test
    public void create_addsFileInFolderUsingHashFromParentFolder() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "dropbox/guid/folder/sub-a.txt").getAbsolutePath())));
    }

    @Test
    public void create_addsFileInFolderUsingHashFromContainingFolder() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "dropbox/guid/folder/sub-b.txt").getAbsolutePath())));
    }

    @Test
    public void create_addsZipFiles() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "dropbox/guid/zip.zip").getAbsolutePath())));
    }

    @Test
    public void create_addsFilesInZipFileUsingKeyword_extracted() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "dropbox/guid/_extracted-zip/z.txt").getAbsolutePath())));
    }

    @Test
    public void create_addsSubZipFilesUsingKeyword_extracted() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "dropbox/guid/_extracted-zip/sub-zip.zip").getAbsolutePath())));
    }

    @Test
    public void create_addsFilesFromSubZipFileUsingKeyword_extracted() {
        val dropbox = uploadDocument.getUploadFiles().get("documents");
        val dropboxDocuments = dropbox.getDocuments();
        assertThat(dropboxDocuments.keySet(), hasItem(is(new File(directory, "dropbox/guid/_extracted-zip/_extracted-sub-zip/sz.txt").getAbsolutePath())));
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
}