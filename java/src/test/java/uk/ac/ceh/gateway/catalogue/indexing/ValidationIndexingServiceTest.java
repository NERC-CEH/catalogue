package uk.ac.ceh.gateway.catalogue.indexing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidationIndexingServiceTest {
    
    @Mock BundledReaderService reader;
    @Mock DocumentListingService listingService;
    @Mock DataRepository repo;
    @Mock PostProcessingService postProcessingService;
    @Mock DocumentIdentifierService documentIdentifierService;
    @Mock IndexGenerator indexGenerator;
    
    @InjectMocks private ValidationIndexingService service;

    
    @Test
    public void checkThatIndexEmptyIsAtStart() throws Exception {
        //Given
        //Nothing
        
        //When
        boolean isEmpty = service.isIndexEmpty();
        
        //Then
        assertTrue(isEmpty);
    }
    
    @Test
    public void checkThatFailedIndexGetsCaught() throws Exception {
        //Given
        String rev = "revision";
        List<String> docs = Arrays.asList("1","2");
        when(indexGenerator.generateIndex(any())).thenThrow(new DocumentIndexingException("I failed"));
        
        //When
        try {
            service.indexDocuments(docs, rev);
            fail("Shouldn't get here");
        }
        catch(DocumentIndexingException ex) {
        //Then
            assertFalse(service.isIndexEmpty());
            assertTrue(service.getFailed().contains("1"));
            assertTrue(service.getFailed().contains("2"));
        }
        
    }
}
