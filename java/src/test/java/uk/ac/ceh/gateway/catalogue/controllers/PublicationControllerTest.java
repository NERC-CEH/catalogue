package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.publication.State;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.publication.PublicationService;

import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.EIDC_PUBLISHER_USERNAME;

//TODO: complete testing of other endpoints

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("PublicationController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})
@WebMvcTest(
    controllers=PublicationController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class PublicationControllerTest {
    @MockBean private PublicationService publicationService;
    @MockBean(name="permission") private PermissionService permissionService;
    @MockBean private CatalogueService catalogueService;

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    private final String file = "345-678";
    private final String catalogueKey = "eidc";

    private void givenUserCanView() {
        given(permissionService.toAccess(any(CatalogueUser.class), eq(file), eq("VIEW")))
            .willReturn(true);
    }

    private void givenUserCanEdit() {
        given(permissionService.userCanEdit(file))
            .willReturn(true);
    }

    private void givenCurrentState() {
        val state = new StateResource(State.UNKNOWN_STATE, new HashSet<>(), file, catalogueKey);
        given(publicationService.current(any(CatalogueUser.class), eq(file)))
            .willReturn(state);
    }

    private void givenStateTransition() {
        val state = new StateResource(State.UNKNOWN_STATE, new HashSet<>(), file, catalogueKey);
        given(publicationService.transition(any(CatalogueUser.class), eq(file), eq("foo")))
            .willReturn(state);
    }

    private void givenCatalogue() {
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(Catalogue.builder().id(catalogueKey).title("Foo").url("https://example.com").contactUrl("").logo("eidc.png").build());
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
    }

    @Test
    @SneakyThrows
    void getCurrentPublication() {
        //given
        givenUserCanView();
        givenCurrentState();
        givenCatalogue();
        givenFreemarkerConfiguration();

        //when
        mvc.perform(
            get("/documents/{file}/publication", file)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML))
            .andDo(print());

    }

    @Test
    @SneakyThrows
    void transitionState() {
        //given
        val toState = new State("foo", "bar");
        givenUserCanEdit();
        givenStateTransition();
        givenCatalogue();
        givenFreemarkerConfiguration();

        //when
        mvc.perform(
            post("/documents/{file}/publication/{toState}", file, toState.getId())
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML))
            .andDo(print());
    }
}
