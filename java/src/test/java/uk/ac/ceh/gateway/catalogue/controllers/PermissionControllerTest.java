package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.permission.CataloguePermission;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.EIDC_PUBLISHER_USERNAME;

// TODO: convert remaining tests to use mvc

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("PermissionController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=PermissionController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
public class PermissionControllerTest {
    @MockBean(name="permission") private PermissionService permissionService;
    @MockBean private DocumentRepository documentRepository;
    @MockBean private CatalogueService catalogueService;
    @MockBean private ProfileService profileService;

    private PermissionController permissionController;

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    private final String file = "12345";
    private final String catalogueKey = "eidc";

    private void givenUserHasPermission() {
        given(permissionService.toAccess(any(CatalogueUser.class), eq(file), eq("VIEW")))
            .willReturn(true);
    }

    @SneakyThrows
    private void givenMetadataDocument() {
        val gemini = new GeminiDocument();
        gemini.setId(file);
        gemini.setMetadata(MetadataInfo.builder().catalogue(catalogueKey).build());
        given(documentRepository.read(file))
            .willReturn(gemini);
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("permission", permissionService);
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
    }

    private void givenUserCanEdit() {
        given(permissionService.userCanEdit(file))
            .willReturn(true);
    }

    private void givenCatalogue() {
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(Catalogue.builder()
            .id(catalogueKey)
            .title("Foo")
            .url("https://example.com")
            .contactUrl("")
            .logo("eidc.png")
            .build());
    }

    @BeforeEach
    void setup() {
        permissionController = new PermissionController(permissionService, documentRepository);
    }

    @Test
    @SneakyThrows
    void getCurrentPermissions() {
        //given
        givenUserHasPermission();
        givenMetadataDocument();
        givenFreemarkerConfiguration();
        givenUserCanEdit();
        givenCatalogue();

        //when
        mvc.perform(
            get("/documents/{file}/permission", file)
            .header("remote-user", EIDC_PUBLISHER_USERNAME)
            .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML));
    }

    @Test
    public void getCurrentPermission() throws Exception {
        //Given
        CatalogueUser publisher = new CatalogueUser("publisher", "publisher@example.com");
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
        CatalogueUser publisher = new CatalogueUser("publisher", "publisher");
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

        CatalogueUser notPublisher = new CatalogueUser("notPublisher", "notPublisher");
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
        CatalogueUser publisher = new CatalogueUser("publisher", "publisher");
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
