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
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/file.txt");
    }

    @Test
    public void create_addsFileInFolderUsingHashFromParentFolder() {
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/folder/sub-a.txt");
    }

    @Test
    public void create_addsFileInFolderUsingHashFromContainingFolder() {
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/folder/sub-b.txt");
    }

    @Test
    public void create_addsZipFilesAndAllZipFiles() {
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/zip.zip");
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/_extracted-zip/z.txt");
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/_extracted-zip/sub-zip.zip");
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/_extracted-zip/_extracted-sub-zip/sz.txt");
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

        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/new-file.txt");
    }

    @Test
    @SneakyThrows
    public void add_getAllZipFilesFromAZip () {
        val in = new FileInputStream(new File(directory, "upload-service/zippy.zip"));
        service.add(CatalogueUser.PUBLIC_USER, uploadDocument, "zippy.zip", in);

        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/zippy.zip");
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/_extracted-zippy/zip-1.txt");
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/_extracted-zippy/zip-2.txt");
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/_extracted-zippy/sub-zippy.zip");
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/_extracted-zippy/_extracted-sub-zippy/zip-1.txt");
        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/_extracted-zippy/_extracted-sub-zippy/zip-2.txt");
    }

    @Test
    public void move_fromOnePlaceToAnother () {
        val file = new File(dropboxFolder, "guid/file.txt");
        service.move(CatalogueUser.PUBLIC_USER, uploadDocument, "documents", "datastore", file.getAbsolutePath());

        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/file.txt");
        assertThatDocumentsHasFile("datastore", datastoreFolder, "guid/file.txt");
    }

    @Test
    public void move_zipFromOnePlaceToAnotherTakesAllFilesWithIt () {
        val file = new File(dropboxFolder, "guid/zip.zip");
        service.move(CatalogueUser.PUBLIC_USER, uploadDocument, "documents", "datastore", file.getAbsolutePath());

        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/zip.zip");
        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/_extracted-zip/z.txt");
        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/_extracted-zip/sub-zip.zip");
        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/_extracted-zip/_extracted-sub-zip/sz.txt");

        assertThatDocumentsHasFile("datastore", datastoreFolder, "guid/zip.zip");
        assertThatDocumentsHasFile("datastore", datastoreFolder, "guid/_extracted-zip/z.txt");
        assertThatDocumentsHasFile("datastore", datastoreFolder, "guid/_extracted-zip/sub-zip.zip");
        assertThatDocumentsHasFile("datastore", datastoreFolder, "guid/_extracted-zip/_extracted-sub-zip/sz.txt");
    }

    @Test
    @SneakyThrows
    public void delete_onlyAddsTheFileToDocuments () {
        val file = new File(dropboxFolder, "guid/file.txt");
        service.delete(CatalogueUser.PUBLIC_USER, uploadDocument, "documents", file.getAbsolutePath());

        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/file.txt");
    }

    @Test
    @SneakyThrows
    public void delete_allZipFilesFromAZip () {
        val file = new File(dropboxFolder, "guid/zip.zip");
        service.delete(CatalogueUser.PUBLIC_USER, uploadDocument, "documents", file.getAbsolutePath());

        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/zip.zip");
        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/_extracted-zip/z.txt");
        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/_extracted-zip/sub-zip.zip");
        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/_extracted-zip/_extracted-sub-zip/sz.txt");
    }

    @Test
    @SneakyThrows
    public void delete_invalidFile () {
        val file = new File(dropboxFolder, "guid/unknown.txt");
        service.delete(CatalogueUser.PUBLIC_USER, uploadDocument, "documents", file.getAbsolutePath());

        assertThatDocumentsDoesNotHaveFile("documents", dropboxFolder, "guid/unknown.txt");
        assertThatInvalidDoesNotHaveFile("documents", dropboxFolder, "guid/unknown.txt");
    }

    @Test
    @SneakyThrows
    public void acceptInvalid () {
        val file = new File(dropboxFolder, "guid/unknown.txt");
        service.acceptInvalid(CatalogueUser.PUBLIC_USER, uploadDocument, "documents", file.getAbsolutePath());

        assertThatDocumentsHasFile("documents", dropboxFolder, "guid/unknown.txt");
        assertThatInvalidDoesNotHaveFile("documents", dropboxFolder, "guid/unknown.txt");
    }

    @Test
    @SneakyThrows
    public void zip () {
        val file = new File(dropboxFolder, "guid/zip.zip");
        service.move(CatalogueUser.PUBLIC_USER, uploadDocument, "documents", "datastore", file.getAbsolutePath());

        service.zip(CatalogueUser.PUBLIC_USER, uploadDocument);

        val datastore = uploadDocument.getUploadFiles().get("datastore");
        assertTrue("should be zipped", datastore.isZipped());
        assertThatDocumentsHasFile("datastore", datastoreFolder, "guid/guid.zip");
    }

    @Test
    @SneakyThrows
    public void zip_doesNothingIfAlreadyZipped () {
        val file = new File(dropboxFolder, "guid/zip.zip");
        service.move(CatalogueUser.PUBLIC_USER, uploadDocument, "documents", "datastore", file.getAbsolutePath());

        service.zip(CatalogueUser.PUBLIC_USER, uploadDocument);
        service.zip(CatalogueUser.PUBLIC_USER, uploadDocument);

        val datastore = uploadDocument.getUploadFiles().get("datastore");
        assertTrue("should be zipped", datastore.isZipped());
        assertThatDocumentsHasFile("datastore", datastoreFolder, "guid/guid.zip");
    }

    @Test
    @SneakyThrows
    public void unzip () {
        val file = new File(dropboxFolder, "guid/zip.zip");
        service.move(CatalogueUser.PUBLIC_USER, uploadDocument, "documents", "datastore", file.getAbsolutePath());

        service.zip(CatalogueUser.PUBLIC_USER, uploadDocument);
        service.unzip(CatalogueUser.PUBLIC_USER, uploadDocument);

        val datastore = uploadDocument.getUploadFiles().get("datastore");
        assertFalse("should not be zipped", datastore.isZipped());
        assertThatDocumentsDoesNotHaveFile("datastore", datastoreFolder, "guid/guid.zip");
    }

    @Test
    @SneakyThrows
    public void unzip_doesNothingIfNotCorrectlyZipped () {
        val file = new File(dropboxFolder, "guid/zip.zip");
        service.move(CatalogueUser.PUBLIC_USER, uploadDocument, "documents", "datastore", file.getAbsolutePath());
        
        service.unzip(CatalogueUser.PUBLIC_USER, uploadDocument);

        val datastore = uploadDocument.getUploadFiles().get("datastore");
        assertFalse("should not be zipped", datastore.isZipped());
        assertThatDocumentsDoesNotHaveFile("datastore", datastoreFolder, "guid/guid.zip");
    }

    private void assertThatDocumentsDoesNotHaveFile(String name, File folder, String filename) {
        val documents = uploadDocument.getUploadFiles().get(name).getDocuments();
        assertThat(documents.keySet(), not(hasItem(is(new File(folder, filename).getAbsolutePath()))));
    }

    private void assertThatDocumentsHasFile(String name, File folder, String filename) {
        val documents = uploadDocument.getUploadFiles().get(name).getDocuments();
        assertThat(documents.keySet(), hasItem(is(new File(folder, filename).getAbsolutePath())));
    }

    private void assertThatInvalidDoesNotHaveFile(String name, File folder, String filename) {
        val documents = uploadDocument.getUploadFiles().get(name).getInvalid();
        assertThat(documents.keySet(), not(hasItem(is(new File(folder, filename).getAbsolutePath()))));
    }

    private void assertThatInvalidHasFile(String name, File folder, String filename) {
        val documents = uploadDocument.getUploadFiles().get(name).getInvalid();
        assertThat(documents.keySet(), hasItem(is(new File(folder, filename).getAbsolutePath())));
    }
}