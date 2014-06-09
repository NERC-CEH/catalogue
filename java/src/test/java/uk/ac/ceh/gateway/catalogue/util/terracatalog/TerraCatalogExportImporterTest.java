package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.xpath.XPathExpressionException;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.TerraCatalogImporter.TerraCatalogPair;

/**
 *
 * @author cjohn
 */
public class TerraCatalogExportImporterTest {
    @Mock(answer=RETURNS_DEEP_STUBS) DataRepository<CatalogueUser> repo;
    @Spy CatalogueUser automatedUser;
    @Mock DocumentListingService listingService;
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
        
        MockitoAnnotations.initMocks(this);
                
        importer = new TerraCatalogImporter<>(repo, listingService, userFactory, documentReader, documentInfoMapper, terraCatalogDocumentInfoFactory, tcExtReader, automatedUser);
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
        
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList( "486f7764-7943-6f64-6550-6172746e6572",
                                                                           "4d6f6d65-6e74-6172-794c-617073654f66"));
        
        //When
        List<String> ids = importer.getFilesInRepositoryButNotInImport(Arrays.asList(pair));
        
        //Then
        assertEquals("Expected a single entry in the list", 1, ids.size());
        assertEquals("Expected to find the missing id", "4d6f6d65-6e74-6172-794c-617073654f66", ids.get(0));
    }
    
    @Test
    public void checkThatCanDeleteMultipleFiles() throws DataRepositoryException {
        //Given
        List<String> idsToDelete = Arrays.asList("id1", "id2");
        DataRevision revision = mock(DataRevision.class);
        
        when(repo.deleteData("id1.meta")
                 .deleteData("id1.raw")
                 .deleteData("id2.meta")
                 .deleteData("id2.raw")
                 .commit(eq(automatedUser), any(String.class))).thenReturn(revision);
        
        //When
        DataRevision<CatalogueUser> deleteFiles = importer.deleteFiles("FileImportingFrom", idsToDelete);
        
        //Then
        assertEquals("Expected the same revision from the importer", revision, deleteFiles);
    }
    
    @Test
    public void checkThatCanCommitListOfFiles() throws DataRepositoryException {
        //Given
        DataRevision revision = mock(DataRevision.class);
        CatalogueUser user = mock(CatalogueUser.class);
        
        TerraCatalogPair file1 = mock(TerraCatalogPair.class);
        TerraCatalogPair file2 = mock(TerraCatalogPair.class);
        when(file1.getId()).thenReturn("48657954-6865-7265-2d4d-722e55554944");
        when(file2.getId()).thenReturn("49734974-4e65-6172-486f-6d6554696d65");
        List<TerraCatalogImporter<MetadataInfo,CatalogueUser>.TerraCatalogPair> files = Arrays.asList(file1, file2);
        
        when(repo.submitData(eq("48657954-6865-7265-2d4d-722e55554944.meta"), any(DataWriter.class))
                 .submitData(eq("48657954-6865-7265-2d4d-722e55554944.raw"), any(DataWriter.class))
                 .submitData(eq("49734974-4e65-6172-486f-6d6554696d65.meta"), any(DataWriter.class))
                 .submitData(eq("49734974-4e65-6172-486f-6d6554696d65.raw"), any(DataWriter.class))
                 .commit(eq(user), any(String.class)))
            .thenReturn(revision);
        
        //When
        DataRevision<CatalogueUser> commitedRevision = importer.commitAuthorsFiles("FileImportingFrom", user, files);
        
        //Then
        assertEquals("Expected the mocked revision to be passed through", revision, commitedRevision);
    }
    
    @Test
    public void checkThatImportDoesntHappenIfNoFilesFound() throws IOException, UnknownContentTypeException {
        //Given
        ZipFile toImport = mock(ZipFile.class);
        
        TerraCatalogImporter importer = mock(TerraCatalogImporter.class);
        when(importer.getFiles(toImport)).thenReturn(Collections.EMPTY_LIST);
        doCallRealMethod().when(importer).importFile(toImport);
        
        //When
        importer.importFile(toImport);
        
        //Then
        verify(importer, never()).commitAuthorsFiles(any(String.class), any(CatalogueUser.class), any(List.class));
        verify(importer, never()).deleteFiles(any(String.class), any(List.class));
    }
    
    @Test
    public void checkThatFilesAreNotAttemptedToBeDeletedIfNonAreThereToDelete() throws IOException, UnknownContentTypeException {
        //Given
        ZipFile toImport = mock(ZipFile.class);
        
        CatalogueUser author = mock(CatalogueUser.class);
        TerraCatalogPair filePair = mock(TerraCatalogPair.class);
        when(filePair.getOwner()).thenReturn(author);
                
        List<TerraCatalogPair> filesToImport = Arrays.asList(filePair);
        TerraCatalogImporter importer = mock(TerraCatalogImporter.class);
        when(importer.getFiles(toImport)).thenReturn(filesToImport);
        when(importer.getFilesInRepositoryButNotInImport(filesToImport)).thenReturn(Collections.EMPTY_LIST);
        doCallRealMethod().when(importer).importFile(toImport);
        
        //When
        importer.importFile(toImport);
        
        //Then
        verify(importer, never()).deleteFiles(any(String.class), any(List.class));
        verify(importer).commitAuthorsFiles(any(String.class), any(CatalogueUser.class), eq(filesToImport));
    }
    
    @Test
    public void checkThatFilesAreDeletedIfThereAreSomeInTheRepoButNotTheImport() throws IOException, UnknownContentTypeException {
        //Given
        ZipFile toImport = mock(ZipFile.class);
        
        List<String> toDelete = Arrays.asList("toDelete");
        
        CatalogueUser author = mock(CatalogueUser.class);
        TerraCatalogPair filePair = mock(TerraCatalogPair.class);
        when(filePair.getOwner()).thenReturn(author);
                
        List<TerraCatalogPair> filesToImport = Arrays.asList(filePair);
        TerraCatalogImporter importer = mock(TerraCatalogImporter.class);
        when(importer.getFiles(toImport)).thenReturn(filesToImport);
        when(importer.getFilesInRepositoryButNotInImport(any(List.class))).thenReturn(toDelete);
        doCallRealMethod().when(importer).importFile(toImport);
        
        //When
        importer.importFile(toImport);
        
        //Then
        verify(importer).deleteFiles(any(String.class), any(List.class));
    }
    
    @Test
    public void checkThatFilesAreGroupedByAuthorsWhenCommiting() throws IOException, UnknownContentTypeException {
        //Given
        ZipFile toImport = mock(ZipFile.class);
        
        CatalogueUser author1 = mock(CatalogueUser.class);
        TerraCatalogPair filePair1 = mock(TerraCatalogPair.class);
        when(filePair1.getOwner()).thenReturn(author1);
        
        CatalogueUser author2 = mock(CatalogueUser.class);
        TerraCatalogPair filePair2 = mock(TerraCatalogPair.class);
        when(filePair2.getOwner()).thenReturn(author2);
        
        TerraCatalogPair filePair3 = mock(TerraCatalogPair.class);
        when(filePair3.getOwner()).thenReturn(author2);
                
        List<TerraCatalogPair> filesToImport = Arrays.asList(filePair1, filePair2, filePair3);
        TerraCatalogImporter importer = mock(TerraCatalogImporter.class);
        when(importer.getFiles(toImport)).thenReturn(filesToImport);
        when(importer.getFilesInRepositoryButNotInImport(filesToImport)).thenReturn(Collections.EMPTY_LIST);
        doCallRealMethod().when(importer).importFile(toImport);
        
        //When
        importer.importFile(toImport);
        
        //Then
        verify(importer, times(2)).commitAuthorsFiles(any(String.class), any(CatalogueUser.class), any(List.class));
    }
    
    @Test
    public void checkThatDirectoryImportIsDoneInCorrectOrder() throws IOException, UnknownContentTypeException {
        //Given
        File toImport = folder.newFolder();
        
        File firstFile = createZip(toImport, "BACKUP_TC_2013-03-25-13-14-30-951.zip");
        File secondFile = createZip(toImport, "BACKUP_TC_2014-05-22-15-02-06-454.zip");
                
        TerraCatalogImporter importer = mock(TerraCatalogImporter.class);
        doCallRealMethod().when(importer).importDirectory(toImport);
        doNothing().when(importer).importFile(any(ZipFile.class));
        
        //When
        importer.importDirectory(toImport);
        
        //Then
        ArgumentCaptor<ZipFile> imported = ArgumentCaptor.forClass(ZipFile.class);
        verify(importer, times(2)).importFile(imported.capture());
        
        assertEquals("Expected first file to be earliest", firstFile.getPath(), imported.getAllValues().get(0).getName());
        assertEquals("Expected srcond file to be latest", secondFile.getPath(), imported.getAllValues().get(1).getName());
    }
    
    private static File createZip(File directory, String name) throws IOException {
        File toReturn = new File(directory, name);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(toReturn));
        out.close();
        return toReturn;
    }
}
