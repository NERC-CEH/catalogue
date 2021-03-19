package uk.ac.ceh.gateway.catalogue.controllers;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class PermissionControllerTest {
    @Mock private PermissionService permissionService;
    @Mock private DocumentRepository documentRepository;
    
    private PermissionController permissionController;
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        permissionController = new PermissionController(
            permissionService,
            documentRepository
        );
    }
    
    @Test
    public void getCurrentPermission() throws Exception {
        //Given
        CatalogueUser publisher = new CatalogueUser().setUsername("publisher");
        String file = "1234-567-890";
        MetadataInfo info = MetadataInfo.builder().build();
        MetadataDocument document = new GeminiDocument()
            .setMetadata(info);
        PermissionResource expected = new PermissionResource(document);
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/documents/1234-567-890");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        given(documentRepository.read(file)).willReturn(document);
        
        
        //When
        HttpEntity<PermissionResource> actual = permissionController.currentPermission(publisher, file);
        
        //Then
        assertThat("Actual permissionResource should equal expected", actual.getBody(), equalTo(expected));
    }
    

    @Test
    public void permissions() throws Exception {
        //Given
        CatalogueUser publisher = new CatalogueUser().setUsername("publisher");
        String file = "1234-567-890";
        MetadataInfo info = MetadataInfo.builder().catalogue("catalogue").build();
        MetadataDocument document = new GeminiDocument().setMetadata(info);
        
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/documents/1234-567-890");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        given(documentRepository.read(file)).willReturn(document);
        
        
        //When
        HttpEntity<CataloguePermission> actual = permissionController.permissions(publisher, null, file);
        
        //Then
        CataloguePermission expected = CataloguePermission.builder().identity("publisher").catalogue("catalogue").id(file).groups(Lists.newArrayList()).build();
        assertThat(actual.getBody(), equalTo(expected));
    }
    
    @Test
    public void nonPublisherAttemptToMakeRecordPublic() throws Exception {
        //Given
        
        CatalogueUser notPublisher = new CatalogueUser().setUsername("notPublisher");
        String file = "1234-567-890";
        MetadataInfo info = MetadataInfo.builder().catalogue("eidc").build();
        info.addPermission(Permission.VIEW, "bob");
        MetadataDocument original = new GeminiDocument()
            .setMetadata(info);
        original.setUri("/documents/" + file);
        
        MetadataInfo mi = MetadataInfo.builder().build();
        mi.addPermission(Permission.VIEW, "public");
        GeminiDocument updated = new GeminiDocument();
        updated.setMetadata(mi);
        updated.setUri("/documents/" + file);
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/documents/1234-567-890");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        given(documentRepository.read(file)).willReturn(original);
        given(permissionService.userCanMakePublic("eidc")).willReturn(Boolean.FALSE);
        
        //When
        permissionController.updatePermission(notPublisher, file, new PermissionResource(updated));
        
        //Then
        verify(documentRepository).save(notPublisher, original, file, "Permissions of 1234-567-890 changed.");
        verify(permissionService).userCanMakePublic("eidc");
    }
    
    @Test
    public void PublisherToMakeRecordPublic() throws Exception {
        //Given
        CatalogueUser publisher = new CatalogueUser().setUsername("publisher");
        String file = "1234-567-890";
        MetadataInfo info = MetadataInfo.builder().catalogue("eidc").state("published").build();
        info.addPermission(Permission.VIEW, "bob");
        MetadataDocument document = new GeminiDocument()
            .setMetadata(info);
        document.setUri("/documents/" + file);
        
        MetadataInfo mi = MetadataInfo.builder().state("published").catalogue("eidc").build();
        mi.addPermission(Permission.VIEW, "bob");
        mi.addPermission(Permission.VIEW, "public");
        GeminiDocument updated = new GeminiDocument();
        updated.setMetadata(mi);
        updated.setUri("/documents/" + file);
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/documents/1234-567-890");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        given(documentRepository.read(file)).willReturn(document);
        given(permissionService.userCanMakePublic("eidc")).willReturn(Boolean.TRUE);
        
        //When
        permissionController.updatePermission(publisher, file, new PermissionResource(updated));
        
        //Then
        verify(documentRepository).save(publisher, updated, file, "Permissions of 1234-567-890 changed.");
        verify(permissionService).userCanMakePublic("eidc");
    }
    
}