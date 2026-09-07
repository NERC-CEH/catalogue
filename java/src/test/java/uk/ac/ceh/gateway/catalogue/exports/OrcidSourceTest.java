package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.apache.jena.vocabulary.RDF.type;
import static org.apache.jena.vocabulary.RDFS.label;

@DisplayName("What ORCID says about a researcher")
class OrcidSourceTest {

    private static final String ORCID = "https://orcid.org/0000-0002-0394-2998";
    private static final String FOAF = "http://xmlns.com/foaf/0.1/";

    private final OrcidSource source = new OrcidSource(500);

    /** ORCID's own RDF, including the parts that are not about the person. */
    private static String response() {
        return """
            @prefix foaf: <http://xmlns.com/foaf/0.1/> .
            @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
            @prefix pav:  <http://purl.org/pav/> .

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

    @Test
    @DisplayName("is asked for by content negotiation on the ORCID itself")
    void request() {
        val request = source.request(List.of(ORCID));

        assertThat(request.uri().toString(), is(ORCID));
        assertThat(request.accept(), is("text/turtle"));
        assertThat("ORCID uses no client id, unlike ROR", request.headers(), is(anEmptyMap()));
    }

    @Test
    @DisplayName("the researcher's own name is taken, and nothing else in the record")
    void takesTheNameAndNothingElse() {
        val model = source.describe(List.of(ORCID), response()).get(ORCID);
        val person = createResource(ORCID);

        assertTrue(model.contains(person, type, createResource(FOAF + "Person")));
        assertTrue(model.contains(person, label, "Claire Wood"));
        assertTrue(model.contains(person, createProperty(FOAF + "givenName"), "Claire"));
        assertTrue(model.contains(person, createProperty(FOAF + "familyName"), "Wood"));

        assertFalse(model.containsResource(createResource(ORCID + "#orcid-id")),
            "the account node is not a statement about the person");
        assertFalse(
            model.containsResource(createResource("https://pub.orcid.org/profile/0000-0002-0394-2998")),
            "nor is the profile document or its update history");
        assertThat("only the four statements about the person", model.size(), is(4L));
    }

    @Test
    @DisplayName("a record with no name still yields the type, as it always has")
    void typeOnlyWhenThereIsNoName() {
        val turtle = """
            @prefix foaf: <http://xmlns.com/foaf/0.1/> .
            <%s> a foaf:Person .
            """.formatted(ORCID);

        val model = source.describe(List.of(ORCID), turtle).get(ORCID);

        assertThat("pre-existing behaviour: the type is asserted whether or not a name was found",
            model.size(), is(1L));
    }

    @Test
    @DisplayName("a response that is not the RDF we asked for is thrown, not swallowed")
    void unreadableResponseThrows() {
        // The distinction matters. An error page served with a 200 is not ORCID
        // saying it holds nothing about this researcher, so it must count as
        // transient and hold the graph back -- which is what AuthorityRetriever
        // does with anything thrown from here. Swallowing it would cache a
        // negative and publish a nameless person.
        assertThrows(Exception.class,
            () -> source.describe(List.of(ORCID), "<html>Service Unavailable</html>"));
    }

    @Test
    @DisplayName("an ORCID's account node is not mistaken for a person")
    void describesOnlyPeople() {
        assertTrue(source.describes(ORCID));
        assertFalse(source.describes(ORCID + "#orcid-id"));
        assertFalse(source.describes("https://ror.org/00pggkr55"));
    }

    @Test
    @DisplayName("the budget fills the referenced set well inside the fortnight it may age")
    void budgetConverges() {
        // 500 a run fills the 2,125 people referenced today in five runs, against
        // a 14-day refresh window. The ceiling is 500 x 14 = 7,000; past that the
        // graph stalls permanently, so headroom is worth asserting.
        assertThat(source.requestsPerRun(), is(500));
        assertThat("a first fill must finish comfortably inside maxAge",
            source.requestsPerRun() * source.maxAge().toDays(), is(7000L));
    }
}
