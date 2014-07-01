package uk.ac.ceh.gateway.catalogue.gemini.elements;

import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;

public class DownloadOrderTest {
    private static final String OGL_URL = "http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain";
    
    @Test
    public void isOgl() {
        //Given
        DownloadOrder actual = DownloadOrder.builder().licenseUrl(OGL_URL).build();
        
        //When
        final boolean ogl = actual.isOgl();
        
        //Then
        assertThat("Should be an OGL licensed DownloadOrder", ogl, equalTo(true));
    }
    
    @Test
    public void isNotOgl() {
        //Given
        DownloadOrder actual = DownloadOrder.builder().licenseUrl("some other url").build();
        
        //When
        final boolean ogl = actual.isOgl();
        
        //Then
        assertThat("Should not be an OGL licensed DownloadOrder", ogl, equalTo(false));
    }

}