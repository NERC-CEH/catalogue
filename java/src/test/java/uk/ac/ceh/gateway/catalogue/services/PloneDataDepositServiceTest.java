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
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

public class PloneDataDepositServiceTest {
    
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MetadataDocument document;
    
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
        FileUtils.write(file2, "something else");
        
        initMocks(this);

        doReturn(document).when(documentRepository).read(anyString());
        doReturn("Mock document title").when(document).getTitle();
        doReturn("type").when(document).getType();

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(getSecret("PLONE_USERNAME"), getSecret("PLONE_PASSWORD")));
        WebResource ploneClient = client.resource("http://localhost:8080/eidc/administration-folder/eidc-data-inventory/adddatadeposit");
        dus = new DocumentUploadService(dropbox, documentRepository);
        dus.add("guid","file1.txt",new FileInputStream(file1));
        dus.add("guid","file2.txt",new FileInputStream(file2));
        pdds = new PloneDataDepositService(ploneClient, dus);
    }
    
    @After
    @SneakyThrows
    public void after() {
        FileUtils.forceDelete(new File(dropbox, "guid"));
    }
    
    @Test
    public void testResponse() throws IOException, DocumentRepositoryException {
        pdds.processDataDeposit("guid");
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
