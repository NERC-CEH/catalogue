package uk.ac.ceh.gateway.catalogue.wellknown;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStats;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStatsService;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("WellKnownController")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = "fuseki.catalogueIds=eidc")
class WellKnownControllerTest extends AbstractMvcTest {

    @MockitoBean private CatalogueService catalogueService;
    @MockitoBean private ProfileService profileService;
    @Autowired private Configuration configuration;
    @Autowired private VoidStatsService voidStatsService;

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
    }

    @SneakyThrows
    @Test
    @DisplayName("GET /.well-known/void returns per-catalogue VoID description as Turtle")
    void getVoidDescription() {
        //given
        givenFreemarkerConfiguration();
        given(catalogueService.retrieve("eidc"))
            .willReturn(Catalogue.builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc.ac.uk")
                .contactUrl("")
                .logo("eidc.png")
                .build());

        //when, then
        mvc.perform(get("/.well-known/void"))
            .andExpectAll(
                status().isOk(),
                content().contentType("text/turtle"),
                content().string(containsString("void:DatasetDescription")),
                content().string(containsString("void:Dataset")),
                content().string(containsString("eidc")),
                content().string(containsString("void:sparqlEndpoint"))
            );
    }

    @SneakyThrows
    @Test
    @DisplayName("GET /.well-known/void includes void:entities when stats are available")
    void getVoidDescriptionWithStats() {
        //given
        givenFreemarkerConfiguration();
        given(catalogueService.retrieve("eidc"))
            .willReturn(Catalogue.builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc.ac.uk")
                .contactUrl("")
                .logo("eidc.png")
                .build());
        voidStatsService.update("eidc", new VoidStats(
            1234L,
            50000L,
            Map.of("http://www.w3.org/ns/dcat#Dataset", 1234L)
        ));

        //when, then
        mvc.perform(get("/.well-known/void"))
            .andExpectAll(
                status().isOk(),
                content().string(containsString("void:entities 1234")),
                content().string(containsString("void:triples 50000")),
                content().string(containsString("void:classPartition")),
                content().string(containsString("void:propertyPartition"))
            );
    }
}
