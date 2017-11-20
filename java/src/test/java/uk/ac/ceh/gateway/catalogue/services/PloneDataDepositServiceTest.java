package uk.ac.ceh.gateway.catalogue.services;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

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
    private MetadataDocument document;
    
    @Mock
    private DocumentUpload documentUpload;
    
    private PloneDataDepositService pdds;
    private DocumentUploadService documentsUploadService;
    private DocumentUploadService dataUploadService;
    private static final File documents = new File("../dropbox");
    private static final File data = new File("../data");

    @Before
    @SneakyThrows
    public void before() {
        FileUtils.forceMkdir(documents);
        FileUtils.forceMkdir(data);
        
        initMocks(this);

        doReturn(document).when(documentRepository).read(anyString());
        doReturn("Demonstration upload").when(document).getTitle();
        doReturn("type").when(document).getType();

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(getSecret("PLONE_USERNAME"), getSecret("PLONE_PASSWORD")));
        WebResource ploneClient = client.resource(getSecret("PLONE_ADDRESS"));
        documentsUploadService = new DocumentUploadService(documents, documentRepository);
        dataUploadService = new DocumentUploadService(data, documentRepository);
        pdds = new PloneDataDepositService(ploneClient);
    }
    
    @After
    @SneakyThrows
    public void after() {
        FileUtils.forceDelete(new File(documents, "ploneTestGuid"));
        FileUtils.forceDelete(new File(data, "ploneTestGuid"));
    }

    /*
    Add 6 files to dropbox location and update Plone
    */
    @Ignore
    @Test
    @SneakyThrows
    public void testCreateDataDeposit() {
        for (int i = 0; i < 6; i++) {
            File file = new File(documents, String.format("test_file_%s.txt", i));
            FileUtils.write(file, String.format("something %s", i));
            documentsUploadService.add("ploneTestGuid", String.format("file%s.txt", i), new FileInputStream(file));
        }
        String actual = pdds.addOrUpdate(documentsUploadService.get("ploneTestGuid"), dataUploadService.get("ploneTestGuid"));
        assertTrue(actual.contains("Success"));
    }

    /*
    Move 3 files to documents location
    */
    @Ignore
    @Test
    @SneakyThrows
    public void testCreateMoveSomeFilesToData() {
        //Add the same files as in a, but put 3 in the Data folder
        for (int i = 0; i < 3; i++) {
            File file = new File(documents, String.format("test_file_%s.txt", i));
            FileUtils.write(file, String.format("something %s", i));
            documentsUploadService.add("ploneTestGuid", String.format("file%s.txt", i), new FileInputStream(file));
        }
        for (int i = 3; i < 6; i++) {
            File file = new File(documents, String.format("test_file_%s.txt", i));
            FileUtils.write(file, String.format("something %s", i));
            dataUploadService.add("ploneTestGuid", String.format("file%s.txt", i), new FileInputStream(file));
        }
        String actual = pdds.addOrUpdate(documentsUploadService.get("ploneTestGuid"), dataUploadService.get("ploneTestGuid"));
        assertTrue(actual.contains("Success"));
    }

    /*
    When files are moved to the plone metadata store they simply cease to exist in the Documents and Data upload services.
    So just have 2 files per list and check that the data deposit in Plone shows just those files.
    You should see the data deposit has 4 files, where file0.txt and file1.txt have physical location = <documentLocation>, and file3.txt and file4.txt have physical location = <dataLocation>.  There should no longer be file3.txt or file5.txt
    */
    @Test
    @SneakyThrows
    public void testCreateMoveSomeFilesToPlone() {

        for (int i = 0; i < 2; i++) {
            File file = new File(documents, String.format("test_file_%s.txt", i));
            FileUtils.write(file, String.format("something %s", i));
            documentsUploadService.add("ploneTestGuid", String.format("file%s.txt", i), new FileInputStream(file));
        }
        for (int i = 3; i < 5; i++) {
            File file = new File(documents, String.format("test_file_%s.txt", i));
            FileUtils.write(file, String.format("something %s", i));
            dataUploadService.add("ploneTestGuid", String.format("file%s.txt", i), new FileInputStream(file));
        }
        String actual = pdds.addOrUpdate(documentsUploadService.get("ploneTestGuid"), dataUploadService.get("ploneTestGuid"));
        assertTrue(actual.contains("Success"));
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
