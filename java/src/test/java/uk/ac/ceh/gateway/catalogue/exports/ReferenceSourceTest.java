package uk.ac.ceh.gateway.catalogue.exports;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The four mappings of dri-one #350 phase 4, each against the authority's real
 * response.
 *
 * <p>The fixtures are captured from the live APIs rather than written by hand,
 * because the two subtlest things about this phase are properties of the real
 * data and would be invisible in a fixture I invented: the subject IRI an
 * authority uses is not the one the catalogue holds, and a GeoNames feature is
 * almost entirely multilingual aliases.
 */
@DisplayName("Describing the works, places and sites records cite (dri-one #350 phase 4)")
class ReferenceSourceTest {

    private static final String GN = "http://www.geonames.org/ontology#";
    private static final String WGS84 = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    private static final String BIBO = "http://purl.org/ontology/bibo/";

    @SneakyThrows
    private static String fixture(String name) {
        try (val in = ReferenceSourceTest.class.getResourceAsStream("/exports/" + name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Proves what is published survives the export's all-or-nothing PUT. */
    private static void assertRoundTrips(Model model) {
        val writer = new StringWriter();
        RDFDataMgr.write(writer, model, Lang.TURTLE);
        val reparsed = ModelFactory.createDefaultModel();
        RDFDataMgr.read(reparsed, new StringReader(writer.toString()), null, Lang.TURTLE);
        assertThat("serialised output must re-parse", reparsed.size(), is(model.size()));
    }

    @Nested
    @DisplayName("Works, by DOI")
    class Works {

        private static final String DOI = "https://doi.org/10.1016/j.scitotenv.2019.134044";
        private final DoiSource source = new DoiSource();

        @Test
        @DisplayName("the paper is described under the IRI the catalogue holds, not Crossref's")
        void statementsAreReSubjected() {
            val model = source.describe(DOI, fixture("crossref-work.ttl"));

            assertTrue(
                model.contains(createResource(DOI), DCTerms.title,
                    "Combined NOx and noise pollution from road traffic in Trabzon, Turkey"),
                "Crossref describes this as http://dx.doi.org/10.1016/... -- copying its "
                    + "statements as they arrive would describe an IRI nothing cites"
            );
            assertFalse(
                model.containsResource(createResource("http://dx.doi.org/10.1016/j.scitotenv.2019.134044")),
                "and the dx.doi.org form should not appear in the graph at all"
            );
            assertRoundTrips(model);
        }

        @Test
        @DisplayName("the journal is named, not merely pointed at")
        void journalIsNamed() {
            val model = source.describe(DOI, fixture("crossref-work.ttl"));
            val journal = createResource("https://id.crossref.org/issn/0048-9697");

            assertTrue(model.contains(createResource(DOI), DCTerms.isPartOf, journal));
            assertTrue(
                model.contains(journal, DCTerms.title, "Science of The Total Environment"),
                "otherwise a consumer needs a second lookup to find out what it was published in"
            );
            assertTrue(model.contains(journal, createProperty(BIBO + "issn"), "0048-9697"));
        }

        @Test
        @DisplayName("the authors are left out, so a second population of person nodes is not minted")
        void authorsAreNotImported() {
            val model = source.describe(DOI, fixture("crossref-work.ttl"));

            assertTrue(
                model.listStatements().toList().stream().noneMatch(statement ->
                    statement.toString().contains("id.crossref.org/contributor")),
                "these carry a name and no ORCID, so they could not be joined to the people "
                    + "phase 3 publishes -- #334 spent its effort consolidating person nodes, "
                    + "not multiplying them"
            );
            assertFalse(model.contains(createResource(DOI), DCTerms.creator));
        }

        @Test
        @DisplayName("our own DOIs are not asked about, since they resolve to us")
        void ownDoisAreExcluded() {
            assertFalse(source.describes("https://doi.org/10.5285/aabbccdd-1234-5678-9012-aabbccddeeff"));
            assertTrue(source.describes(DOI));
        }

        @Test
        @DisplayName("a response about a different DOI contributes nothing")
        void wrongResponseContributesNothing() {
            // The pipeline caches per entity, so a mapper that accepted any
            // response would cache one paper's description under another's IRI.
            val model = source.describe(
                "https://doi.org/10.1111/some.other.paper", fixture("crossref-work.ttl"));

            assertThat(model.size(), is(0L));
        }
    }

    @Nested
    @DisplayName("Places, by GeoNames id")
    class Places {

        private static final String FEATURE = "https://sws.geonames.org/2635167";
        private final GeoNamesSource source = new GeoNamesSource();

        @Test
        @DisplayName("RDF is fetched from about.rdf, since the IRI itself serves HTML")
        void requestUrlIsTheDocument() {
            assertThat(source.requestUrl(FEATURE), is(FEATURE + "/about.rdf"));
            assertThat("and a trailing slash must not produce a double one",
                source.requestUrl(FEATURE + "/"), is(FEATURE + "/about.rdf"));
        }

        @Test
        @DisplayName("the feature is described under the catalogue's slashless IRI")
        void statementsAreReSubjected() {
            val model = source.describe(FEATURE, fixture("geonames-feature.rdf"));

            assertTrue(
                model.contains(createResource(FEATURE), createProperty(GN + "name"), "United Kingdom"),
                "GeoNames' own subject carries a trailing slash the catalogue's IRI lacks"
            );
            assertFalse(
                model.containsResource(createResource(FEATURE + "/")),
                "the slashed form should not appear: nothing in the catalogue refers to it"
            );
            assertRoundTrips(model);
        }

        @Test
        @DisplayName("what makes a place joinable is kept")
        void structuralPropertiesAreKept() {
            val model = source.describe(FEATURE, fixture("geonames-feature.rdf"));
            val feature = createResource(FEATURE);

            assertTrue(model.contains(feature, createProperty(GN + "countryCode"), "GB"));
            assertTrue(model.contains(feature, createProperty(WGS84 + "lat"), "54.75844"));
            assertTrue(model.contains(feature, createProperty(WGS84 + "long"), "-2.69531"));
            assertTrue(model.contains(feature, createProperty(GN + "featureCode"),
                createResource("https://www.geonames.org/ontology#A.PCLI")));
            assertTrue(model.contains(feature, createProperty(GN + "parentFeature"),
                createResource("https://sws.geonames.org/11812257/")));
        }

        @Test
        @DisplayName("the 241 multilingual aliases are not, because 254 features of them would cost a fifth of the store")
        void aliasesAreDropped() {
            val model = source.describe(FEATURE, fixture("geonames-feature.rdf"));

            assertFalse(model.contains(createResource(FEATURE), createProperty(GN + "alternateName")));
            assertFalse(model.contains(createResource(FEATURE), createProperty(GN + "officialName")));
            assertThat(
                "a feature should cost a handful of triples, not hundreds: the real record "
                    + "holds 133 official names for the United Kingdom alone",
                model.size(), lessThan(20L)
            );
        }

        @Test
        @DisplayName("document links are not facts about the place")
        void documentLinksAreDropped() {
            val model = source.describe(FEATURE, fixture("geonames-feature.rdf"));
            val feature = createResource(FEATURE);

            assertFalse(model.contains(feature, createProperty(GN + "childrenFeatures")),
                "this points at contains.rdf, which states nothing on its own");
            assertFalse(model.contains(feature, createProperty(GN + "neighbouringFeatures")));
            assertFalse(model.contains(feature, createProperty(GN + "locationMap")),
                "and this is an HTML page");
        }
    }

    @Nested
    @DisplayName("Sites, by DEIMS id")
    class Sites {

        private static final String SITE = "https://deims.org/b7f692ef-10b0-432e-ab42-a3e9b764c5cc";
        private final DeimsSource source = new DeimsSource();

        @Test
        @DisplayName("the site is described from its API record")
        void mapsTheRecord() {
            val model = source.describe(SITE, fixture("deims-site.json"));
            val site = createResource(SITE);

            assertTrue(model.contains(site, RDFS.label, "Allt a'Mharcaidh - United Kingdom"));
            assertTrue(model.contains(site, DCTerms.type, "Stationary land-based site"));
            assertTrue(model.contains(site, DCTerms.created, "1992"));
            assertRoundTrips(model);
        }

        @Test
        @DisplayName("no contact details are published, whatever DEIMS holds")
        void noContactDetailsArePublished() {
            // The fixture is the live record with the addresses and person names
            // replaced -- it still carries three of each, so this assertion is
            // about the mapping and not about a fixture that happens to be clean.
            assertTrue(fixture("deims-site.json").contains("contact@example.invalid"),
                "the fixture must still contain contacts for this test to mean anything");

            val model = source.describe(SITE, fixture("deims-site.json"));
            val published = model.listStatements().toList().toString();

            assertFalse(published.contains("@example.invalid"),
                "#348 removed 2,429 addresses from this export; another authority's API "
                    + "is not a loophole for putting them back");
            assertFalse(published.contains("A Person"), "nor the people they belong to");
        }

        @Test
        @DisplayName("the representative point is published latitude-first despite WKT being the reverse")
        void coordinatesAreNotTransposed() {
            val model = source.describe(SITE, fixture("deims-site.json"));
            val site = createResource(SITE);

            // POINT (-3.843425 57.114196) is longitude then latitude. Reading it
            // positionally would place a Cairngorms site off the Somali coast.
            assertTrue(model.contains(site, createProperty(WGS84 + "lat"), "57.114196"));
            assertTrue(model.contains(site, createProperty(WGS84 + "long"), "-3.843425"));
        }

        @Test
        @DisplayName("the networks a site belongs to are linked and named")
        void networksAreLinked() {
            val model = source.describe(SITE, fixture("deims-site.json"));
            val network = createResource("https://deims.org/networks/1aa7ccb2-a14b-43d6-90ac-5e0a6bc1d65b");

            assertTrue(model.contains(createResource(SITE), DCTerms.isPartOf, network));
            assertTrue(model.contains(network, RDFS.label, "ILTER"));
        }

        @Test
        @DisplayName("only site URIs are claimed, since the host also serves networks and activities")
        void onlySitesAreClaimed() {
            assertTrue(source.describes(SITE));
            assertFalse(source.describes("https://deims.org/networks/1aa7ccb2-a14b-43d6-90ac-5e0a6bc1d65b"),
                "the sites API would 404 on this");
            assertFalse(source.describes("https://deims.org/about"));
        }

        @Test
        @DisplayName("a response that is not JSON yields nothing rather than throwing")
        void unreadableResponseYieldsNothing() {
            assertThat(source.describe(SITE, "<html>not json</html>").size(), is(0L));
        }
    }

    @Nested
    @DisplayName("Grants, by GtR reference")
    class Grants {

        private static final String GRANT = "https://gtr.ukri.org/projects?ref=NE/R016429/1";
        private final GtrSource source = new GtrSource();

        @Test
        @DisplayName("the reference is searched for, since ?ref= is silently ignored")
        void requestUrlUsesTheSearchParameter() {
            assertThat(
                source.requestUrl(GRANT),
                is("https://gtr.ukri.org/gtr/api/projects?q=NE%2FR016429%2F1")
            );
        }

        @Test
        @DisplayName("the grant is described from its own project record")
        void mapsTheProject() {
            val model = source.describe(GRANT, fixture("gtr-project.json"));
            val grant = createResource(GRANT);

            assertTrue(model.contains(grant, RDFS.label,
                "UK Status, Change and Projections of the Environment (UK-SCaPE)"));
            assertTrue(model.contains(grant, DCTerms.identifier, "NE/R016429/1"));
            assertTrue(model.contains(grant, DCTerms.contributor, "NERC"));
            assertTrue(model.contains(grant, DCTerms.subject, "Water Quality"),
                "GtR's own classification of what the project is about");
            assertRoundTrips(model);
        }

        @Test
        @DisplayName("an unfiltered response describes nothing rather than the wrong grant")
        void unfilteredResponseIsRefused() {
            // This fixture is what /gtr/api/projects?ref=NE/R016429/1 really
            // returns: HTTP 200, totalSize 158712, and page one of every
            // project GtR holds. Taking the first would have published
            // AH/V01241X/1's title as this grant's, and cached it for 60 days.
            val body = fixture("gtr-unfiltered.json");
            assertTrue(body.contains("AH/V01241X/1"),
                "the fixture must contain the wrong grant for this test to mean anything");

            val model = source.describe(GRANT, body);

            assertThat(
                "position in a search result says nothing about identity",
                model.size(), is(0L)
            );
        }

        @Test
        @DisplayName("a project whose reference does not match exactly is not accepted")
        void nearMissIsRefused() {
            // ?q= is a search, so a longer reference sharing a prefix is exactly
            // the kind of thing it could return.
            val model = source.describe(
                "https://gtr.ukri.org/projects?ref=NE/R016429/2", fixture("gtr-project.json"));

            assertThat(model.size(), is(0L));
        }

        @Test
        @DisplayName("the investigators are left out, as with Crossref's contributors")
        void investigatorsAreNotImported() {
            val model = source.describe(GRANT, fixture("gtr-project.json"));

            assertTrue(
                model.listStatements().toList().stream()
                    .noneMatch(statement -> statement.toString().contains("/persons/")),
                "a third population of person nodes with no ORCID could not be joined to "
                    + "the people phase 3 publishes"
            );
        }

        @Test
        @DisplayName("a reference-less URI is not claimed")
        void referencelessUriIsNotClaimed() {
            assertFalse(source.describes("https://gtr.ukri.org/projects?ref="));
            assertFalse(source.describes("https://gtr.ukri.org/"));
            assertTrue(source.describes(GRANT));
        }
    }

    @Nested
    @DisplayName("Every source")
    class EverySource {

        private static java.util.stream.Stream<ReferenceSource> sources() {
            return java.util.stream.Stream.of(
                new DoiSource(), new GeoNamesSource(), new DeimsSource(), new GtrSource());
        }

        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("sources")
        @DisplayName("declares a graph, a title, and a budget that converges inside its refresh age")
        void budgetConverges(ReferenceSource source) {
            assertTrue(Iris.isPublishable(source.graph()), "the graph name must be a usable IRI");
            assertFalse(source.title() == null || source.title().isBlank());

            // The rule phase 3 learned: a first fill must finish comfortably
            // inside maxAge, or the entities fetched first go stale before the
            // last are reached and the tail is never described at all.
            val entities = 882;
            val runsToFill = (entities + source.requestsPerRun() - 1) / source.requestsPerRun();
            assertTrue(
                runsToFill < source.maxAge().toDays(),
                () -> "%s needs %d daily runs for the largest set in this phase, but its "
                    .formatted(source.getClass().getSimpleName(), runsToFill)
                    + "descriptions go stale after " + source.maxAge().toDays()
            );
        }

        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("sources")
        @DisplayName("claims only its own IRIs")
        void claimsOnlyItsOwn(ReferenceSource source) {
            assertFalse(source.describes("https://catalogue.ceh.ac.uk/id/abc"));
            assertFalse(source.describes("https://orcid.org/0000-0002-0394-2998"));
            assertFalse(source.describes("https://ror.org/00pggkr55"));
            assertFalse(source.describes("https://gtr.ukri.org/person/x"));
        }
    }
}
