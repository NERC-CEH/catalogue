package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.xpath.XPathExpressionException;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class TerraCatalogExportImporterTest {
    @Mock DataRepository<CatalogueUser> repo;
    @Spy CatalogueUser automatedUser;
    //@Spy AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory;
    @Mock UserStore<CatalogueUser> userstore;
    @Mock TerraCatalogUserFactory userFactory;
    @Mock DocumentReadingService<GeminiDocument> documentReader;
    @Mock DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock TerraCatalogExtReader tcExtReader;
    @Mock TerraCatalogDocumentInfoFactory<MetadataInfo> terraCatalogDocumentInfoFactory;
    private TerraCatalogImporter<MetadataInfo, CatalogueUser> importer;
 
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    
    @Before
    public void createTerraCatalogExportImporter() throws IOException, XPathExpressionException {
        automatedUser = new CatalogueUser();
        automatedUser.setUsername("autobot");
        automatedUser.setEmail("autobot@ceh.ac.uk");
        
//        phantomUserBuilderFactory = new AnnotatedUserHelper<>(CatalogueUser.class);        
        
        
//        repo = new GitDataRepository(folder.newFolder("gitRepo"),
//                                     new InMemoryUserStore<>(),
//                                     phantomUserBuilderFactory,
//                                     new EventBus());
        MockitoAnnotations.initMocks(this);
                
        importer = new TerraCatalogImporter(repo, userFactory, documentReader, documentInfoMapper, terraCatalogDocumentInfoFactory, tcExtReader, automatedUser);
        
    }
    
    @Test
    public void checkThatImporterCanLocateFilePairToImport() throws IOException, UnknownContentTypeException {
        //Given        
        ZipEntry tcExtEntry = mock(ZipEntry.class);
        ZipEntry xmlEntry = mock(ZipEntry.class);
        
        when(tcExtEntry.getName()).thenReturn("1.tcext");
        when(xmlEntry.getName()).thenReturn("1.xml");
        
        ZipFile tcExport = mock(ZipFile.class);
        List zipEntries = Arrays.asList(tcExtEntry, xmlEntry);
        when(tcExport.stream()).thenReturn(zipEntries.stream());
        
        //When
        List<TerraCatalogImporter<MetadataInfo, CatalogueUser>.TerraCatalogPair> files = importer.getFiles(tcExport);
        
        //Then
        assertEquals("Expected a single terracatalogpair to have been located", 1, files.size());
    }
    
    @Test
    public void checkThatCanIdentifyFilesWhichAreInRepoButNotImport() throws DataRepositoryException {
        //Given
        TerraCatalogImporter.TerraCatalogPair pair = mock(TerraCatalogImporter.TerraCatalogPair.class);
        when(pair.getId()).thenReturn("486f7764-7943-6f64-6550-6172746e6572");
        
        when(repo.getFiles()).thenReturn(Arrays.asList( "486f7764-7943-6f64-6550-6172746e6572.raw",
                                                        "486f7764-7943-6f64-6550-6172746e6572.meta",
                                                        "4d6f6d65-6e74-6172-794c-617073654f66.raw",
                                                        "4d6f6d65-6e74-6172-794c-617073654f66.meta"));
        
        //When
        List<String> ids = importer.getFilesInRepositoryButNotInImport(Arrays.asList(pair));
        
        //Then
        assertEquals("Expected a single entry in the list", 1, ids.size());
        assertEquals("Expected to find the missing id", "4d6f6d65-6e74-6172-794c-617073654f66", ids.get(0));
    }
}
