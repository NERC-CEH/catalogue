package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import com.google.common.eventbus.EventBus;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;
import javax.xml.xpath.XPathExpressionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Answers;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.converters.Xml2GeminiDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.MessageConverterReadingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class TerraCatalogExportImporterTest {
    @Spy DataRepository<CatalogueUser> repo;
    @Mock UserStore<CatalogueUser> userstore;
    @Spy CatalogueUser automatedUser;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentReadingService<GeminiDocument> documentReader;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentInfoMapper documentInfoMapper;
    private TerraCatalogImporter importer;
 
    
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    
    @Before
    public void createTerraCatalogExportImporter() throws IOException, XPathExpressionException {
        automatedUser = new CatalogueUser();
        automatedUser.setUsername("autobot");
        automatedUser.setEmail("autobot@ceh.ac.uk");
        
        AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory = new AnnotatedUserHelper<>(CatalogueUser.class);        
        
        repo = new GitDataRepository(new File("f:/gitRepo"),
                                     new InMemoryUserStore<>(),
                                     phantomUserBuilderFactory,
                                     new EventBus());
        MockitoAnnotations.initMocks(this);
        
        //Del me
        documentReader = new MessageConverterReadingService(GeminiDocument.class)
                .addMessageConverter(new Xml2GeminiDocumentMessageConverter());
                
        OfflineTerraCatalogUserFactory userFactory = new OfflineTerraCatalogUserFactory(phantomUserBuilderFactory);
        userFactory.put("CEH", "@ceh.ac.uk");
        userFactory.put("ceh", "@ceh.ac.uk");
        importer = new TerraCatalogImporter(repo, userFactory, documentReader, documentInfoMapper, automatedUser);
        
    }
    
    @Test
    public void loadInZip() throws IOException, UnknownUserException, UnknownContentTypeException {
        //Given
        when(userstore.getUser(any(String.class))).thenReturn(automatedUser);
        
        //When        
        importer.importDirectory(new File("E:\\repos\\cig\\terraCatalogExports"));
        
        //Then
        System.out.println("Test");
    }
}
