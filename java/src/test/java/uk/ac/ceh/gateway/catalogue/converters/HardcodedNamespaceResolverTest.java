package uk.ac.ceh.gateway.catalogue.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author cjohn
 */
public class HardcodedNamespaceResolverTest {
    private HardcodedNamespaceResolver resolver;
    
    @Before
    public void createHardcodedNamespaceResolver() {
        resolver = new HardcodedNamespaceResolver();
    }
    
    @Test
    public void checkCanResolveGMDPrefix() {
        //Given
        String prefix = "gmd";
        
        //When
        String uri = resolver.getNamespaceURI(prefix);
        
        //Then
        assertEquals("Expected the gmd uri", "http://www.isotc211.org/2005/gmd", uri);
    }
    
    @Test
    public void checkCanResolveGCOPrefix() {
        //Given
        String prefix = "gco";
        
        //When
        String uri = resolver.getNamespaceURI(prefix);
        
        //Then
        assertEquals("Expected the gco uri", "http://www.isotc211.org/2005/gco", uri);
    }
    
    @Test
    public void checkCanResolveCSWPrefix() {
        //Given
        String prefix = "csw";
        
        //When
        String uri = resolver.getNamespaceURI(prefix);
        
        //Then
        assertEquals("Expected the csw uri", "http://www.opengis.net/cat/csw/2.0.2", uri);
    }
    
    @Test
    public void checkCantResolveMixedCasePrefix() {
        //Given
        String prefix = "CsW";
        
        //When
        String uri = resolver.getNamespaceURI(prefix);
        
        //Then
        assertEquals("Expected an empty string uri", "", uri);
    }
    
    @Test
    public void checkUnHandledPrefixResolvesToEmptyString() {
        //Given
        String prefix = "unHandledPrefix";
        
        //When
        String uri = resolver.getNamespaceURI(prefix);
        
        //Then
        assertEquals("Expected an empty string uri", "", uri);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void checkIllegalArgumentExceptionIsThrownOnNullUriLookup() {
        //Given
        String prefix = null;
        
        //When
        String uri = resolver.getNamespaceURI(prefix);
        
        //Then
        fail("Expected to fail");
    }
    
}
