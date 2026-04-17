package uk.ac.ceh.gateway.catalogue.organisations;

import lombok.SneakyThrows;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"server-eidc", "test"})
@DisplayName("OrganisationController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(OrganisationController.class)
class OrganisationControllerTest {

    public static final String QUERY = "queryTest";
    List<Organisation> orgList = List.of(
        new Organisation("id1", "name1", List.of("acronym1", "acronym2"), List.of("aliase1", "aliase2")),
        new Organisation("id2", "name2", Collections.emptyList(), Collections.emptyList())
    );
    String expectedResponse = "[{\"id\":\"id1\",\"name\":\"name1\",\"acronyms\":[\"acronym1\",\"acronym2\"],\"aliases\":[\"aliase1\",\"aliase2\"]},{\"id\":\"id2\",\"name\":\"name2\"}]";

    @MockitoBean
    private OrganisationSolrQueryService organisationService;

    @Autowired
    private MockMvc mvc;

    @Test
    @SneakyThrows
    void getOrganisationWithQuery() {
        //Given
        given(organisationService.query(QUERY)).willReturn(orgList);

        //When
        mvc.perform(get("/organisation/names").queryParam("query", QUERY))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));
    }

    @Test
    @SneakyThrows
    void getOrganisationWithoutQuery() {
        //Given
        given(organisationService.query("*")).willReturn(orgList);

        //When
        mvc.perform(get("/organisation/names"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));
    }

    @Test
    @SneakyThrows
    void throwSolrServerException() {
        //Given
        given(organisationService.query(QUERY)).willThrow(new SolrServerException("Test"));

        //When
        mvc.perform(get("/organisation/names").queryParam("query", QUERY))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"message\":\"Solr did not respond as expected\"}"));
    }
}
