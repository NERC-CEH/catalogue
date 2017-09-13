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
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.After;
import static org.junit.Assert.assertThat;
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
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

public class PloneDataDepositServiceTest {
    
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MetadataDocument document;
    
    @Mock
    private DocumentUpload documentUpload;
    
    private PloneDataDepositService pdds;
    private DocumentUploadService dus;
    private static final File dropbox = new File("../dropbox");
    private static final File file1 = new File(dropbox, "file1.txt");
    private static final File file2 = new File(dropbox, "file2.txt");

    @Before
    @SneakyThrows
    public void before() {
        FileUtils.forceMkdir(dropbox);
        FileUtils.write(file1, "something");
        FileUtils.write(file2, "something else more again");
        
        initMocks(this);

        doReturn(document).when(documentRepository).read(anyString());
        doReturn("Demonstration upload").when(document).getTitle();
        doReturn("type").when(document).getType();

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(getSecret("PLONE_USERNAME"), getSecret("PLONE_PASSWORD")));
        System.out.println(getSecret("PLONE_ADDRESS"));
        WebResource ploneClient = client.resource(getSecret("PLONE_ADDRESS"));
        dus = new DocumentUploadService(dropbox, documentRepository);
        dus.add("guid","file1.txt",new FileInputStream(file1));
        dus.add("guid","file3.txt",new FileInputStream(file2));
        pdds = new PloneDataDepositService(ploneClient);
    }
    
    @After
    @SneakyThrows
    public void after() {
        FileUtils.forceDelete(new File(dropbox, "guid"));
    }
    
    @Ignore
    /*
    This test is ignored because it is just useful utility used during plone development to send requests the plone service
    */
    @Test
    public void testResponse() throws IOException, DocumentRepositoryException {
        String actual = pdds.addOrUpdate(dus.get("guid"));
        String expected = "Success - updated: guid";
        assertThat(actual, equalTo(expected));
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
