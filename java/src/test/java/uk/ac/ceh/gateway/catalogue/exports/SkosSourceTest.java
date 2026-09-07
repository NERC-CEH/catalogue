package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("What a vocabulary authority says about its own concepts")
class SkosSourceTest {

    private static final String NVS = "http://vocab.nerc.ac.uk/";
    private static final String CAST = "http://onto.nerc.ac.uk/CAST/";
    private static final String CONCEPT = NVS + "collection/P07/current/CFSN0381/";
    private static final String NEIGHBOUR = NVS + "collection/P07/current/CFSN0382/";

    private final NvsSource nvs = new NvsSource();
    private final AgrovocSource agrovoc = new AgrovocSource();
    private final CastSource cast = new CastSource("https://vocabs.ceh.ac.uk/sparql");

    /** What NVS sends: the concept, plus a good deal that is not about it. */
    private static String response() {
        return """
            @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
            @prefix dcterms: <http://purl.org/dc/terms/> .
            @prefix owl: <http://www.w3.org/2002/07/owl#> .

            <%1$s>
                a skos:Concept ;
                skos:prefLabel "Nitrogen concentration"@en ;
                skos:altLabel "N concentration"@en ;
                skos:definition "The concentration of nitrogen."@en ;
                skos:notation "SDN:P07::CFSN0381" ;
                skos:broader <%2$s> ;
                skos:inScheme <http://vocab.nerc.ac.uk/collection/P07/current/> ;
                dcterms:date "2021-03-02" ;
                owl:sameAs <http://example.invalid/elsewhere> ;
                skos:related [ a skos:Concept ; skos:prefLabel "a blank node" ] .

            <%2$s>
                a skos:Concept ;
                skos:prefLabel "Something else"@en .
            """.formatted(CONCEPT, NEIGHBOUR);
    }

    @Nested
    @DisplayName("The extraction")
    class Extraction {

        @Test
        @DisplayName("keeps the SKOS properties that describe the concept, and its type")
        void keepsTheSkosDescription() {
            val model = nvs.describe(List.of(CONCEPT), response()).get(CONCEPT);
            val concept = createResource(CONCEPT);

            assertTrue(model.contains(concept, RDF.type, SKOS.Concept));
            assertTrue(model.contains(concept, SKOS.prefLabel, "Nitrogen concentration", "en"));
            assertTrue(model.contains(concept, SKOS.altLabel, "N concentration", "en"));
            assertTrue(model.contains(concept, SKOS.definition, "The concentration of nitrogen.", "en"));
            assertTrue(model.contains(concept, SKOS.notation, "SDN:P07::CFSN0381"));
            assertTrue(model.contains(concept, SKOS.broader, createResource(NEIGHBOUR)));
        }

        @Test
        @DisplayName("discards everything else the authority sent")
        void discardsTheRest() {
            val model = nvs.describe(List.of(CONCEPT), response()).get(CONCEPT);
            val concept = createResource(CONCEPT);

            assertFalse(model.contains(concept, createProperty("http://purl.org/dc/terms/date")),
                "NVS's registry provenance is not a description of the concept");
            assertFalse(model.contains(concept, createProperty("http://www.w3.org/2002/07/owl#sameAs")),
                "nor are its mappings to other vocabularies");
            assertFalse(model.containsResource(createResource(NEIGHBOUR + "x")));
            assertThat("and the neighbour's own label is not published as this concept's",
                model.contains(concept, SKOS.prefLabel, "Something else", "en"), is(false));
        }

        @Test
        @DisplayName("never follows a blank node into the authority's internal structure")
        void skipsBlankNodes() {
            val model = nvs.describe(List.of(CONCEPT), response()).get(CONCEPT);

            assertThat(model.listObjectsOfProperty(createResource(CONCEPT), SKOS.related)
                .toList().size(), is(0));
        }

        @Test
        @DisplayName("a concept only mentioned by a neighbour is not described")
        void mentionIsNotDescription() {
            // The test used to be containsResource, which is true when the
            // concept appears anywhere in the source -- including as the object
            // of someone else's skos:broader. Being mentioned by a neighbour is
            // not being described, and treating it as such cached a type triple
            // over a good description.
            val onlyMentioned = NVS + "collection/P07/current/CFSN9999/";
            val turtle = """
                @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
                <%s> a skos:Concept ; skos:broader <%s> .
                """.formatted(CONCEPT, onlyMentioned);

            val model = nvs.describe(List.of(onlyMentioned), turtle).get(onlyMentioned);

            assertThat(model.isEmpty(), is(true));
        }

        @Test
        @DisplayName("a response that is not RDF is thrown, so it counts as transient")
        void unreadableResponseThrows() {
            // Changed, and deliberately. The method this replaced logged at
            // debug and returned null, which logging.level.root=warn does not
            // emit -- so a vocabulary server serving error pages was invisible.
            assertThrows(Exception.class,
                () -> nvs.describe(List.of(CONCEPT), "<html>Service Unavailable</html>"));
        }
    }

    @Nested
    @DisplayName("How each authority is asked")
    class Transport {

        @Test
        @DisplayName("NVS and AGROVOC are dereferenced, one concept per request")
        void dereferenced() {
            assertThat(nvs.request(List.of(CONCEPT)).uri().toString(), is(CONCEPT));
            assertThat(nvs.request(List.of(CONCEPT)).accept(), is("text/turtle"));
            assertThat(nvs.batchSize(), is(1));
            assertThat(agrovoc.batchSize(), is(1));
        }

        @Test
        @DisplayName("CAST is one CONSTRUCT naming the whole batch")
        void castBatches() {
            val request = cast.request(List.of(CAST + "273", CAST + "276"));

            assertThat("the UKCEH server holds these directly, so asking 32 times would be wasteful",
                cast.batchSize(), is(500));
            assertThat(request.uri().toString(), containsString("https://vocabs.ceh.ac.uk/sparql?query="));
            // Encoded by the source, not handed over as a URI template: the
            // mechanism that turned a GtR grant reference's %2F into %252F.
            assertThat(request.uri().getQuery(), containsString("<" + CAST + "273>"));
            assertThat(request.uri().getQuery(), containsString("<" + CAST + "276>"));
        }

        @Test
        @DisplayName("each authority claims only its own concepts")
        void describesOnlyItsOwn() {
            assertTrue(nvs.describes(CONCEPT));
            assertFalse(nvs.describes(CAST + "273"));
            assertTrue(cast.describes(CAST + "273"));
            assertFalse(cast.describes(CONCEPT));
            assertTrue(agrovoc.describes("http://aims.fao.org/aos/agrovoc/c_1234"));
        }

        @Test
        @DisplayName("a thesaurus is refetched sooner than a person, since it can be revised")
        void maxAgeIsShorterThanAnIdentity() {
            // A researcher's name is settled; a thesaurus can gain a definition
            // or move a concept in its hierarchy on any release.
            assertThat(nvs.maxAge().toDays(), is(7L));
            assertThat("shorter than the fortnight ORCID and ROR get",
                nvs.maxAge().toDays() < new OrcidSource(500).maxAge().toDays(), is(true));
        }
    }
}
