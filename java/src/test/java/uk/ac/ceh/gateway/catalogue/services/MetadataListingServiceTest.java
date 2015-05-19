package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;

/**
 *
 * @author cjohn
 */
public class MetadataListingServiceTest {
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DataRepository<CatalogueUser> repo;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentListingService listingService;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) BundledReaderService<MetadataDocument> documentBundleReader;
    private MetadataListingService service;
    
    
    @Before
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        service = new MetadataListingService(repo,
                                            listingService,
                                            documentBundleReader);
    }
    
    
    @Test
    public void checkThatReadsDocumentsListFromDataRepositiory() throws IOException, UnknownContentTypeException {
        //Given
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList("uid"));
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn("uid");
        when(document.getType()).thenReturn("Dataset");
        MetadataInfo metadata = mock(MetadataInfo.class);
        when(metadata.isPubliclyViewable(Permission.VIEW)).thenReturn(Boolean.TRUE);
        when(document.getMetadata()).thenReturn(metadata);
        when(documentBundleReader.readBundle("uid", revision)).thenReturn(document);
        
        //When
        List<String> ids = service.getPublicDocuments(revision, GeminiDocument.class);
        
        //Then
        assertSame("Expected only one id", 1, ids.size());
        assertTrue("Expected ids out", ids.contains("uid"));
    }
    
    @Test
    public void checkThatSkipsUnreadableDocuments() throws IOException, UnknownContentTypeException {
        //Given
        String revision = "revision";
        String id = "mydoc id";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        when(documentBundleReader.readBundle(id, revision)).thenThrow(new UnknownContentTypeException("Unreadable"));
        
        //When
        List<String> ids = service.getPublicDocuments(revision, GeminiDocument.class);
        
        //Then
        assertTrue("Expected no matching results", ids.isEmpty());
    }
    
    @Test
    public void checkThatOnlyReadsDocumentsOfCorrectType() throws IOException, UnknownContentTypeException {
        //Given
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList("uid"));
        
        Class differentMetadataType = mock(MetadataDocument.class).getClass();
        
        //When
        List<String> ids = service.getPublicDocuments(revision, differentMetadataType);
        
        //Then
        assertTrue("Expected no matching results", ids.isEmpty());
    }
    
    @Test
    public void checkThatOnlyReadsUserAccessableDocuments() throws IOException, UnknownContentTypeException {
        //Givenss);
        String id = "id";
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn(id);
        MetadataInfo metadata = mock(MetadataInfo.class);
        when(document.getMetadata()).thenReturn(metadata);
        when(documentBundleReader.readBundle(id, revision)).thenReturn(document);
        
        //When
        List<String> ids = service.getPublicDocuments(revision, GeminiDocument.class);
        
        //Then
        assertTrue("Expected no records", ids.isEmpty());
        verify(documentBundleReader).readBundle(id, revision);
    }
    
    @Test
    public void checkThatGetsDatasetSeriesService() throws IOException, UnknownContentTypeException {
        //Given
        List<String> ids = Arrays.asList("a","b","c","d");
        String revision = "revision";
        MetadataInfo metadata = mock(MetadataInfo.class);  
        when(metadata.isPubliclyViewable(Permission.VIEW)).thenReturn(true);
        when(listingService.filterFilenames(any(List.class))).thenReturn(ids);
        GeminiDocument documentA = mock(GeminiDocument.class);
        GeminiDocument documentB = mock(GeminiDocument.class);
        GeminiDocument documentC = mock(GeminiDocument.class);
        GeminiDocument documentD = mock(GeminiDocument.class);
        when(documentA.getId()).thenReturn("a");
        when(documentB.getId()).thenReturn("b");
        when(documentC.getId()).thenReturn("c");
        when(documentD.getId()).thenReturn("d");
        when(documentA.getType()).thenReturn("Dataset");
        when(documentB.getType()).thenReturn("Series");
        when(documentC.getType()).thenReturn("Service");
        when(documentD.getType()).thenReturn("A_N_Other");
        when(documentA.getMetadata()).thenReturn(metadata);
        when(documentB.getMetadata()).thenReturn(metadata);
        when(documentC.getMetadata()).thenReturn(metadata);
        when(documentD.getMetadata()).thenReturn(metadata);
        when(documentBundleReader.readBundle("a", revision)).thenReturn(documentA);
        when(documentBundleReader.readBundle("b", revision)).thenReturn(documentB);
        when(documentBundleReader.readBundle("c", revision)).thenReturn(documentC);
        when(documentBundleReader.readBundle("D", revision)).thenReturn(documentC);
        
        //When
        List<String> publicIds = service.getPublicDocuments(revision, GeminiDocument.class);
        
        //Then
        assertTrue("Expected three records", publicIds.size() == 3);
        verify(documentBundleReader).readBundle("a", revision);
        verify(documentBundleReader).readBundle("b", revision);
        verify(documentBundleReader).readBundle("c", revision);
        verify(documentBundleReader).readBundle("d", revision);
    }
}
