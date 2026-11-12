package uk.ac.ceh.gateway.catalogue.people;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"server-eidc", "test", "search-basic"})
@DisplayName("PeopleController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PeopleControllerTest extends AbstractMvcTest {

    @MockitoBean private PeopleSearchService peopleSearchService;
    @MockitoBean private OrcidRegistry orcidRegistry;

    @Test
    @SneakyThrows
    @DisplayName("offers the people the catalogue names, for the signed-in editor")
    void names() {
        given(peopleSearchService.search(eq("rowland"), argThat(user -> "test".equals(user.getUsername()))))
            .willReturn(List.of(new KnownPerson("https://orcid.org/0000-0002-0459-506X", "Clare", "Rowland", List.of(),
                List.of(new KnownPerson.Affiliation("UK Centre for Ecology & Hydrology", "https://ror.org/00pggkr55")), 3)));

        mvc.perform(get("/people/names").queryParam("query", "rowland"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
                [{"orcid": "https://orcid.org/0000-0002-0459-506X", "givenName": "Clare", "familyName": "Rowland",
                  "affiliations": [{"name": "UK Centre for Ecology & Hydrology", "identifier": "https://ror.org/00pggkr55"}],
                  "records": 3}]
                """));
    }

    @Test
    @SneakyThrows
    @DisplayName("says whose an iD is, read however it was typed")
    void orcid() {
        given(orcidRegistry.person("0000-0002-0459-506X"))
            .willReturn(Optional.of(new OrcidPerson("https://orcid.org/0000-0002-0459-506X", "Clare", "Rowland", "", List.of())));

        mvc.perform(get("/people/orcid").queryParam("id", "0000-0002-0459-506x"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {"orcid": "https://orcid.org/0000-0002-0459-506X", "givenNames": "Clare", "familyName": "Rowland"}
                """));
    }

    @Test
    @SneakyThrows
    @DisplayName("is 404 for an iD ORCID never issued")
    void orcidNotIssued() {
        given(orcidRegistry.person("0000-0002-1825-0097")).willReturn(Optional.empty());

        mvc.perform(get("/people/orcid").queryParam("id", "https://orcid.org/0000-0002-1825-0097"))
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("is 400, without asking ORCID, for an iD whose check character is wrong")
    void orcidInvalid() {
        mvc.perform(get("/people/orcid").queryParam("id", "0000-0002-1825-0098"))
            .andExpect(status().isBadRequest());

        verify(orcidRegistry, never()).person(anyString());
    }

    @Test
    @SneakyThrows
    @DisplayName("is 502 when ORCID can't be asked")
    void orcidUnavailable() {
        given(orcidRegistry.person("0000-0002-1825-0097"))
            .willThrow(new OrcidRegistry.OrcidUnavailableException("0000-0002-1825-0097", new RuntimeException()));

        mvc.perform(get("/people/orcid").queryParam("id", "0000-0002-1825-0097"))
            .andExpect(status().isBadGateway());
    }

    @Test
    @SneakyThrows
    @DisplayName("is for signed-in editors only")
    void signedInOnly() {
        var controller = new PeopleController(peopleSearchService, orcidRegistry);

        assertThat(controller.names(CatalogueUser.PUBLIC_USER, "rowland").getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
        assertThat(controller.orcid(CatalogueUser.PUBLIC_USER, "0000-0002-1825-0097").getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
        verifyNoInteractions(peopleSearchService, orcidRegistry);
    }
}
