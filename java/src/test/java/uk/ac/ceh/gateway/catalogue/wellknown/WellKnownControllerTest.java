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
import uk.ac.ceh.gateway.catalogue.services.FusekiExportService;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStats;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStatsService;

import java.util.Map;

import lombok.val;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProvider;
import java.io.StringReader;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic", "exports"})
@DisplayName("WellKnownController")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = "fuseki.catalogueIds=eidc")
class WellKnownControllerTest extends AbstractMvcTest {

    @MockitoBean private CatalogueService catalogueService;
    @MockitoBean private ProfileService profileService;
    @MockitoBean private FusekiExportService fusekiExportService;
    @Autowired private Configuration configuration;
    @Autowired private VoidStatsService voidStatsService;
    @Autowired private java.util.List<SourceGraphProvider> sourceGraphProviders;

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

    @SneakyThrows
    @Test
    @DisplayName("the VoID description is parseable Turtle, not merely a string that contains the right words")
    void voidDescriptionParses() {
        givenFreemarkerConfiguration();
        givenEidcCatalogue();

        val body = mvc.perform(get("/.well-known/void"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        val model = ModelFactory.createDefaultModel();
        // Throws on malformed Turtle, which is the point: a discovery document
        // nothing can parse advertises nothing.
        RDFDataMgr.read(model, new StringReader(body), null, Lang.TURTLE);
        assertTrue(model.size() > 0, "the description should assert something");
    }

    @SneakyThrows
    @Test
    @DisplayName("every named graph the endpoint holds is advertised, the catalogue's and each authority's")
    void advertisesEveryNamedGraph() {
        givenFreemarkerConfiguration();
        givenEidcCatalogue();

        val body = mvc.perform(get("/.well-known/void"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        val model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(body), null, Lang.TURTLE);

        val named = model.listObjectsOfProperty(
                createProperty("http://www.w3.org/ns/sparql-service-description#name"))
            .toList().stream()
            .map(node -> node.asResource().getURI())
            .toList();

        assertThat(
            "VoID cannot express named graphs, so this is the service description's job",
            named,
            hasItem("https://catalogue.ceh.ac.uk")
        );
        // Not a fixed count: that would break on every phase of dri-one #350
        // that adds an authority, for no benefit. What makes the loop below
        // meaningful is simply that there is something in it.
        val declared = sourceGraphProviders.stream()
            .flatMap(provider -> provider.sourceGraphs().stream())
            .toList();
        assertThat(
            "an empty declaration would make the assertions below vacuous",
            declared, is(not(empty()))
        );
        for (val source : declared) {
            assertThat(named, hasItem(source.graph()));
        }
    }

    @SneakyThrows
    @Test
    @DisplayName("each authority graph is described as a dataset in its own right")
    void describesEachAuthorityGraph() {
        givenFreemarkerConfiguration();
        givenEidcCatalogue();

        val body = mvc.perform(get("/.well-known/void"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        val model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(body), null, Lang.TURTLE);

        val gemet = createResource("http://www.eionet.europa.eu/gemet/");
        assertTrue(
            model.contains(gemet, createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                createResource("http://rdfs.org/ns/void#Dataset")),
            "a consumer should be able to discover what the graph holds, not just that it exists"
        );
        assertTrue(
            model.listObjectsOfProperty(gemet, createProperty("http://purl.org/dc/terms/license"))
                .toList().isEmpty(),
            "no licence is claimed for an authority's content until one has been established"
        );
    }

    private void givenEidcCatalogue() {
        given(catalogueService.retrieve("eidc"))
            .willReturn(Catalogue.builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc.ac.uk")
                .contactUrl("")
                .logo("eidc.png")
                .build());
    }
}
