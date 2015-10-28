package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyObject;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

/**
 *
 * @author cjohn
 */
public class ValidationIndexingServiceTest {
    
    @Mock BundledReaderService reader;
    @Mock DocumentListingService listingService;
    @Mock DataRepository repo;
    @Mock PostProcessingService postProcessingService;
    @Mock DocumentIdentifierService documentIdentifierService;
    @Mock IndexGenerator indexGenerator;
    
    private ValidationIndexingService service;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        
        service = new ValidationIndexingService(reader, listingService, repo, postProcessingService, documentIdentifierService, indexGenerator);
    }
    
    @Test
    public void checkThatIndexEmptyIsAtStart() throws Exception {
        //Given
        //Nothing
        
        //When
        boolean isEmpty = service.isIndexEmpty();
        
        //Then
        assertTrue("Expected to be empty", isEmpty);
    }
    
    @Test
    public void checkThatFailedIndexGetsCaught() throws Exception {
        //Given
        String rev = "revision";
        List<String> docs = Arrays.asList("1","2");
        when(indexGenerator.generateIndex(anyObject())).thenThrow(new DocumentIndexingException("I failed"));
        
        //When
        try {
            service.indexDocuments(docs, rev);
            fail("Shouldn't get here");
        }
        catch(DocumentIndexingException ex) {
        //Then
            assertFalse("Expected the index to be full", service.isIndexEmpty());
            assertTrue("Expected to find the failure", service.getFailed().contains("1"));
            assertTrue("Expected to find the failure", service.getFailed().contains("2"));
        }
        
    }
}
