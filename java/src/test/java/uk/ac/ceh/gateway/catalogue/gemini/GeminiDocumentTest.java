package uk.ac.ceh.gateway.catalogue.gemini;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class GeminiDocumentTest {

    @Test
    public void checkIfIsMapViewableIfGetCapabilitiesOnlineResourceExists() {
        //Given
        OnlineResource wmsResource = OnlineResource.builder()
            .url("http://www.com?request=GetCapabilities&SERVICE=WMS")
            .name("wms resource") 
            .description("wms description")
            .build();
        GeminiDocument document = new GeminiDocument();
        document.setOnlineResources(Arrays.asList(wmsResource));
        
        //When
        boolean isMapViewable = document.isMapViewable();
        
        //Then
        assertTrue(isMapViewable);
    }
    
    
    @Test
    public void checkIfIsntMapViewableIfGetCapabilitiesOnlineResourceDoesntExists() {
        //Given
        OnlineResource wmsResource = OnlineResource.builder()
                .url("http://www.google.com") 
                .name("wms resource")
                .description("wms description")
                .build();
        GeminiDocument document = new GeminiDocument();
        document.setOnlineResources(Arrays.asList(wmsResource));
        
        //When
        boolean isMapViewable = document.isMapViewable();
        
        //Then
        assertFalse(isMapViewable);
    }
    
    @Test
    public void getLinkToMapViewer() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class, CALLS_REAL_METHODS);
        doReturn(true).when(document).isMapViewable();
        doReturn("metadataId").when(document).getId();
        
        //When
        String url = document.getMapViewerUrl();
        
        //Then
        assertEquals("/maps#layers/metadataId", url);
    }
    
    @Test
    public void checkThatMapViewerURLIsNullIfNotMapViewable() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.isMapViewable()).thenReturn(false);
        
        //When
        String url = document.getMapViewerUrl();
        
        //Then
        assertNull(url);
    }
    
    @Test
    public void checkThatMetadataDateTimeIsEmptyStringIfNoMetadataDate() {
        //Given
        GeminiDocument document = new GeminiDocument();
        
        //When
        String actual = document.getMetadataDateTime();
        
        //Then
        assertThat("MetadataDateTime should be empty string", actual, equalTo(""));
        
    }

    @Test
    public void testGetIncomingCitationCount() {
        // Given
        GeminiDocument document = new GeminiDocument();
        Supplemental supplemental = Supplemental.builder().name("foo").function("other").build();
        Supplemental isReferencedBy = Supplemental.builder().name("foo").function("isReferencedBy").build();
        Supplemental isSupplementTo = Supplemental.builder().name("foo").function("isSupplementTo").build();
        List<Supplemental> supplementals = new ArrayList<>();
        supplementals.add(supplemental);
        supplementals.add(isReferencedBy);
        supplementals.add(isSupplementTo);
        document.setSupplemental(supplementals);
        long expected = 2;

        // When
        long output = document.getIncomingCitationCount();

        // Then
        assertThat(output, is(expected));
    }

    @Test
    public void testGetIncomingCitationCount_ShouldBeEmpty() {
        // Given
        GeminiDocument document = new GeminiDocument();
        Supplemental supplemental = Supplemental.builder().name("foo").function("other").build();
        List<Supplemental> supplementals = new ArrayList<>();
        supplementals.add(supplemental);
        document.setSupplemental(supplementals);
        long expected = 0;

        // When
        long output = document.getIncomingCitationCount();

        // Then
        assertThat(output, is(expected));
    }

    @Test
    public void testGetIncomingCitationCount_NoSupplemental() {
        // Given
        GeminiDocument document = new GeminiDocument();
        List<Supplemental> supplementals = new ArrayList<>();
        document.setSupplemental(supplementals);
        long expected = 0;

        // When
        long output = document.getIncomingCitationCount();

        // Then
        assertThat(output, is(expected));
    }
}