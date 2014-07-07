package uk.ac.ceh.gateway.catalogue.linking;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;

public class DatabaseDocumentLinkingServiceTest {
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private BundledReaderService<GeminiDocument> documentBundleReader;
    @Mock private LinkingRepository linkingRepository;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void canRebuildLinks() throws Exception {
        //Given
        GeminiDocument document = new GeminiDocument();
        when(repo.getFiles()).thenReturn(Arrays.asList("123-123-123", "222-333-444"));
        when(repo.getLatestRevision()).thenReturn(mock(DataRevision.class));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(document);
        DatabaseDocumentLinkingService service = new DatabaseDocumentLinkingService(repo, documentBundleReader, linkingRepository);
        
        //When
        service.rebuildLinks();
        
        //Then
        verify(linkingRepository, times(2)).delete(document);
        verify(linkingRepository, times(2)).add(document);
    }
    
    @Test
    public void canLinkDocuments() throws Exception {
        //Given
        List<String> documents =  Arrays.asList("1bb", "234");
        GeminiDocument document = new GeminiDocument();
        when(repo.getLatestRevision()).thenReturn(mock(DataRevision.class));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(document);
        DatabaseDocumentLinkingService service = new DatabaseDocumentLinkingService(repo, documentBundleReader, linkingRepository);
        
        //When
        service.linkDocuments(documents);
        
        //Then
        verify(linkingRepository, times(2)).delete(document);
        verify(linkingRepository, times(2)).add(document);
    }
    
}
