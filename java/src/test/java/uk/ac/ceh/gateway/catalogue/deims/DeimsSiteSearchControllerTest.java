package uk.ac.ceh.gateway.catalogue.deims;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrServerException;
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
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;

import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"server:elter", "test"})
@DisplayName("DeimsSiteSearchController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=DeimsSiteSearchController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class DeimsSiteSearchControllerTest {

    public static final String SITE_1 = "site1";
    public static final String SITE_2 = "site2";
    public static final String QUERY = "queryTest";
    public static final String PREFIX = "https://example.com/";

    @MockBean
    private DeimsSolrQueryService deimsService;

    @Autowired private MockMvc mvc;

    @SneakyThrows
    private void givenQueryResponse() {
        val site1 = new DeimsSite(SITE_1, PREFIX, "1");
        val site2 = new DeimsSite(SITE_2, PREFIX, "2");

        given(deimsService.query(QUERY))
            .willReturn(Arrays.asList(
                new DeimsSolrIndex(site1),
                new DeimsSolrIndex(site2)
            ));
    }

    @SneakyThrows
    private void givenQueryResponseNoQuery() {
        val site1 = new DeimsSite(SITE_1, PREFIX, "1");
        val site2 = new DeimsSite(SITE_2, PREFIX, "2");

        given(deimsService.query("*"))
            .willReturn(Arrays.asList(
                new DeimsSolrIndex(site1),
                new DeimsSolrIndex(site2)
            ));
    }

    @Test
    @SneakyThrows
    void getSites() {
        //Given
        givenQueryResponse();
        val expectedResponse = "[{\"title\":\"site1\",\"id\":\"1\",\"url\":\"https://example.com/1\"},{\"title\":\"site2\",\"id\":\"2\",\"url\":\"https://example.com/2\"}]";

        //When
        mvc.perform(
            get("/vocabulary/deims")
                .queryParam("query", QUERY)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));
    }

    @Test
    @SneakyThrows
    void getSitesNoQuery() {
        //Given
        givenQueryResponseNoQuery();
        val expectedResponse = "[{\"title\":\"site1\",\"id\":\"1\",\"url\":\"https://example.com/1\"},{\"title\":\"site2\",\"id\":\"2\",\"url\":\"https://example.com/2\"}]";

        //When
        mvc.perform(
            get("/vocabulary/deims")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));
    }


    @Test
    @SneakyThrows
    void ThrowSolrServerException() {
        //Given
        given(deimsService.query(QUERY)).willThrow(new SolrServerException("Test"));

        //When
        mvc.perform(
            get("/vocabulary/deims")
                .queryParam("query", QUERY)
        )
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"message\":\"Solr did not respond as expected\"}"));
    }
}
