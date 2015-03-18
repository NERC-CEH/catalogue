package uk.ac.ceh.gateway.catalogue.controllers;

import java.net.URI;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource;
import uk.ac.ceh.gateway.catalogue.services.MetadataInfoEditingService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;

public class PermissionControllerTest {
    @Mock private MetadataInfoEditingService metadataInfoEditingService;
    @Mock private PermissionService permissionService;
    
    private PermissionController permissionController;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        permissionController = new PermissionController(metadataInfoEditingService, permissionService);
    }
    
    @Test
    public void getCurrentPermission() {
        //Given
        CatalogueUser publisher = new CatalogueUser().setUsername("publisher");
        String file = "1234-567-890";
        MetadataInfo info = new MetadataInfo();
        GeminiDocument document = new GeminiDocument()
            .setMetadata(info);
        document.attachUri(URI.create("/documents/" + file));
        PermissionResource expected = new PermissionResource(document);
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/documents/1234-567-890");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        given(metadataInfoEditingService.getMetadataDocument(any(String.class), any(URI.class))).willReturn(document);
        
        
        //When
        HttpEntity<PermissionResource> actual = permissionController.currentPermission(publisher, file);
        
        //Then
        assertThat("Actual permissionResource should equal expected", actual.getBody(), equalTo(expected));
    }
    
    @Test
    public void nonPublisherAttemptToMakeRecordPublic() throws DataRepositoryException {
        //Given
        
        CatalogueUser notPublisher = new CatalogueUser().setUsername("notPublisher");
        String file = "1234-567-890";
        MetadataInfo info = new MetadataInfo();
        info.addPermission(Permission.VIEW, "bob");
        GeminiDocument document = new GeminiDocument()
            .setMetadata(info);
        document.attachUri(URI.create("/documents/" + file));
        PermissionResource expected = new PermissionResource(document);
        
        MetadataInfo mi = new MetadataInfo();
        mi.addPermission(Permission.VIEW, "public");
        GeminiDocument updated = new GeminiDocument();
        updated.setMetadata(mi);
        updated.attachUri(URI.create("/documents/" + file));
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/documents/1234-567-890");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        given(metadataInfoEditingService.getMetadataDocument(any(String.class), any(URI.class))).willReturn(document);
        
        //When
        HttpEntity<PermissionResource> actual = permissionController.updatePermission(notPublisher, file, new PermissionResource(updated));
        
        //Then
        verify(metadataInfoEditingService, never()).saveMetadataInfo(any(String.class), any(MetadataInfo.class), any(CatalogueUser.class), any(String.class));
        assertThat("Actual permissionResource should equal expected", actual.getBody(), equalTo(expected));
    }
    
    @Test
    public void PublisherToMakeRecordPublic() throws DataRepositoryException {
        //Given
        
        CatalogueUser publisher = new CatalogueUser().setUsername("publisher");
        String file = "1234-567-890";
        MetadataInfo info = new MetadataInfo();
        info.addPermission(Permission.VIEW, "bob");
        info.setState("published");
        GeminiDocument document = new GeminiDocument()
            .setMetadata(info);
        document.attachUri(URI.create("/documents/" + file));
        PermissionResource expected = new PermissionResource(document);
        
        MetadataInfo mi = new MetadataInfo();
        mi.addPermission(Permission.VIEW, "bob");
        mi.addPermission(Permission.VIEW, "public");
        mi.setState("published");
        GeminiDocument updated = new GeminiDocument();
        updated.setMetadata(mi);
        updated.attachUri(URI.create("/documents/" + file));
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/documents/1234-567-890");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        given(metadataInfoEditingService.getMetadataDocument(any(String.class), any(URI.class))).willReturn(document);
        given(permissionService.userCanMakePublic()).willReturn(Boolean.TRUE);
        
        //When
        HttpEntity<PermissionResource> actual = permissionController.updatePermission(publisher, file, new PermissionResource(updated));
        
        //Then
        verify(metadataInfoEditingService).saveMetadataInfo(any(String.class), any(MetadataInfo.class), any(CatalogueUser.class), any(String.class));
        assertThat("Actual permissionResource should equal expected", actual.getBody(), equalTo(expected));
    }
    
}