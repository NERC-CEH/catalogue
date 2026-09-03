package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.util.List;
import java.util.stream.IntStream;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("Retrieving what ORCID and ROR say (dri-one #350 phase 3)")
class IdentityRetrieverTest {

    private static final String ORCID = "https://orcid.org/0000-0002-0394-2998";
    private static final String ROR = "https://ror.org/00pggkr55";
    private static final String ROR_API = "https://api.ror.org/v2/organizations/00pggkr55";
    private static final String FOAF = "http://xmlns.com/foaf/0.1/";

    private Dataset dataset;
    private MockRestServiceServer server;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        dataset = TDB2Factory.createDataset();
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    void tearDown() {
        dataset.close();
    }

    private IdentityRetriever retriever(String rorClientId) {
        return retriever(rorClientId, 200);
    }

    private IdentityRetriever retriever(String rorClientId, int rorUnidentifiedBudget) {
        return new IdentityRetriever(
            restTemplate, new DescriptionCache(dataset, Clock.systemUTC()),
            rorClientId, rorUnidentifiedBudget);
    }

    /** ORCID's real shape: the person, plus the profile document and account node. */
    private static String orcidResponse() {
        return """
            @prefix foaf: <http://xmlns.com/foaf/0.1/> .
            @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
            @prefix pav:  <http://purl.org/pav/> .
            @prefix prov: <http://www.w3.org/ns/prov#> .

            <%1$s>
                a foaf:Person ;
                rdfs:label "Claire Wood" ;
                foaf:givenName "Claire" ;
                foaf:familyName "Wood" ;
                foaf:account <%1$s#orcid-id> ;
                foaf:publications <%1$s#workspace-works> .

            <%1$s#orcid-id>
                a foaf:OnlineAccount ;
                foaf:accountName "0000-0002-0394-2998" .

            <https://pub.orcid.org/profile/0000-0002-0394-2998>
                a foaf:PersonalProfileDocument ;
                pav:lastUpdateOn "2026-04-13T18:48:30.934Z" ;
                foaf:primaryTopic <%1$s> .
            """.formatted(ORCID);
    }

    private static String rorResponse() {
        return """
            {
              "id": "https://ror.org/00pggkr55",
              "established": 2000,
              "names": [
                {"types": ["ror_display", "label"], "value": "UK Centre for Ecology & Hydrology"},
                {"types": ["acronym"], "value": "UKCEH"},
                {"types": ["alias"], "value": "Canolfan Ecoleg a Hydroleg y DU"}
              ],
              "links": [
                {"type": "website", "value": "https://www.ceh.ac.uk/"},
                {"type": "wikipedia", "value": "https://en.wikipedia.org/wiki/X"}
              ],
              "external_ids": [
                {"type": "fundref", "preferred": "501100011027", "all": ["501100011027"]},
                {"type": "wikidata", "preferred": null, "all": ["Q5062417"]}
              ],
              "locations": [
                {"geonames_details": {"country_code": "GB", "country_name": "United Kingdom"}}
              ],
              "admin": {"created": {"date": "2018-11-14"}}
            }
            """;
    }

    @Nested
    @DisplayName("ORCID")
    class Orcid {

        @Test
        @DisplayName("the researcher's own name is taken, and nothing else in the record")
        void takesTheNameAndNothingElse() {
            server.expect(requestTo(ORCID))
                .andExpect(header("Accept", "text/turtle"))
                .andRespond(withSuccess(orcidResponse(), MediaType.valueOf("text/turtle")));

            val model = retriever("").describe(List.of(ORCID), IdentityRetriever.Authority.ORCID).model();
            val person = createResource(ORCID);

            server.verify();
            assertTrue(model.contains(person, RDF.type, createResource(FOAF + "Person")));
            assertTrue(model.contains(person, RDFS.label, "Claire Wood"));
            assertTrue(model.contains(person, createProperty(FOAF + "givenName"), "Claire"));
            assertTrue(model.contains(person, createProperty(FOAF + "familyName"), "Wood"));

            assertFalse(
                model.containsResource(createResource(ORCID + "#orcid-id")),
                "the account node is not a statement about the person"
            );
            assertFalse(
                model.containsResource(createResource("https://pub.orcid.org/profile/0000-0002-0394-2998")),
                "nor is the profile document or its update history"
            );
            assertThat("only the four statements about the person", model.size(), is(4L));
        }

        @Test
        @DisplayName("no client id header is sent to ORCID, which does not use one")
        void noClientIdForOrcid() {
            server.expect(requestTo(ORCID))
                .andExpect(headerDoesNotExist("Client-Id"))
                .andRespond(withSuccess(orcidResponse(), MediaType.valueOf("text/turtle")));

            retriever("some-client-id").describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);

            server.verify();
        }
    }

    @Nested
    @DisplayName("ROR")
    class Ror {

        @Test
        @DisplayName("the official name, its aliases, and the identifiers that make the graph join up")
        void mapsTheJsonRecord() {
            server.expect(requestTo(ROR_API))
                .andRespond(withSuccess(rorResponse(), MediaType.APPLICATION_JSON));

            val model = retriever("").describe(List.of(ROR), IdentityRetriever.Authority.ROR).model();
            val organisation = createResource(ROR);

            server.verify();
            assertTrue(model.contains(organisation, RDF.type, createResource(FOAF + "Organization")));
            assertTrue(model.contains(organisation, RDFS.label, "UK Centre for Ecology & Hydrology"),
                "the display name");
            assertTrue(model.contains(organisation, SKOS.altLabel, "UKCEH"),
                "the acronym, which is one of the spellings our records use");
            assertTrue(model.contains(organisation, SKOS.altLabel, "Canolfan Ecoleg a Hydroleg y DU"),
                "and the aliases");
            assertFalse(
                model.listStatements().toList().stream().anyMatch(
                    statement -> statement.getPredicate().getURI().contains("ns/org#")),
                "org:alternateName is not a real property, and the Organization Ontology "
                    + "namespace is http rather than https; aliases belong on skos:altLabel"
            );
            assertTrue(model.contains(organisation, createProperty(FOAF + "homepage"),
                createResource("https://www.ceh.ac.uk/")));
            assertTrue(model.contains(organisation, OWL.sameAs,
                    createResource("https://doi.org/10.13039/501100011027")),
                "the Crossref funder id links an organisation to the funder DOIs on grants");
        }

        @Test
        @DisplayName("a cross-reference with no preferred value still comes through")
        void fallsBackToTheFirstIdentifier() {
            server.expect(requestTo(ROR_API))
                .andRespond(withSuccess(rorResponse(), MediaType.APPLICATION_JSON));

            val model = retriever("").describe(List.of(ROR), IdentityRetriever.Authority.ROR).model();

            assertTrue(
                model.contains(createResource(ROR), OWL.sameAs,
                    createResource("http://www.wikidata.org/entity/Q5062417")),
                "UKCEH's wikidata id has no preferred value, and it is the most useful "
                    + "cross-reference in the record: 2,064 wikidata entities are already in the graph"
            );
        }

        @Test
        @DisplayName("a configured client id is sent, since ROR requires one from Q3 2026")
        void sendsTheClientId() {
            server.expect(requestTo(ROR_API))
                .andExpect(header("Client-Id", "our-client-id"))
                .andRespond(withSuccess(rorResponse(), MediaType.APPLICATION_JSON));

            retriever("our-client-id").describe(List.of(ROR), IdentityRetriever.Authority.ROR);

            server.verify();
        }

        @Test
        @DisplayName("the v2 endpoint is addressed explicitly, since the mapping reads the v2 shape")
        void pinsTheApiVersion() {
            server.expect(requestTo(ROR_API))
                .andRespond(withSuccess(rorResponse(), MediaType.APPLICATION_JSON));

            retriever("").describe(List.of(ROR), IdentityRetriever.Authority.ROR);

            // The expectation is the assertion: an unversioned URL would not match.
            server.verify();
        }
    }

    @Nested
    @DisplayName("When an authority pushes back")
    class PushBack {

        private static java.util.List<String> organisations(int count) {
            return IntStream.range(0, count)
                .mapToObj(i -> "https://ror.org/org%02d".formatted(i))
                .toList();
        }

        @Test
        @DisplayName("a 429 stops the run asking that authority for anything more")
        void rateLimitStopsTheRun() {
            // Exactly one request is expected for 100 organisations: the 429
            // ends the run's dealings with ROR, so a second request finds no
            // expectation and throws.
            server.expect(once(), requestTo(org.hamcrest.Matchers.startsWith(
                    "https://api.ror.org/v2/organizations/")))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

            retriever("", 40).describe(organisations(100), IdentityRetriever.Authority.ROR);

            server.verify();
        }

        @Test
        @DisplayName("the entities it never got to are deferred, so the graph is held back")
        void rateLimitedEntitiesAreDeferred() {
            server.expect(once(), requestTo(org.hamcrest.Matchers.startsWith(
                    "https://api.ror.org/v2/organizations/")))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

            val described = retriever("", 40)
                .describe(organisations(100), IdentityRetriever.Authority.ROR);

            assertThat(
                "the 100th organisation was never asked about, so a later run will get it",
                described.isComplete(), is(false)
            );
            assertThat(described.deferred(), is(99));
        }

        @Test
        @DisplayName("a failed request costs budget, so a failing authority is not hammered")
        void failuresConsumeBudget() {
            // The budget used to count successes, so a run against a failing
            // authority made a request for every entity however small the
            // budget was -- the one situation the budget exists for.
            IntStream.range(0, 40).forEach(i ->
                server.expect(requestTo(org.hamcrest.Matchers.startsWith(
                        "https://api.ror.org/v2/organizations/")))
                    .andRespond(withServerError()));

            retriever("", 40).describe(organisations(100), IdentityRetriever.Authority.ROR);

            // 40 expectations for 100 organisations: a 41st request would throw.
            server.verify();
        }

        @Test
        @DisplayName("a 5xx is transient, so it holds the graph back")
        void serverErrorIsTransient() {
            server.expect(requestTo(ORCID)).andRespond(withServerError());

            val described = retriever("").describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);

            assertThat(described.transientFailures(), is(1));
            assertThat(described.isComplete(), is(false));
        }

        @Test
        @DisplayName("a 404 is definitive, so it must not hold the graph back for ever")
        void notFoundIsDefinitive() {
            // A mistyped ORCID in a record 404s today and will 404 for ever.
            // Blocking on it would freeze the graph permanently.
            server.expect(requestTo(ORCID)).andRespond(withStatus(HttpStatus.NOT_FOUND));

            val described = retriever("").describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);

            assertThat(described.transientFailures(), is(0));
            assertThat(described.deferred(), is(0));
            assertThat("nothing a later run can do, so the rest may still publish",
                described.isComplete(), is(true));
        }

        @Test
        @DisplayName("a response that is not the RDF we asked for is transient, not silent")
        void unparseableResponseIsTransient() {
            // This is the shape an unfollowed redirect had: a 200 whose body is
            // a short piece of HTML. It used to vanish at log.debug, which
            // logging.level.root=warn does not emit at all.
            server.expect(requestTo(ORCID))
                .andRespond(withSuccess("<html><body>Moved</body></html>", MediaType.TEXT_HTML));

            val described = retriever("").describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);

            assertThat(described.transientFailures(), is(1));
        }

        @Test
        @DisplayName("an entity with a stored copy is not counted against the run")
        void aStoredCopyMeansNothingIsMissing() {
            server.expect(requestTo(ORCID))
                .andRespond(withSuccess(orcidResponse(), MediaType.valueOf("text/turtle")));
            val retriever = retriever("");
            retriever.describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);

            val laterServer = MockRestServiceServer.createServer(restTemplate);
            laterServer.expect(requestTo(ORCID)).andRespond(withServerError());
            val aged = new DescriptionCache(dataset,
                Clock.fixed(java.time.Instant.now().plus(java.time.Duration.ofDays(30)),
                    java.time.ZoneOffset.UTC));

            val described = new IdentityRetriever(restTemplate, aged, "", 200)
                .describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);

            assertThat(
                "the stale copy fills the gap, so the graph loses nothing and may publish",
                described.isComplete(), is(true)
            );
        }
    }

    @Nested
    @DisplayName("Being a considerate client")
    class RateLimits {

        @Test
        @DisplayName("a second run takes the cached copy instead of asking again")
        void cachedOnTheSecondRun() {
            server.expect(once(), requestTo(ORCID))
                .andRespond(withSuccess(orcidResponse(), MediaType.valueOf("text/turtle")));

            val retriever = retriever("");
            retriever.describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);
            val second = retriever.describe(List.of(ORCID), IdentityRetriever.Authority.ORCID).model();

            // once() means a second request fails verification.
            server.verify();
            assertTrue(second.contains(createResource(ORCID), RDFS.label, "Claire Wood"));
        }

        @Test
        @DisplayName("without a client id, ROR is asked no more than its configured budget in one run")
        void unidentifiedRorRunIsBudgeted() {
            // Exactly 40 expectations for 100 organisations, against a budget of
            // 40: fewer requests than that leaves an expectation unmet and
            // verify() fails, more finds no expectation and throws. So this pins
            // the budget from both sides.
            val many = IntStream.range(0, 100)
                .mapToObj(i -> "https://ror.org/org%02d".formatted(i))
                .toList();
            IntStream.range(0, 40).forEach(i ->
                server.expect(requestTo(org.hamcrest.Matchers.startsWith(
                        "https://api.ror.org/v2/organizations/")))
                    .andRespond(withSuccess(rorResponse(), MediaType.APPLICATION_JSON)));

            retriever("", 40).describe(many, IdentityRetriever.Authority.ROR);

            server.verify();
        }

        @Test
        @DisplayName("a client id lifts the budget past the whole set, so one run fills it")
        void identifiedRorRunIsNotBudgetedToTheLowLimit() {
            val many = IntStream.range(0, 100)
                .mapToObj(i -> "https://ror.org/org%02d".formatted(i))
                .toList();
            IntStream.range(0, 100).forEach(i ->
                server.expect(requestTo(org.hamcrest.Matchers.startsWith(
                        "https://api.ror.org/v2/organizations/")))
                    .andRespond(withSuccess(rorResponse(), MediaType.APPLICATION_JSON)));

            // The unidentified budget is deliberately set below the set size, to
            // show that it is the client id and not the budget doing the work.
            retriever("our-client-id", 40).describe(many, IdentityRetriever.Authority.ROR);

            server.verify();
        }

        @Test
        @DisplayName("the configured budget is high enough for a first fill to finish before it goes stale")
        void configuredBudgetConverges() throws Exception {
            // A budget that cannot fill the set inside MAX_AGE never fills it at
            // all: the entities fetched on the first run are stale again before
            // the last ones are reached, so every subsequent run spends its
            // budget refetching the head of the list. At 561 organisations and a
            // fortnight, 40 a run — the figure that matches ROR's unidentified
            // rate limit — is just under the line, which is why the property
            // exists and why lowering it is not a free choice.
            val organisations = 561;
            val properties = new java.util.Properties();
            try (var in = getClass().getResourceAsStream("/application.properties")) {
                properties.load(in);
            }
            val budget = Integer.parseInt(properties.getProperty("ror.unidentifiedRequestsPerRun"));
            val runsToFill = (organisations + budget - 1) / budget;

            assertTrue(
                runsToFill < IdentityRetriever.MAX_AGE.toDays(),
                () -> "%d a run needs %d daily runs to fetch %d organisations, but they go stale after %d"
                    .formatted(budget, runsToFill, organisations, IdentityRetriever.MAX_AGE.toDays())
            );
        }

        @Test
        @DisplayName("the entities the budget did not reach are reported, not silently dropped")
        void deferredEntitiesAreReported() {
            val many = IntStream.range(0, 100)
                .mapToObj(i -> "https://ror.org/org%02d".formatted(i))
                .toList();
            IntStream.range(0, 40).forEach(i ->
                server.expect(requestTo(org.hamcrest.Matchers.startsWith(
                        "https://api.ror.org/v2/organizations/")))
                    .andRespond(withSuccess(rorResponse(), MediaType.APPLICATION_JSON)));

            val described = retriever("", 40).describe(many, IdentityRetriever.Authority.ROR);

            assertThat(
                "the caller cannot tell a warm cache from a filling one without this",
                described.deferred(), is(60)
            );
        }

        @Test
        @DisplayName("nothing is deferred once every entity has been reached")
        void nothingDeferredWhenTheCacheIsWarm() {
            server.expect(once(), requestTo(ORCID))
                .andRespond(withSuccess(orcidResponse(), MediaType.valueOf("text/turtle")));

            val retriever = retriever("");
            retriever.describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);
            val second = retriever.describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);

            assertThat(second.deferred(), is(0));
        }

        @Test
        @DisplayName("an entity asked about and unreachable is not deferred, because a later run cannot help")
        void unreachableIsNotDeferred() {
            server.expect(requestTo(ORCID)).andRespond(withServerError());

            val described = retriever("").describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);

            assertThat(
                "deferring means not yet asked; this one was asked and had nothing to give",
                described.deferred(), is(0)
            );
        }

        @Test
        @DisplayName("an unreachable authority falls back to any copy held, however old")
        void staleBeatsNothing() {
            server.expect(once(), requestTo(ORCID))
                .andRespond(withSuccess(orcidResponse(), MediaType.valueOf("text/turtle")));

            // Fill the cache, then age it past the refresh limit and fail the refetch.
            new IdentityRetriever(restTemplate, new DescriptionCache(dataset, Clock.systemUTC()), "", 200)
                .describe(List.of(ORCID), IdentityRetriever.Authority.ORCID);

            val laterServer = MockRestServiceServer.createServer(restTemplate);
            laterServer.expect(requestTo(ORCID)).andRespond(withServerError());
            val aged = new DescriptionCache(dataset,
                Clock.fixed(java.time.Instant.now().plus(java.time.Duration.ofDays(30)), java.time.ZoneOffset.UTC));

            val model = new IdentityRetriever(restTemplate, aged, "", 200)
                .describe(List.of(ORCID), IdentityRetriever.Authority.ORCID).model();

            assertTrue(
                model.contains(createResource(ORCID), RDFS.label, "Claire Wood"),
                "a name from a month ago is still that person's name"
            );
        }

        @Test
        @DisplayName("an entity nothing can describe is simply absent")
        void unreachableAndUncachedIsAbsent() {
            server.expect(requestTo(ORCID)).andRespond(withServerError());

            val model = retriever("").describe(List.of(ORCID), IdentityRetriever.Authority.ORCID).model();

            assertThat(model.size(), is(0L));
        }
    }
}
