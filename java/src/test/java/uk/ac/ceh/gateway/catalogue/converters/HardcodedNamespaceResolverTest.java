package uk.ac.ceh.gateway.catalogue.converters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class HardcodedNamespaceResolverTest {
    private HardcodedGeminiNamespaceResolver resolver;

    @BeforeEach
    public void createHardcodedNamespaceResolver() {
        resolver = new HardcodedGeminiNamespaceResolver();
    }

    @Test
    public void checkCanResolveSrvPrefix() {
        //Given
        String prefix = "srv";

        //When
        String uri = resolver.getNamespaceURI(prefix);

        //Then
        assertEquals("http://www.isotc211.org/2005/srv", uri);
    }

    @Test
    public void checkCanResolveXlinkPrefix() {
        //Given
        String prefix = "xlink";

        //When
        String uri = resolver.getNamespaceURI(prefix);

        //Then
        assertEquals("http://www.w3.org/1999/xlink", uri);
    }

    @Test
    public void checkCanResolveGMDPrefix() {
        //Given
        String prefix = "gmd";

        //When
        String uri = resolver.getNamespaceURI(prefix);

        //Then
        assertEquals("http://www.isotc211.org/2005/gmd", uri);
    }

    @Test
    public void checkCanResolveGCOPrefix() {
        //Given
        String prefix = "gco";

        //When
        String uri = resolver.getNamespaceURI(prefix);

        //Then
        assertEquals("http://www.isotc211.org/2005/gco", uri);
    }

    @Test
    public void checkCanResolveCSWPrefix() {
        //Given
        String prefix = "csw";

        //When
        String uri = resolver.getNamespaceURI(prefix);

        //Then
        assertEquals("http://www.opengis.net/cat/csw/2.0.2", uri);
    }

    @Test
    public void checkCantResolveMixedCasePrefix() {
        //Given
        String prefix = "CsW";

        //When
        String uri = resolver.getNamespaceURI(prefix);

        //Then
        assertEquals("", uri);
    }

    @Test
    public void checkUnHandledPrefixResolvesToEmptyString() {
        //Given
        String prefix = "unHandledPrefix";

        //When
        String uri = resolver.getNamespaceURI(prefix);

        //Then
        assertEquals("", uri);
    }

    @Test
    public void checkIllegalArgumentExceptionIsThrownOnNullUriLookup() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //Given
            String prefix = null;

            //When
            resolver.getNamespaceURI(prefix);

            //Then
            fail("Expected to fail");
        });
    }

}
