package uk.ac.ceh.gateway.catalogue.exports;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("What Wikidata says about a subject concept")
class WikidataSourceTest {

    private static final String PREFIX = "http://www.wikidata.org/entity/";
    private static final String ARACHNID = PREFIX + "Q1358";
    private static final String MURRE = PREFIX + "Q21062";
    private static final String WOOD = PREFIX + "Q663181";
    private static final String TAXON = PREFIX + "Q16521";
    private static final String WDT = "http://www.wikidata.org/prop/direct/";
    private static final String SCHEMA = "http://schema.org/";

    private final WikidataSource source =
        new WikidataSource("https://query.wikidata.org/sparql", "ukceh-catalogue-export/1.0", 500, 8);

    @SneakyThrows
    private static String fixture() {
        try (val in = WikidataSourceTest.class.getResourceAsStream("/exports/wikidata-batch.ttl")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Nested
    @DisplayName("Which URIs are worth asking about")
    class Describes {

        @Test
        @DisplayName("an entity id is claimed")
        void entityIdsAreClaimed() {
            assertTrue(source.describes(ARACHNID));
            assertTrue(source.describes(PREFIX + "Q1"));
        }

        @Test
        @DisplayName("the two malformed URIs in production are not")
        void malformedUrisAreNot() {
            // A bare namespace with no id, and one with a stray opening
            // parenthesis. Neither will ever resolve, and a VALUES clause would
            // accept both silently -- remembering an empty answer for a typo.
            assertFalse(source.describes("http://www.wikidata.org/entity"));
            assertFalse(source.describes(PREFIX + "(Q1054552"));
        }

        @Test
        @DisplayName("nor is anything else under the namespace")
        void otherShapesAreNot() {
            assertFalse(source.describes(PREFIX + "P31"));
            assertFalse(source.describes(PREFIX + "Q0"));
            assertFalse(source.describes("https://www.wikidata.org/entity/Q1358"),
                "https is not the form Wikidata mints");
        }
    }

    @Nested
    @DisplayName("The query")
    class Query {

        @Test
        @DisplayName("is one POST naming the whole batch, identifying itself as the service requires")
        void oneQueryForTheBatch() {
            val request = source.request(List.of(ARACHNID, MURRE));

            assertThat(request.method(), is(HttpMethod.POST));
            assertThat(request.uri().toString(), is("https://query.wikidata.org/sparql"));
            assertThat(request.contentType(), is("application/sparql-query"));
            assertThat("the Query Service blocks clients that do not identify themselves",
                request.headers(), hasKey(HttpHeaders.USER_AGENT));
        }

        @Test
        @DisplayName("names every entity asked for")
        void namesEveryEntity() {
            val body = source.request(List.of(ARACHNID, MURRE)).body();

            assertThat(body, containsString("<" + ARACHNID + ">"));
            assertThat(body, containsString("<" + MURRE + ">"));
        }

        @Test
        @DisplayName("asks for the taxon name, which is the only name a tenth of these have")
        void asksForTheTaxonName() {
            // 10 of a 500-entity sample have no English label at all, and the
            // scientific name is the only name half of them have.
            assertThat(source.request(List.of(ARACHNID)).body(), containsString("wdt:P225"));
        }

        @Test
        @DisplayName("batches at 500 and spends its budget in queries, not entities")
        void batchesAndBudgets() {
            assertThat(source.batchSize(), is(500));
            assertThat("eight queries covers the referenced set five times over",
                source.requestsPerRun(), is(8));
        }
    }

    @Nested
    @DisplayName("What each entity ends up with")
    class Extraction {

        @Test
        @DisplayName("its own statements, and nothing from its neighbours")
        void ownStatementsOnly() {
            val described = source.describe(List.of(ARACHNID, MURRE, WOOD), fixture());

            val arachnid = described.get(ARACHNID);
            assertTrue(arachnid.contains(createResource(ARACHNID), SKOS.altLabel, "arachnid", "en"));
            assertFalse(
                arachnid.contains(createResource(MURRE), RDFS.label, "Common Murre", "en"),
                "one entity's statements must never be published as another's");
        }

        @Test
        @DisplayName("the type is named, not just pointed at")
        void theTypeIsNamed() {
            // Without the one-hop expansion, P31 points at an entity nothing in
            // the graph names, so a consumer reading "instance of Q16521" learns
            // nothing.
            val described = source.describe(List.of(ARACHNID), fixture()).get(ARACHNID);

            assertTrue(described.contains(createResource(ARACHNID),
                createProperty(WDT + "P31"), createResource(TAXON)));
            assertTrue(described.contains(createResource(TAXON), RDFS.label, "taxon", "en"),
                "and the type carries its own label");
        }

        @Test
        @DisplayName("a description and every alias Wikidata holds in English")
        void descriptionAndAliases() {
            val described = source.describe(List.of(MURRE), fixture()).get(MURRE);
            val murre = createResource(MURRE);

            assertTrue(described.contains(murre, createProperty(SCHEMA + "description"),
                "species of bird, auk", "en"));
            assertThat("the common-name synonyms a keyword search would otherwise miss",
                described.listObjectsOfProperty(murre, SKOS.altLabel).toList().size(), is(3));
        }

        @Test
        @DisplayName("an entity the response says nothing about gets an empty description")
        void absentEntityIsEmpty() {
            // Asked, and Wikidata holds nothing under that id -- a deleted or
            // merged entity. The retriever remembers that rather than asking
            // again every run.
            val described = source.describe(List.of(PREFIX + "Q99999999"), fixture());

            assertThat(described.get(PREFIX + "Q99999999").isEmpty(), is(true));
        }

        @Test
        @DisplayName("a response that is not RDF is thrown, since 500 entities ride on the query")
        void unreadableResponseThrows() {
            assertThrows(Exception.class,
                () -> source.describe(List.of(ARACHNID), "<html>Service Unavailable</html>"));
        }
    }
}
