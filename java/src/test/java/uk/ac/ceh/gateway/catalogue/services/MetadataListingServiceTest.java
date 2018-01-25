package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MetadataListingServiceTest {
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private DocumentListingService listingService;
    @Mock private BundledReaderService<MetadataDocument> documentBundleReader;
    private MetadataListingService service;
    private final List<String> defaultResourceTypes = Arrays.asList("Dataset","Series","Service");
    
    @Before
    @SneakyThrows
    public void initMocks() {
        service = new MetadataListingService(repo,
                                            listingService,
                                            documentBundleReader);
    }

    @Test
    @SneakyThrows
    public void onlyPublicDocumentsForCatalogue() {
        //given
        DataRevision<CatalogueUser> revision = new DataRevision<CatalogueUser>() {
            @Override
            public String getRevisionID() {
                return "current";
            }

            @Override
            public String getMessage() {
                return null;
            }

            @Override
            public String getShortMessage() {
                return null;
            }

            @Override
            public CatalogueUser getAuthor() {
                return null;
            }
        };
        Multimap<Permission, String> publicPermissions = ImmutableListMultimap.of(Permission.VIEW, "public");
        Multimap<Permission, String> draftPermissions = ImmutableListMultimap.of(Permission.VIEW, "another");
        MetadataInfo publicMeta = MetadataInfo.builder().catalogue("eidc").permissions(publicPermissions).state("published").build();
        MetadataInfo draftMeta = MetadataInfo.builder().catalogue("eidc").permissions(draftPermissions).state("draft").build();
        MetadataDocument public1 = new GeminiDocument().setId("123").setMetadata(publicMeta);
        MetadataDocument public2 = new GeminiDocument().setId("456").setMetadata(publicMeta);
        MetadataDocument draft = new GeminiDocument().setId("789").setMetadata(draftMeta);
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList("123", "456", "789"));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(public1, public2, draft);
        when(repo.getLatestRevision()).thenReturn(revision);

        //when
        List<String> actual = service.getPublicDocumentsOfCatalogue("eidc");

        //then
        assertThat("should be public documents only", actual, contains("123", "456"));
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
    @SneakyThrows
    public void checkThatOnlyReadsUserAccessibleDocuments() {
        //Given
        String id = "id";
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        MetadataInfo metadata = MetadataInfo.builder().build();
        MetadataDocument document = new GeminiDocument().setId(id).setMetadata(metadata);
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
    @SneakyThrows
    public void checksThatTypeIsNotMatched() {
        //Given
        String id = "a";
        String documentResourceType = "A_N_Other";
        String geminiResourceType = "Dataset";
        String revision = "revision";
        Multimap<Permission, String> permissions = HashMultimap.create();
        permissions.put(Permission.VIEW, "public");
        MetadataInfo metadata = MetadataInfo.builder().permissions(permissions).build();  
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        MetadataDocument document = new GeminiDocument().setId(id).setType(documentResourceType).setMetadata(metadata);
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
    @SneakyThrows
    public void checkOnlyEidcDocumentListed() {
        //given
        String revision = "revision";
        MetadataDocument document = new GeminiDocument()
            .setId("test")
            .setMetadata(
                MetadataInfo.builder().catalogue("ceh").build()
            );
        
        //when
        List<String> actual = service.getPublicDocuments(revision, GeminiDocument.class, defaultResourceTypes);
        
        //then
        assertThat("should be no items in list", actual.size(), equalTo(0));
    }
}
