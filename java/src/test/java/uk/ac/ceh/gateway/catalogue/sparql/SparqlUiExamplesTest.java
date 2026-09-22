package uk.ac.ceh.gateway.catalogue.sparql;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.jena.query.QueryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The example queries offered by the SPARQL UI.
 *
 * <p>They are hand-written SPARQL embedded in a template as arrays of JavaScript
 * string literals, and nothing checked them. A typo in one is not a broken
 * build, it is a visitor clicking "Datasets, their licence and their funder" and
 * getting a parse error from the endpoint — the worst place to find out, since
 * these exist to demonstrate that the endpoint works.
 *
 * <p>So this extracts each one exactly as the page assembles it and puts it
 * through the same parser Fuseki uses.
 */
@DisplayName("The SPARQL UI's example queries")
class SparqlUiExamplesTest {

    private static final Path TEMPLATE = Path.of("../templates/html/sparql-ui.ftlh");

    /** Matches one {@code query: [ ... ]} block. */
    private static final Pattern BLOCK = Pattern.compile(
        "query:\\s*\\[(.*?)\\n\\s*\\]", Pattern.DOTALL);
    private static final Pattern LITERAL = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");

    /**
     * Every example, assembled the way the page assembles it.
     *
     * <p>The template concatenates a JavaScript constant into some lines, as
     * {@code "  GRAPH <" + CATALOGUE_GRAPH + "> {"}. That is substituted first,
     * so the line becomes a single literal — extracting the pieces separately
     * would silently produce {@code GRAPH <> } and test a query no visitor ever
     * runs.
     */
    @SneakyThrows
    private static List<String> examples() {
        var template = Files.readString(TEMPLATE)
            .replace("\" + CATALOGUE_GRAPH + \"", "https://catalogue.ceh.ac.uk");

        val queries = new ArrayList<String>();
        Matcher block = BLOCK.matcher(template);
        while (block.find()) {
            val lines = new ArrayList<String>();
            Matcher literal = LITERAL.matcher(block.group(1));
            while (literal.find()) {
                lines.add(literal.group(1));
            }
            queries.add(String.join("\n", lines));
        }
        return queries;
    }

    @Test
    @DisplayName("are all found, so this test cannot pass by extracting nothing")
    void examplesAreFound() {
        val examples = examples();

        assertThat("the template offers six; an extraction bug must not look like success",
            examples.size(), greaterThanOrEqualTo(6));
        assertTrue(examples.stream().allMatch(query -> query.contains("SELECT")
                || query.contains("CONSTRUCT") || query.contains("DESCRIBE")),
            "each should be a query rather than a fragment of one");
    }

    @Test
    @DisplayName("all parse, so no visitor is handed a syntax error")
    void allExamplesParse() {
        for (val example : examples()) {
            assertDoesNotThrow(
                () -> QueryFactory.create(example),
                () -> "this example does not parse:\n" + example
            );
        }
    }

    @Test
    @DisplayName("the named graphs they use are ones the endpoint actually holds")
    void graphsAreReal() {
        // A query naming a graph that does not exist returns zero rows and no
        // error, which is a worse failure than a syntax error because it looks
        // like an answer. These are the graphs the export publishes.
        val known = List.of(
            "https://catalogue.ceh.ac.uk",
            "http://www.wikidata.org/entity/",
            "https://orcid.org/",
            "https://ror.org/",
            "https://doi.org/",
            "https://sws.geonames.org/",
            "https://deims.org/",
            "https://gtr.ukri.org/",
            "http://vocab.nerc.ac.uk/",
            "http://onto.nerc.ac.uk/CAST/",
            "http://aims.fao.org/aos/agrovoc/",
            "http://www.eionet.europa.eu/gemet/",
            "http://vocabs.lter-europe.net/EnvThes/",
            "https://digital.ceh.ac.uk/vocab/ra/",
            "https://digital.ceh.ac.uk/vocab/fdri/"
        );
        val named = Pattern.compile("GRAPH\\s+<([^>?]+)>");

        for (val example : examples()) {
            Matcher graph = named.matcher(example);
            while (graph.find()) {
                assertThat(
                    "an example names a graph the export does not publish: " + graph.group(1),
                    known, hasItem(graph.group(1))
                );
            }
        }
    }

    @Test
    @DisplayName("a vocabulary graph and Wikidata's are each demonstrated by a cross-graph join")
    void authorityGraphsAreDemonstrated() {
        // Weaker versions of this test passed for the wrong reason. Asserting
        // merely that *some* example joins across graphs was already satisfied
        // by the GEMET one, so it said nothing about whether the graph a given
        // phase added is advertised anywhere -- and these examples are the only
        // place a visitor learns the source graphs exist at all.
        //
        // Named rather than counted, so adding examples is free and removing one
        // of these is a visible decision rather than a silent loss.
        for (val authority : List.of(
            "http://www.eionet.europa.eu/gemet/", "http://www.wikidata.org/entity/")) {
            assertTrue(
                examples().stream().anyMatch(example ->
                    example.contains("GRAPH <" + authority + ">")
                        && example.contains("GRAPH <https://catalogue.ceh.ac.uk>")),
                "no example joins the catalogue graph to " + authority
                    + ", so nothing on the page shows a visitor what it is for"
            );
        }
    }
}
