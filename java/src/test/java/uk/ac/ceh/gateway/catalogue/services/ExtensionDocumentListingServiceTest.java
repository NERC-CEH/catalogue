package uk.ac.ceh.gateway.catalogue.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

public class ExtensionDocumentListingServiceTest {    
    private ExtensionDocumentListingService service;
    
    @BeforeEach
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
        assertTrue(docs.isEmpty());
    }
    
    @Test
    public void checkThatSingleMetadataFileDoesNotResultInDocument() {
        //Given
        List singleFile = Arrays.asList("something.meta");
        
        //When
        List<String> docs = service.filterFilenames(singleFile);
        
        //Then
        assertTrue(docs.isEmpty());
    }
    
    @Test
    public void checkThatSingleRawFileDoesNotResultInDocument() {
        //Given
        List singleFile = Arrays.asList("something.raw");
                
        //When
        List<String> docs = service.filterFilenames(singleFile);
        
        //Then
        assertTrue(docs.isEmpty());
    }
    
    @Test
    public void checkThatPairDoesResultInDocument() {
        //Given
        List singlePair = Arrays.asList("something.raw", "something.meta");
        
        //When
        List<String> docs = service.filterFilenames(singlePair);
        
        //Then
        assertEquals(1, docs.size());
        assertEquals("something", docs.get(0));
    }
    
    @Test
    public void checkThatMoreThanOneFileResultsInDocument() {
        //Given
        List singlePair = Arrays.asList("some.raw", "some.meta", "some.img");
        
        //When
        List<String> docs = service.filterFilenames(singlePair);
        
        //Then
        assertEquals(1, docs.size());
        assertEquals("some", docs.get(0));
    }
    
    @Test
    public void checkThatCanFindMultipleFiles() {
        //Given
        List multiplePairs = Arrays.asList("some.raw", "some.meta", "some1.raw", "some1.meta");
        
        //When
        List<String> docs = service.filterFilenames(multiplePairs);
        
        //Then
        assertEquals(2, docs.size());
        assertTrue(docs.contains("some"));
        assertTrue(docs.contains("some1"));
    }
    
    @Test
    public void checkThatRawOnlyResultsInDocument() {
        //Given
        List raw = Arrays.asList("some.raw");
        
        //When
        List<String> docs = service.filterFilenamesEitherExtension(raw);
        
        //Then
        assertEquals(1, docs.size());
        assertEquals("some", docs.get(0));
    }
    
    @Test
    public void checkThatUnknownExtensionsDoesNotResultInDocument() {
        //Given
        List unknown = Arrays.asList("some.unkown");
        
        //When
        List<String> docs = service.filterFilenamesEitherExtension(unknown);
        
        //Then
        assertTrue(docs.isEmpty());
    }
}
