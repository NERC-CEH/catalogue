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
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.templateHelpers.ContactUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FundingUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.KeywordUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FormatUri;
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
    private DocumentIndexingService linkIndex;
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
        configuration.setSharedVariable("formatUris", new FormatUri());
        configuration.setSharedVariable("jena", jenaLookupService);
        // Every test but the ones about it starts with a complete Jena index
        lenient().when(linkIndex.isReady()).thenReturn(true);
        service = new CatalogueToTurtleService(
            catalogueService,
            configuration,
            listing,
            baseUri,
            linkIndex
        );
    }

    /**
     * dri-one #408: the daily prefetch first runs three minutes after start-up. Caching the EIDC
     * Turtle while the Jena index is still being rebuilt would serve /eidc/catalogue.ttl without
     * its record-to-record links for a day.
     */
    @Test
    void thePrefetchIsSkippedWhileTheJenaIndexIsNotReady() {
        // given
        given(linkIndex.isReady()).willReturn(false);

        // when
        service.fetchCatalogues();

        // then
        verify(catalogueService, never()).retrieve(anyString());
    }

    /**
     * dri-one #408: a request for the catalogue Turtle on a cache miss used to fill the cache too,
     * so one made while the index was being rebuilt cached the link-less Turtle until the next day.
     */
    @Test
    void aRequestWhileTheJenaIndexIsNotReadyIsServedButNotCached() {
        // given: not ready for the first request, ready from the second
        given(linkIndex.isReady()).willReturn(false, true);
        given(catalogueService.retrieve(catalogueKey)).willReturn(catalogue);

        // when
        val whileRebuilding = service.getBigTtl(catalogueKey);
        service.getBigTtl(catalogueKey);
        service.getBigTtl(catalogueKey);

        // then: the first is rendered and returned but not kept; the second is cached for the third
        assertTrue(whileRebuilding.isPresent());
        verify(catalogueService, times(2)).retrieve(catalogueKey);
    }

    @Test
    void turtleRenderedWhileARebuildStartedIsNotCached() {
        // given: ready when rendering starts, rebuilding by the time it finishes
        given(linkIndex.isReady()).willReturn(true, false, true);
        given(catalogueService.retrieve(catalogueKey)).willReturn(catalogue);

        // when
        service.getBigTtl(catalogueKey);
        service.getBigTtl(catalogueKey);

        // then
        verify(catalogueService, times(2)).retrieve(catalogueKey);
    }

    @Test
    void aRefreshWhileTheJenaIndexIsNotReadyKeepsTheEarlierCompleteCache() {
        // given: a complete entry cached while the index was ready
        given(catalogueService.retrieve(catalogueKey)).willReturn(catalogue);
        service.fetchCatalogues();
        given(linkIndex.isReady()).willReturn(false);

        // when
        service.refresh();
        val result = service.getBigTtl(catalogueKey);

        // then: served from the cache, without rendering again
        assertTrue(result.isPresent());
        verify(catalogueService, times(1)).retrieve(catalogueKey);
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

    /**
     * The record template already renders a non-geographic dataset as a
     * dcat:Dataset, but the type filter in front of it dropped all 140 of
     * EIDC's before they reached the template, so none were in Fuseki.
     */
    @Test
    void bigTtlExportsNonGeographicDatasets() {
        //Given
        val document = (GeminiDocument) new GeminiDocument()
            .setType("nonGeographicDataset")
            .setId("nongeorecord")
            .setUri(baseUri + "/id/nongeorecord")
            .setTitle("Non-geographic record");

        given(catalogueService.retrieve(catalogueKey)).willReturn(catalogue);
        given(listing.getLatestPublicDocumentsOfCatalogue(catalogueKey)).willReturn(List.of(document));

        //When
        val ttl = service.getBigTtl(catalogueKey).orElseThrow();

        //Then
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(ttl), baseUri + "/", Lang.TTL);

        val record = createResource(baseUri + "/id/nongeorecord");
        assertTrue(model.contains(
            createResource(baseUri + "/" + catalogueKey + "/documents"),
            createProperty("http://www.w3.org/ns/dcat#resource"),
            record
        ));
        assertTrue(model.contains(
            record,
            createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            createResource("http://www.w3.org/ns/dcat#Dataset")
        ));
    }
}
