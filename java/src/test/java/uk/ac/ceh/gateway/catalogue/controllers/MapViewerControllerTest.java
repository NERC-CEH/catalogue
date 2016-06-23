/*
 * Copyright (C) 2016 cjohn
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package uk.ac.ceh.gateway.catalogue.controllers;

import java.net.URISyntaxException;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;

/**
 *
 * @author cjohn
 */
public class MapViewerControllerTest {
    @Mock MapServerDetailsService mapServerDetailsService;
    
    private MapViewerController controller;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        
        controller = spy(new MapViewerController(mapServerDetailsService));
    }
    
    @Test
    public void checkThatCanProxyToMapServer() throws URISyntaxException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("REQUEST=GetMap&FORMAT=image/png");
        when(mapServerDetailsService.getLocalWMSRequest("file", "REQUEST=GetMap&FORMAT=image/png")).thenReturn("http://rewritten");
        
        //When
        TransparentProxy proxy = controller.wmsService("file", request);
        
        //Then
        assertEquals(proxy.getUri().toString(), "http://rewritten");
    }
}
