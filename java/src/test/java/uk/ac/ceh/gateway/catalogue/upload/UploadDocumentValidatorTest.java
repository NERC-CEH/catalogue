package uk.ac.ceh.gateway.catalogue.upload;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.File;
import java.nio.charset.Charset;
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
public class UploadDocumentValidatorTest {
    private final File dataDirectory = new File("../file-test-data");
    private final File directory = new File("../file-test-data-test");

    private final File dropboxFolder = new File(directory, "dropbox");
    private final File ploneFolder = new File(directory, "plone");
    private final File datastoreFolder = new File(directory, "eidchub-rw");

    private final File manuallyAdded = new File(dropboxFolder, "guid/added_manually.txt");
    private final File changed = new File(dropboxFolder, "guid/file.txt");
    private final File removed = new File(dropboxFolder, "guid/folder/sub-a.txt");

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

        FileUtils.write(manuallyAdded, "added manually", Charset.defaultCharset());
        FileUtils.write(changed, "changed the contents of known file", Charset.defaultCharset());
        FileUtils.forceDelete(removed);

        UploadDocumentValidator.validate(uploadDocument);
    }

    @Test
    public void ignoreGuidZipFile () {
        val geminiDocument = new GeminiDocument();
        geminiDocument.setId("guid-changed-zipped");
        uploadDocument = service.create(CatalogueUser.PUBLIC_USER, geminiDocument);
        UploadDocumentValidator.validate(uploadDocument);
        assertThat(uploadDocument.getUploadFiles().get("documents").getInvalid().size(), is(0));
        assertThat(uploadDocument.getUploadFiles().get("datastore").getInvalid().size(), is(0));
        assertThat(uploadDocument.getUploadFiles().get("plone").getInvalid().size(), is(0));
    }

    @Test
    public void validate_addsUknownFiles() {
        val uploadFile = uploadDocument.getUploadFiles().get("documents").getInvalid().get(manuallyAdded.getAbsolutePath());

        assertThat(uploadFile.getType(), is(UploadType.UNKNOWN_FILE));
    }

    @Test
    public void validate_addMissingFiles() {
        val uploadFile = uploadDocument.getUploadFiles().get("documents").getInvalid().get(removed.getAbsolutePath());
        val documents = uploadDocument.getUploadFiles().get("documents").getDocuments().keySet();

        assertThat(uploadFile.getType(), is(UploadType.MISSING_FILE));
        assertThat(documents, not(hasItem(is(removed.getAbsolutePath()))));
    }

    @Test
    public void validate_invalidHashes() {
        val uploadFile = uploadDocument.getUploadFiles().get("documents").getInvalid().get(changed.getAbsolutePath());
        val documents = uploadDocument.getUploadFiles().get("documents").getDocuments().keySet();

        assertThat(uploadFile.getType(), is(UploadType.INVALID_HASH));
        assertThat(documents, not(hasItem(is(changed.getAbsolutePath()))));
    }
}


