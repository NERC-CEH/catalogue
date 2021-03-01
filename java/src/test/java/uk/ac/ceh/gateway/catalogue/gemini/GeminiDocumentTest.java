package uk.ac.ceh.gateway.catalogue.gemini;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldSetter;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeminiDocumentTest {

    private Supplemental supplemental;
    private Supplemental isReferencedBy;
    private Supplemental isSupplementTo;

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
        assertTrue("Expected to be map viewable", isMapViewable);
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
        assertFalse("Expected to not be map viewable", isMapViewable);
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
        assertEquals("Expected a map viewer url", "/maps#layers/metadataId", url);
    }
    
    @Test
    public void checkThatMapViewerURLIsNullIfNotMapViewable() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.isMapViewable()).thenReturn(false);
        
        //When
        String url = document.getMapViewerUrl();
        
        //Then
        assertNull("Expected to get a null url for the map viewer", url);
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
        supplemental = Supplemental.builder().name("foo").type("other").build();
        isReferencedBy = Supplemental.builder().name("foo").type("isReferencedBy").build();
        isSupplementTo = Supplemental.builder().name("foo").type("isSupplementTo").build();
        List<Supplemental> supplementals = new ArrayList<>();
        supplementals.add(supplemental);
        supplementals.add(isReferencedBy);
        supplementals.add(isSupplementTo);
        document.setSupplemental(supplementals);

        // When
        int output = document.getIncomingCitationCount();

        // Then
        assertThat(output, is(2));
    }

    @Test
    public void testGetIncomingCitationCount_ShouldBeEmpty() {
        // Given
        GeminiDocument document = new GeminiDocument();
        supplemental = Supplemental.builder().name("foo").type("other").build();
        List<Supplemental> supplementals = new ArrayList<>();
        supplementals.add(supplemental);
        document.setSupplemental(supplementals);

        // When
        int output = document.getIncomingCitationCount();

        // Then
        assertThat(output, is(0));
    }
}