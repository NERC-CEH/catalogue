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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

/**
 *
 * @author cjohn
 */
public class MetadataListingServiceTest {
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DataRepository<CatalogueUser> repo;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentListingService listingService;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) BundledReaderService<MetadataDocument> documentBundleReader;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) PermissionService permissionsService;
    private MetadataListingService service;
    
    
    @Before
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        service = new MetadataListingService(repo,
                                            listingService,
                                            documentBundleReader,
                                            permissionsService);
    }
    
    
    @Test
    public void checkThatReadsDocumentsListFromDataRepositiory() throws IOException, UnknownContentTypeException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList("uid"));
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn("uid");
        MetadataInfo metadata = mock(MetadataInfo.class);
        when(document.getMetadata()).thenReturn(metadata);
        when(documentBundleReader.readBundle("uid", revision)).thenReturn(document);
        when(permissionsService.userCanAccess(user, metadata)).thenReturn(true);
        
        //When
        List<String> ids = service.getDocuments(revision, GeminiDocument.class, user);
        
        //Then
        assertSame("Expected only one id", 1, ids.size());
        assertTrue("Expected ids out", ids.contains("uid"));
    }
    
    @Test
    public void checkThatSkipsUnreadableDocuments() throws IOException, UnknownContentTypeException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        String revision = "revision";
        String id = "mydoc id";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        when(documentBundleReader.readBundle(id, revision)).thenThrow(new UnknownContentTypeException("Unreadable"));
        
        //When
        List<String> ids = service.getDocuments(revision, GeminiDocument.class, user);
        
        //Then
        assertTrue("Expected no matching results", ids.isEmpty());
    }
    
    @Test
    public void checkThatOnlyReadsDocumentsOfCorrectType() throws IOException, UnknownContentTypeException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList("uid"));
        
        Class differentMetadataType = mock(MetadataDocument.class).getClass();
        
        //When
        List<String> ids = service.getDocuments(revision, differentMetadataType, user);
        
        //Then
        assertTrue("Expected no matching results", ids.isEmpty());
        verify(permissionsService, never()).userCanAccess(any(CatalogueUser.class), any(MetadataInfo.class));
    }
    
    @Test
    public void checkThatOnlyReadsUserAccessableDocuments() throws IOException, UnknownContentTypeException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        String id = "id";
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn(id);
        MetadataInfo metadata = mock(MetadataInfo.class);
        when(document.getMetadata()).thenReturn(metadata);
        when(documentBundleReader.readBundle(id, revision)).thenReturn(document);
        when(permissionsService.userCanAccess(user, metadata)).thenReturn(false);
        
        //When
        List<String> ids = service.getDocuments(revision, GeminiDocument.class, user);
        
        //Then
        assertTrue("Expected no records", ids.isEmpty());
        verify(documentBundleReader).readBundle(id, revision);
    }
}
