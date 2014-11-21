package uk.ac.ceh.gateway.catalogue.linking;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;

public class GitDocumentLinkServiceTest {
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private BundledReaderService<MetadataDocument> documentBundleReader;
    @Mock private LinkDatabase linkDatabase;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void canRebuildLinks() throws Exception {
        //Given
        when(repo.getFiles()).thenReturn(Arrays.asList("123-123-123", "222-333-444"));
        when(repo.getLatestRevision()).thenReturn(mock(DataRevision.class));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(mock(GeminiDocument.class));
        GitDocumentLinkService service = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        
        //When
        service.rebuildLinks();
        
        //Then
        verify(linkDatabase).empty();
        verify(repo).getFiles(any(String.class));
        verify(linkDatabase).addMetadata(anySetOf(Metadata.class));
        verify(linkDatabase).addCoupledResources(anySetOf(CoupledResource.class));
    }
    
    @Test
    public void canLinkDocuments() throws Exception {
        //Given
        when(repo.getLatestRevision()).thenReturn(mock(DataRevision.class));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(mock(GeminiDocument.class));
        GitDocumentLinkService service = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        
        //When
        service.linkDocuments(new HashSet(Arrays.asList("1bb", "234")));
        
        //Then
        verify(linkDatabase).addMetadata((Set<Metadata>) anySetOf(Metadata.class));
        verify(linkDatabase).addCoupledResources((Set<CoupledResource>) anySetOf(CoupledResource.class));
    }
    
    @Test
    public void canGetDatasetLinksForService() {
        //Given
        String fileIdentifier = "absd-asd";
        GeminiDocument dataset = new GeminiDocument();
        dataset.setId(fileIdentifier);
        dataset.setResourceType("dataset");
        GitDocumentLinkService service = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        String urlFragement = "http://localhost:8080/documents/";
        
        //When
        service.getLinks(dataset, urlFragement);
        
        //Then
        verify(linkDatabase).findServicesForDataset(fileIdentifier);
    }
    
    @Test
    public void canGetServiceLinksForDataset() {
        //Given
        String fileIdentifier = "absd-asd";
        GeminiDocument service = new GeminiDocument();
        service.setId(fileIdentifier);
        service.setResourceType("service");
        GitDocumentLinkService linkService = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        String urlFragement = "http://localhost:8080/documents/";
        
        //When
        linkService.getLinks(service, urlFragement);
        
        //Then
        verify(linkDatabase).findDatasetsForService(fileIdentifier);
    }
    
    @Test
    public void canGetParent() {
        //Given
        String fileIdentifier = "absd-asd";
        GeminiDocument document = new GeminiDocument();
        document.setId(fileIdentifier);
        GitDocumentLinkService linkService = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        String urlFragement = "http://localhost:8080/documents/";
        
        //When
        linkService.getParent(document, urlFragement);
        
        //Then
        verify(linkDatabase).findParent(fileIdentifier);
    }
    
    @Test
    public void canGetChildren() {
        //Given
        String fileIdentifier = "absd-asd";
        GeminiDocument document = new GeminiDocument();
        document.setId(fileIdentifier);
        GitDocumentLinkService linkService = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        String urlFragement = "http://localhost:8080/documents/";
        
        //When
        linkService.getChildren(document, urlFragement);
        
        //Then
        verify(linkDatabase).findChildren(fileIdentifier);
    }
    
    @Test
    public void emptyValueDelegatesToDatabase() {
        //Given
        GitDocumentLinkService linkService = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        when(linkDatabase.isEmpty()).thenReturn(true);
        
        //When
        boolean isEmpty = linkService.isEmpty();
        
        //Then
        assertTrue("Expected to get isEmpty from linkDatabase", isEmpty);
        verify(linkDatabase).isEmpty();
    }
    
    @Test
    public void populatedValueDelegatesToDatabase() {
        //Given
        GitDocumentLinkService linkService = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        when(linkDatabase.isEmpty()).thenReturn(false);
        
        //When
        boolean isEmpty = linkService.isEmpty();
        
        //Then
        assertFalse("Expected to get isEmpty from linkDatabase", isEmpty);
        verify(linkDatabase).isEmpty();
    }
    
}