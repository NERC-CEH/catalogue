package uk.ac.ceh.gateway.catalogue.linking;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
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
        when(repo.getFiles()).thenReturn(Arrays.asList("123-123-123"));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(document);
        DatabaseDocumentLinkingService service = new DatabaseDocumentLinkingService(repo, documentBundleReader, linkingRepository);
        
        //When
        service.rebuildLinks();
        
        //Then
        verify(linkingRepository).delete(document);
        verify(linkingRepository).add(document);
    }
    
}
