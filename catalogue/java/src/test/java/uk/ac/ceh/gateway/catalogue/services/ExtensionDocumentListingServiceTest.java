package uk.ac.ceh.gateway.catalogue.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.spy;

/**
 *
 * @author cjohn
 */
public class ExtensionDocumentListingServiceTest {    
    private ExtensionDocumentListingService service;
    
    @Before
    public void initMocks() {
        service = spy(new ExtensionDocumentListingService());
    }
    
    @Test
    public void checkThatEmptyListCanBeRead() {
        //Given
        List empty = Collections.EMPTY_LIST;
        
        //When
        List<String> docs = service.filterFilenames(empty);
        
        //Then
        assertTrue("Expected to get no documents", docs.isEmpty());
    }
    
    @Test
    public void checkThatSingleMetadataFileDoesNotResultInDocument() {
        //Given
        List singleFile = Arrays.asList("something.meta");
        
        //When
        List<String> docs = service.filterFilenames(singleFile);
        
        //Then
        assertTrue("Expected to get no documents", docs.isEmpty());
    }
    
    @Test
    public void checkThatSingleRawFileDoesNotResultInDocument() {
        //Given
        List singleFile = Arrays.asList("something.raw");
                
        //When
        List<String> docs = service.filterFilenames(singleFile);
        
        //Then
        assertTrue("Expected to get no documents", docs.isEmpty());
    }
    
    @Test
    public void checkThatPairDoesResultInDocument() {
        //Given
        List singlePair = Arrays.asList("something.raw", "something.meta");
        
        //When
        List<String> docs = service.filterFilenames(singlePair);
        
        //Then
        assertEquals("Expected One file", 1, docs.size());
        assertEquals("Expected that file to be something", "something", docs.get(0));
    }
    
    @Test
    public void checkThatMoreThanOneFileResultsInDocument() {
        //Given
        List singlePair = Arrays.asList("some.raw", "some.meta", "some.img");
        
        //When
        List<String> docs = service.filterFilenames(singlePair);
        
        //Then
        assertEquals("Expected One file", 1, docs.size());
        assertEquals("Expected that file to be some", "some", docs.get(0));
    }
    
    @Test
    public void checkThatCanFindMultipleFiles() {
        //Given
        List multiplePairs = Arrays.asList("some.raw", "some.meta", "some1.raw", "some1.meta");
        
        //When
        List<String> docs = service.filterFilenames(multiplePairs);
        
        //Then
        assertEquals("Expected two file", 2, docs.size());
        assertTrue("Expected to find some", docs.contains("some"));
        assertTrue("Expected to find some1", docs.contains("some1"));
    }
}
