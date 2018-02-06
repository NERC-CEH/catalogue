package uk.ac.ceh.gateway.catalogue.services;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.upload.UploadDocument;
import uk.ac.ceh.gateway.catalogue.upload.UploadDocumentService;

import static org.mockito.Matchers.anyString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

/*
Use these tests to check the plone updates when you have a locally running instance of Plone. Instructions on getting a local plone running are here: https://jira.ceh.ac.uk/browse/CEHCIG-790

All tests are Ignored since this is only used when there is a locally running instance of Plone - just used to do semi-automated testing really, very convenient for now.

Un-ignore just one test at a time and run it using:
./gradlew -Dtest.single=PloneDataDepositServiceTest test

FYI, you can get System.out.println output using --debug, which you can then grep for:
./gradlew -Dtest.single=PloneDataDepositServiceTest test --debug | grep mydebug

OK - follow these steps to check all is working as expected:

1.  Ensure Plone is running locally (port 8080)
2.  Un-ignore only testCreateDataDeposit and run (using ./gradlew as mentioned), manually check Plone for the Data Deposit, which will have 6 files all with dropbox location
3.  Having done step 2, now un-ignore ONLY testCreateMoveSomeFilesToData and run.  Check Plone to ensure files 3 to 5 have an eidchub location and their history has been updated, and files 0 to 2 haven't changed.
4.  Having done step 3 (or just step 1), now un-ignore ONLY testCreateMoveSomeFilesToPlone.  Check that files 3 and 6 have gone from the Data Deposit, that other files haven't changed and the Data Desposit's history has been updated.

 */
public class PloneDataDepositServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private GeminiDocument document;

    private final File dataDirectory = new File("../file-test-data");
    private final File directory = new File("../file-test-data-test");

    private final File dropboxFolder = new File(directory, "plone-data-deposit-service/dropbox");
    private final File ploneFolder = new File(directory, "plone-data-deposit-service/plone");
    private final File datastoreFolder = new File(directory, "plone-data-deposit-service/eidchub-rw");

    private final CatalogueUser user = CatalogueUser.PUBLIC_USER;

    private PloneDataDepositService pdds;
    private UploadDocument uploadDocument;
    private UploadDocumentService uploadDocumentService;

    @Before
    @SneakyThrows
    public void before() {
        initMocks(this);

        if (directory.exists()) FileUtils.forceDelete(directory);
        FileUtils.copyDirectory(dataDirectory, directory);

        val folders = new HashMap<String, File>();

        if (directory.exists()) FileUtils.forceDelete(directory);
        FileUtils.copyDirectory(dataDirectory, directory);

        folders.put("documents", dropboxFolder);
        folders.put("datastore", datastoreFolder);
        folders.put("plone", ploneFolder);

        uploadDocumentService = new UploadDocumentService(documentRepository, folders);
        
        doReturn(document).when(documentRepository).read(anyString());
        doReturn("Demonstration upload").when(document).getTitle();
        doReturn("guid").when(document).getId();
        doReturn("type").when(document).getType();

        val metadataInfo = MetadataInfo.builder().build();
        doReturn(metadataInfo).when(document).getMetadata();

        uploadDocument = uploadDocumentService.create(user, document);
        uploadDocument.setId("id");

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("admin", getSecret("plone.password")));
        WebResource ploneClient = client.resource("http://eidctest.ceh.ac.uk/administration-folder/eidc-data-inventory/adddatadeposit");

        pdds = new PloneDataDepositService(ploneClient);
    }

    /*
    Add 6 files to dropbox location and update Plone
    And 1 invalid file, you won't see this in plone
    */
    @Ignore
    @Test
    @SneakyThrows
    public void testCreateDataDeposit() {
        String actual = pdds.addOrUpdate(uploadDocument);
        assertTrue(actual.contains("Success"));
    }

    /*
    Move 3 files to documents location
    */
    @Ignore
    @Test
    @SneakyThrows
    public void testCreateMoveSomeFilesToData() {
        move("documents", "datastore", "test_file_0.txt");
        move("documents", "datastore", "test_file_1.txt");
        move("documents", "datastore", "test_file_2.txt");
        String actual = pdds.addOrUpdate(uploadDocument);
        assertTrue(actual.contains("Success"));
    }

    /*
    When files are moved to the plone metadata store they simply cease to exist in the Documents and Data upload services.
    So just have 2 files per list and check that the data deposit in Plone shows just those files.
    You should see the data deposit has 4 files, where file0.txt and file1.txt have physical location = <documentLocation>,
        and file3.txt and file4.txt have physical location = <dataLocation>.  There should no longer be file3.txt or file5.txt
    */
    @Ignore
    @Test
    @SneakyThrows
    public void testCreateMoveSomeFilesToPlone() {
        move("documents", "datastore", "test_file_0.txt");
        move("documents", "datastore", "test_file_1.txt");
        move("documents", "datastore", "test_file_2.txt");
        move("documents", "plone", "test_file_3.txt");
        move("documents", "datastore", "test_file_4.txt");
        move("documents", "plone", "test_file_5.txt");
        String actual = pdds.addOrUpdate(uploadDocument);
        assertTrue(actual.contains("Success"));
    }

    private void move(String from, String to, String filename) {
        uploadDocumentService.move(user, uploadDocument, from, to, getDropboxFilename(filename));
    }

    private String getDropboxFilename(String name) {
        return new File(dropboxFolder, String.format("guid/%s", name)).getAbsolutePath();
    }

    // Get a secret from secrets.env
    private String getSecret(String secret) throws IOException{
        try(Stream<String> stream = Files.lines(Paths.get("../secrets.env"))){
            String[] toReturn = stream
                .filter(k -> secret.equals(k.split("=")[0]))
                .map(k -> k.split("=")[1])
                .toArray(String[]::new);
            return toReturn[0];
        }
    }
}