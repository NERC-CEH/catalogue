package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
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
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;

/**
 *
 * @author cjohn
 */
public class MetadataListingServiceTest {
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DataRepository<CatalogueUser> repo;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentListingService listingService;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) BundledReaderService<MetadataDocument> documentBundleReader;
    private MetadataListingService service;
    private final List<String> defaultResourceTypes = Arrays.asList("Dataset","Series","Service");
    
    @Before
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        service = new MetadataListingService(repo,
                                            listingService,
                                            documentBundleReader);
    }
    
    
    @Test
    public void checkThatReadsDocumentsListFromDataRepositiory() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList("uid"));
        
        Multimap<Permission, String> permissions = HashMultimap.create();
        permissions.put(Permission.VIEW, "public");
        MetadataInfo metadata = MetadataInfo.builder().permissions(permissions).catalogue("eidc").state("published").build(); 
        GeminiDocument document = new GeminiDocument();
        document.setId("uid");
        document.setResourceType(Keyword.builder().value("Dataset").build());
        document.setMetadata(metadata);
         
        when(documentBundleReader.readBundle("uid", revision)).thenReturn(document);
        
        //When
        List<String> ids = service.getPublicDocuments(revision, GeminiDocument.class, defaultResourceTypes);
        System.out.println(ids);
        
        //Then
        assertThat("Expected ids out", ids, contains("uid"));
    }
    
    @Test
    public void checkThatSkipsUnreadableDocuments() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        String id = "mydoc id";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        when(documentBundleReader.readBundle(id, revision)).thenThrow(new UnknownContentTypeException("Unreadable"));
        
        //When
        List<String> ids = service.getPublicDocuments(revision, GeminiDocument.class, defaultResourceTypes);
        
        //Then
        assertTrue("Expected no matching results", ids.isEmpty());
    }
    
    @Test
    public void checkThatOnlyReadsDocumentsOfCorrectType() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList("uid"));
        
        Class differentMetadataType = mock(MetadataDocument.class).getClass();
        
        //When
        List<String> ids = service.getPublicDocuments(revision, differentMetadataType, defaultResourceTypes);
        
        //Then
        assertTrue("Expected no matching results", ids.isEmpty());
    }
    
    @Test
    public void checkThatOnlyReadsUserAccessableDocuments() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Givenss);
        String id = "id";
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn(id);
        MetadataInfo metadata = MetadataInfo.builder().build();
        when(document.getMetadata()).thenReturn(metadata);
        when(documentBundleReader.readBundle(id, revision)).thenReturn(document);
        
        //When
        List<String> ids = service.getPublicDocuments(revision, GeminiDocument.class, defaultResourceTypes);
        
        //Then
        assertTrue("Expected no records", ids.isEmpty());
        verify(documentBundleReader).readBundle(id, revision);
    }
    
    @Test
    public void checksThatTypeIsMatched() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String id = "a";
        String resourceType = "Dataset";
        String revision = "revision";
        Multimap<Permission, String> permissions = HashMultimap.create();
        permissions.put(Permission.VIEW, "public");
        MetadataInfo metadata = MetadataInfo.builder().permissions(permissions).catalogue("eidc").state("published").build();  
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn(id);
        when(document.getType()).thenReturn(resourceType);
        when(document.getMetadata()).thenReturn(metadata);
        when(document.getCatalogue()).thenReturn("eidc");
        when(documentBundleReader.readBundle("a", revision)).thenReturn(document);
        
        //When
        List<String> publicIds = service.getPublicDocuments(revision, GeminiDocument.class,Arrays.asList(resourceType));
        
        //Then
        assertTrue("Expected one record", publicIds.size() == 1);
        verify(documentBundleReader).readBundle("a", revision);
    }
    
    @Test
    public void checksThatTypeIsNotMatched() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String id = "a";
        String documentResourceType = "A_N_Other";
        String geminiResourceType = "Dataset";
        String revision = "revision";
        Multimap<Permission, String> permissions = HashMultimap.create();
        permissions.put(Permission.VIEW, "public");
        MetadataInfo metadata = MetadataInfo.builder().permissions(permissions).build();  
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn(id);
        when(document.getType()).thenReturn(documentResourceType);
        when(document.getMetadata()).thenReturn(metadata);
        when(documentBundleReader.readBundle("a", revision)).thenReturn(document);
        
        //When
        List<String> publicIds = service.getPublicDocuments(revision, GeminiDocument.class, Arrays.asList(geminiResourceType));
        
        //Then
        assertTrue("Expected no records", publicIds.isEmpty());
        verify(documentBundleReader).readBundle("a", revision);
    }
    
    @Test
    public void checksThatTypeIsCaseInsensitive() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String id = "a";
        String documentResourceType = "dataset";
        String geminiResourceType = "DATASET";
        String revision = "revision";
        Multimap<Permission, String> permissions = HashMultimap.create();
        permissions.put(Permission.VIEW, "public");
        MetadataInfo metadata = MetadataInfo.builder().permissions(permissions).catalogue("eidc").state("published").build();  
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn(id);
        when(document.getType()).thenReturn(documentResourceType);
        when(document.getMetadata()).thenReturn(metadata);
        when(document.getCatalogue()).thenReturn("eidc");
        when(documentBundleReader.readBundle("a", revision)).thenReturn(document);
        
        //When
        List<String> publicIds = service.getPublicDocuments(revision, GeminiDocument.class, Arrays.asList(geminiResourceType));
        
        //Then
        assertTrue("Expected one record", publicIds.size() == 1);
        verify(documentBundleReader).readBundle("a", revision);
    }
    
    @Test
    public void checkOnlyEidcDocumentListed() throws Exception {
        //given
        String revision = "revision";
        MetadataDocument document = new GeminiDocument()
            .setId("test")
            .setMetadata(
                MetadataInfo.builder().catalogue("ceh").build()
            );
        when(documentBundleReader.readBundle("a", revision)).thenReturn(document);
        
        //when
        List<String> actual = service.getPublicDocuments(revision, GeminiDocument.class, defaultResourceTypes);
        
        //then
        assertThat("should be no iems in list", actual.size(), equalTo(0));
    }
}
