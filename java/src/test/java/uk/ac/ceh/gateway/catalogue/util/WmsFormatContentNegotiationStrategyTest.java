package uk.ac.ceh.gateway.catalogue.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

public class WmsFormatContentNegotiationStrategyTest {
    @Test
    public void checkThatCanReadMediaTypeFromQueryParameter() throws HttpMediaTypeNotAcceptableException {
        //Given
        MockHttpServletRequest mock = new MockHttpServletRequest();
        NativeWebRequest webRequest = new ServletWebRequest(mock);
        mock.setParameter("INFO_FORMAT", "text/xml");
        WmsFormatContentNegotiationStrategy strategy = new WmsFormatContentNegotiationStrategy("INFO_FORMAT");
        
        //When
        List<MediaType> mediaTypes = strategy.resolveMediaTypes(webRequest);
        
        //Then
        assertThat(mediaTypes.size(), is(1));        
        assertThat(mediaTypes, hasItem(MediaType.TEXT_XML));        
    }
    
    @Test
    public void checkThatNonsenseMediaTypeReturnsEmptyResolvedTypes() throws HttpMediaTypeNotAcceptableException {
        //Given
        MockHttpServletRequest mock = new MockHttpServletRequest();
        NativeWebRequest webRequest = new ServletWebRequest(mock);
        mock.setParameter("INFO_FORMAT", "sneaky.notRellya mediaType");
        WmsFormatContentNegotiationStrategy strategy = new WmsFormatContentNegotiationStrategy("INFO_FORMAT");
        
        //When
        List<MediaType> mediaTypes = strategy.resolveMediaTypes(webRequest);
        
        //Then
        assertThat(mediaTypes.size(), is(0));
    }
    
    @Test
    public void checkThatIgnoresDifferentMissingParameter() throws HttpMediaTypeNotAcceptableException {
        //Given
        MockHttpServletRequest mock = new MockHttpServletRequest();
        NativeWebRequest webRequest = new ServletWebRequest(mock);
        WmsFormatContentNegotiationStrategy strategy = new WmsFormatContentNegotiationStrategy("INFO_FORMAT");
        
        //When
        List<MediaType> mediaTypes = strategy.resolveMediaTypes(webRequest);
        
        //Then
        assertThat(mediaTypes.size(), is(0));
    }
}
