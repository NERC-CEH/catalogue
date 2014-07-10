package uk.ac.ceh.gateway.catalogue.linking;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListItem;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;

public class GitDocumentLinkServiceTest {
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private BundledReaderService<GeminiDocument> documentBundleReader;
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
        verify(linkDatabase).addMetadata((List<Metadata>) anyCollectionOf(Metadata.class));
        verify(linkDatabase).addCoupledResources((List<CoupledResource>) anyCollectionOf(CoupledResource.class));
    }
    
    @Test
    public void canLinkDocuments() throws Exception {
        //Given
        when(repo.getLatestRevision()).thenReturn(mock(DataRevision.class));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(mock(GeminiDocument.class));
        GitDocumentLinkService service = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        
        //When
        service.linkDocuments(Arrays.asList("1bb", "234"));
        
        //Then
        verify(linkDatabase).addMetadata((List<Metadata>) anyCollectionOf(Metadata.class));
        verify(linkDatabase).addCoupledResources((List<CoupledResource>) anyCollectionOf(CoupledResource.class));
    }
    
    @Test
    public void canGetDatasetLinksForService() {
        //Given
        String fileIdentifier = "absd-asd";
        GeminiDocument dataset = new GeminiDocument();
        dataset.setId(fileIdentifier);
        dataset.setResourceType(CodeListItem.builder().value("dataset").build());
        GitDocumentLinkService service = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8080").path("documents/{fileIdentifier}");
        
        //When
        service.getLinks(dataset, builder);
        
        //Then
        verify(linkDatabase).findServicesForDataset(fileIdentifier);
    }
    
    @Test
    public void canGetServiceLinksForDataset() {
        //Given
        String fileIdentifier = "absd-asd";
        GeminiDocument service = new GeminiDocument();
        service.setId(fileIdentifier);
        service.setResourceType(CodeListItem.builder().value("service").build());
        GitDocumentLinkService linkService = new GitDocumentLinkService(repo, documentBundleReader, linkDatabase);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8080").path("documents/{fileIdentifier}");
        
        //When
        linkService.getLinks(service, builder);
        
        //Then
        verify(linkDatabase).findDatasetsForService(fileIdentifier);
    }
    
}