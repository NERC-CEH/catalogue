package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.File;
import java.util.Arrays;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

/**
 *
 * @author cjohn
 */
public class MapServerIndexingServiceTest {
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Mock BundledReaderService reader;
    @Mock DocumentListingService listingService;
    @Mock DataRepository repo;
    @Mock PostProcessingService postProcessingService;
    @Mock DocumentIdentifierService documentIdentifierService;
    @Mock IndexGenerator indexGenerator;
    
    private MapServerIndexingService service;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        
        service = new MapServerIndexingService(reader, listingService, repo, indexGenerator, folder.getRoot());
    }
    
    @Test
    public void checkThatCanClearOutDirectory() throws Exception {
        //Given
        folder.newFile("SomeFile.map");
        
        //When
        service.clearIndex();
        
        //Then
        assertTrue(service.isIndexEmpty());
    }
    
    @Test
    public void checkThatHavingMapFileMeansIndexIsNotEmpty() throws Exception {
        //Given
        folder.newFile("SomeFile.map");
        
        //When
        boolean isEmpty = service.isIndexEmpty();
        
        //Then
        assertFalse(isEmpty);
    }
    
    @Test
    public void checkThatCanUnIndexAFile() throws Exception {
        //Given
        folder.newFile("SomeFile.map");
        
        //When
        service.unindexDocuments(Arrays.asList("SomeFile"));
        
        //Then
        assertTrue(service.isIndexEmpty());
    }
    
    @Test
    public void checkThatCanIndexAMapFile() throws Exception {
        //Given
        MetadataDocument document = mock(MetadataDocument.class);
        when(document.getId()).thenReturn("document-id");
        MapFile mapFile = mock(MapFile.class);
        when(mapFile.getDocument()).thenReturn(document);
        
        //When
        service.index(mapFile);
        
        //Then
        assertFalse(service.isIndexEmpty());
        assertTrue(new File(folder.getRoot(), "document-id.map").exists());
    }
}