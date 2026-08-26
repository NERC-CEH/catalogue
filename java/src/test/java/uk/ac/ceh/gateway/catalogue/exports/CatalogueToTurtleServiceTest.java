package uk.ac.ceh.gateway.catalogue.exports;

import freemarker.template.Configuration;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.templateHelpers.ContactUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FundingUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.KeywordUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.LicenceUri;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabularySolrQueryService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.io.File;
import java.io.StringReader;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogueToTurtleServiceTest {
    private CatalogueToTurtleService service;

    @Mock
    CatalogueService catalogueService;
    @Mock
    MetadataListingService listing;
    @Mock
    JenaLookupService jenaLookupService;
    private static final String baseUri = "https://example.com";
    private static final String catalogueKey = "eidc";
    private static final String otherCatalogueKey = "other";

    private final Catalogue catalogue = Catalogue.builder()
        .id(catalogueKey)
        .title("Env Data Centre")
        .url(baseUri)
        .contactUrl("")
        .logo("eidc.png")
        .build();

    private final Catalogue otherCatalogue = Catalogue.builder()
        .id(otherCatalogueKey)
        .title("Env Data Centre")
        .url(baseUri)
        .contactUrl("")
        .logo("eidc.png")
        .build();

    private final Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

    @SneakyThrows
    @BeforeEach
    public void setup() {
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        val uriNormaliser = new UriNormaliser();
        configuration.setSharedVariable("uriNormaliser", uriNormaliser);
        configuration.setSharedVariable("contactUri", new ContactUri(uriNormaliser));
        configuration.setSharedVariable("fundingUri", new FundingUri(uriNormaliser));
        configuration.setSharedVariable(
            "keywordUri",
            new KeywordUri(uriNormaliser, org.mockito.Mockito.mock(KeywordVocabularySolrQueryService.class))
        );
        // Registered even though no fixture here currently carries a licence: this must mirror
        // FreemarkerConfig, or the first licence-bearing fixture added to this test fails with a
        // confusing "licenceUris is undefined" rather than anything to do with the change made.
        configuration.setSharedVariable("licenceUris", new LicenceUri());
        configuration.setSharedVariable("jena", jenaLookupService);
        service = new CatalogueToTurtleService(
            catalogueService,
            configuration,
            listing,
            baseUri
        );
    }

    @Test
    void getBigTtlWithoutPrefetch() {
        //Given
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(catalogue);

        //When
        val result = service.getBigTtl(catalogueKey);

        //Then
        verify(catalogueService, times(1)).retrieve(catalogueKey);
        assertTrue(result.map(
            ttl -> ttl.contains("<%s/documents>".formatted(catalogueKey))
        ).orElse(false));
    }

    @Test
    void getBigTtlWithPrefetch() {
        //Given
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(catalogue);

        //When
        service.fetchCatalogues();
        val result = service.getBigTtl(catalogueKey);

        //Then
        verify(catalogueService, times(1)).retrieve(catalogueKey);
        assertTrue(result.map(
            ttl -> ttl.contains("<%s/documents>".formatted(catalogueKey))
        ).orElse(false));
    }

    /**
     * dri-one #330: a manual export trigger must not publish a stale prefetched payload, so
     * {@code refresh()} has to rebuild the cache from the current documents every time it's called -
     * not just once, the way the cache would otherwise settle after the first {@code fetchCatalogues()}.
     */
    @Test
    void refreshRebuildsThePrefetchedCatalogueEachTimeItIsCalled() {
        //Given
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(catalogue);

        //When
        service.refresh();
        service.refresh();

        //Then
        verify(catalogueService, times(2)).retrieve(catalogueKey);
    }

    @Test
    void getBigTtlAfterRefreshServesTheFreshCacheWithoutRefetching() {
        //Given
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(catalogue);

        //When
        service.refresh();
        val result = service.getBigTtl(catalogueKey);

        //Then
        verify(catalogueService, times(1)).retrieve(catalogueKey);
        assertTrue(result.map(
            ttl -> ttl.contains("<%s/documents>".formatted(catalogueKey))
        ).orElse(false));
    }

    @Test
    void getOtherBigTtlWithoutPrefetch() {
        //Given
        given(catalogueService.retrieve(otherCatalogueKey))
            .willReturn(otherCatalogue);

        //When
        val result = service.getBigTtl(otherCatalogueKey);

        //Then
        verify(catalogueService, times(1)).retrieve(otherCatalogueKey);
        assertTrue(result.map(
            ttl -> ttl.contains("<%s/documents>".formatted(otherCatalogueKey))
        ).orElse(false));
    }

    @Test
    void getOtherBigTtlWithPrefetch() {
        //Given
        given(catalogueService.retrieve(anyString()))
            .willReturn(otherCatalogue);

        //When
        service.fetchCatalogues();
        val result = service.getBigTtl(otherCatalogueKey);

        //Then
        verify(catalogueService, times(2)).retrieve(anyString());
        assertTrue(result.map(
            ttl -> ttl.contains("<%s/documents>".formatted(otherCatalogueKey))
        ).orElse(false));
    }

    /**
     * The big TTL is the only path into the production triplestore, and it is
     * built by concatenating record templates after the catalogue header — so
     * the prefixed names a record emits, including the person nodes of dri-one
     * #319, only resolve if that header's {@code PREFIX :} covers them. Nothing
     * else parses the two together.
     */
    @Test
    void bigTtlPlacesPersonNodesInTheCatalogueNamespace() {
        //Given
        val author = ResponsibleParty.builder()
            .familyName("Wood")
            .givenName("Claire")
            .organisationName("UK Centre for Ecology & Hydrology")
            .build();
        val document = (GeminiDocument) new GeminiDocument()
            .setType("dataset")
            .setId("bigttlrecord")
            .setUri(baseUri + "/id/bigttlrecord")
            .setTitle("Big TTL record");
        document.setAuthors(List.of(author));

        given(catalogueService.retrieve(catalogueKey)).willReturn(catalogue);
        given(listing.getLatestPublicDocumentsOfCatalogue(catalogueKey)).willReturn(List.of(document));

        //When
        val ttl = service.getBigTtl(catalogueKey).orElseThrow();

        //Then
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(ttl), baseUri + "/", Lang.TTL);

        val creator = model.listObjectsOfProperty(
            createResource(baseUri + "/id/bigttlrecord"),
            createProperty("http://purl.org/dc/terms/creator")
        ).next().asResource();
        assertThat(creator.getURI(), matchesRegex(baseUri + "/id/person_[0-9a-f]{16}"));
        assertTrue(model.contains(
            creator,
            createProperty("http://xmlns.com/foaf/0.1/name"),
            model.createLiteral("Wood, C.")
        ));
    }
}
