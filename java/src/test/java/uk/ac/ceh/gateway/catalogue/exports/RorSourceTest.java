package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("What ROR says about an organisation")
class RorSourceTest {

    private static final String UKCEH = "https://ror.org/00pggkr55";
    private static final String FOAF = "http://xmlns.com/foaf/0.1/";

    private final RorSource source = new RorSource("", 200);

    private static String response() {
        return """
            {
              "id": "https://ror.org/00pggkr55",
              "established": 2000,
              "names": [
                {"lang": "en", "types": ["ror_display", "label"], "value": "UK Centre for Ecology & Hydrology"},
                {"lang": null, "types": ["acronym"], "value": "UKCEH"},
                {"lang": "cy", "types": ["alias"], "value": "Canolfan Ecoleg a Hydroleg y DU"},
                {"lang": "fr", "types": ["alias"], "value": "Centre britannique pour l'Ecologie et l'Hydrologie"}
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

    private Model describe(String body) {
        return source.describe(List.of(UKCEH), body).get(UKCEH);
    }

    /**
     * Literal objects as {@code value} or {@code value@tag}. Built from the
     * lexical form and the tag rather than from Jena's toString, which quotes
     * and prefixes -- the assertions below are about what ROR said, not about
     * how Jena renders it.
     */
    private static List<String> literalsOf(Model model, Property predicate) {
        return model.listObjectsOfProperty(createResource(UKCEH), predicate).toList().stream()
            .map(RDFNode::asLiteral)
            .map(literal -> literal.getLanguage().isEmpty()
                ? literal.getString()
                : literal.getString() + "@" + literal.getLanguage())
            .toList();
    }

    /** IRI objects, by their URI. */
    private static List<String> urisOf(Model model, Property predicate) {
        return model.listObjectsOfProperty(createResource(UKCEH), predicate).toList().stream()
            .map(node -> node.asResource().getURI()).toList();
    }

    @Test
    @DisplayName("the v2 endpoint is addressed explicitly, since the mapping reads the v2 shape")
    void request() {
        val request = source.request(List.of(UKCEH));

        assertThat(request.uri().toString(), is("https://api.ror.org/v2/organizations/00pggkr55"));
        assertThat(request.accept(), is("application/json"));
    }

    @Test
    @DisplayName("a configured client id is sent, since ROR requires one from Q3 2026")
    void clientIdIsSentWhenConfigured() {
        assertThat(new RorSource("an-id", 200).request(List.of(UKCEH)).headers(),
            is(Map.of("Client-Id", "an-id")));
        assertThat("and omitted rather than sent blank while registration is paused",
            source.request(List.of(UKCEH)).headers(), is(anEmptyMap()));
    }

    @Test
    @DisplayName("an unidentified client keeps the smaller budget it will be held to")
    void budgetDependsOnBeingIdentified() {
        // An identified client keeps 2,000 requests per 5 minutes; an
        // unidentified one drops to 50, so 200 a run is the deliberate ceiling
        // until a client id can be obtained.
        assertThat(source.requestsPerRun(), is(200));
        assertThat(new RorSource("an-id", 200).requestsPerRun(), is(600));
    }

    @Test
    @DisplayName("the display name is the label, the name, and a prefLabel")
    void displayNameIsAlsoAPrefLabel() {
        val model = describe(response());
        val organisation = createResource(UKCEH);
        val official = "UK Centre for Ecology & Hydrology";

        assertTrue(model.contains(organisation, RDFS.label, official, "en"));
        assertTrue(model.contains(organisation, createProperty(FOAF + "name"), official, "en"));
        assertThat("so an organisation looks up exactly as a vocabulary concept does",
            model.contains(organisation, SKOS.prefLabel, official, "en"), is(true));
    }

    @Test
    @DisplayName("every other name is an altLabel carrying the tag ROR recorded")
    void aliasesCarryTheirLanguageTag() {
        val model = describe(response());

        assertThat(literalsOf(model, SKOS.altLabel), containsInAnyOrder(
            "UKCEH",
            "Canolfan Ecoleg a Hydroleg y DU@cy",
            "Centre britannique pour l'Ecologie et l'Hydrologie@fr"));
        assertThat("untagged, the French name is not an alternative spelling of anything",
            literalsOf(model, SKOS.altLabel),
            not(hasItem("Centre britannique pour l'Ecologie et l'Hydrologie")));
    }

    @Test
    @DisplayName("a malformed language tag is dropped rather than published")
    void malformedLanguageTagIsDropped() {
        // Jena does not validate the tag, and a bad one serialises into Turtle
        // that will not re-parse -- which the export's all-or-nothing PUT turns
        // into a rejected graph.
        val model = describe("""
            {
              "id": "https://ror.org/00pggkr55",
              "names": [
                {"lang": "en gb", "types": ["ror_display"], "value": "UK Centre for Ecology & Hydrology"}
              ],
              "links": [], "external_ids": [], "locations": []
            }
            """);

        assertTrue(model.contains(createResource(UKCEH), RDFS.label,
            "UK Centre for Ecology & Hydrology"), "kept, but untagged");
    }

    @Test
    @DisplayName("a cross-reference with no preferred value falls back to its first")
    void crossReferenceFallsBack() {
        val model = describe(response());

        assertThat("UKCEH's wikidata preferred is null while its all list holds Q5062417",
            urisOf(model, OWL.sameAs), containsInAnyOrder(
                "https://doi.org/10.13039/501100011027",
                "http://www.wikidata.org/entity/Q5062417"));
    }

    @Test
    @DisplayName("an unusable cross-reference is dropped rather than published")
    void unusableIriIsDropped() {
        val model = describe("""
            {
              "id": "https://ror.org/00pggkr55",
              "names": [{"lang": "en", "types": ["ror_display"], "value": "Somewhere"}],
              "links": [], "locations": [],
              "external_ids": [{"type": "isni", "preferred": "0000 0001 2183 {bad}", "all": []}]
            }
            """);

        assertThat("Jena writes a bad IRI with only a WARN, and then it reaches the endpoint",
            urisOf(model, OWL.sameAs), is(List.of()));
        assertTrue(model.contains(createResource(UKCEH), RDFS.label, "Somewhere", "en"),
            "and the rest of the record still comes through");
    }

    @Test
    @DisplayName("where it is, when it started, and its website are taken")
    void takesTheRest() {
        val model = describe(response());

        assertThat(urisOf(model, DCTerms.spatial),
            is(List.of("http://publications.europa.eu/resource/authority/country/GB")));
        assertThat(urisOf(model, createProperty(FOAF + "homepage")),
            is(List.of("https://www.ceh.ac.uk/")));
        val established = model
            .listObjectsOfProperty(createResource(UKCEH), DCTerms.created).next().asLiteral();
        assertThat(established.getString(), is("2000"));
        assertThat("a bare year, not a date -- ROR gives only the year",
            established.getDatatypeURI(), is("http://www.w3.org/2001/XMLSchema#gYear"));
        assertThat("ROR's own administrative bookkeeping is left behind",
            model.contains(createResource(UKCEH), DCTerms.date, "2018-11-14"), is(false));
    }

    @Test
    @DisplayName("a response that is not the JSON we asked for is thrown, not swallowed")
    void unreadableResponseThrows() {
        // Transient, for the reason OrcidSourceTest records: an error page with a
        // 200 is not ROR saying it holds nothing about this organisation.
        assertThrows(Exception.class, () -> describe("<html>Service Unavailable</html>"));
    }

    @Test
    @DisplayName("only ror.org organisations are this source's concern")
    void describesOnlyOrganisations() {
        assertTrue(source.describes(UKCEH));
        assertFalse(source.describes("https://orcid.org/0000-0002-0394-2998"));
    }
}
