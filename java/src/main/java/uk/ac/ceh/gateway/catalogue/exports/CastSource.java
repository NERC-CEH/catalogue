package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * The NERC CEH categories and subjects thesaurus.
 *
 * <p>The one fetching vocabulary that is also in the keyword harvest, so its
 * graph carries both the harvested labels and the definitions and hierarchy
 * retrieved here — which is why {@link VocabularyGraphService} assembles the two
 * rather than letting them fight over the same named graph.
 *
 * <p>The UKCEH server holds these concepts directly, so they are asked for in
 * one CONSTRUCT rather than dereferenced one at a time.
 */
@Profile("exports")
@Component
class CastSource extends SkosSource {

    private static final String PREFIX = "http://onto.nerc.ac.uk/CAST/";

    private final String endpoint;

    CastSource(@Value("${ukceh.sparql.endpoint}") String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String graph() {
        return PREFIX;
    }

    @Override
    public String title() {
        return "CAST, the NERC CEH categories and subjects thesaurus";
    }

    @Override
    public boolean describes(String iri) {
        return iri.startsWith(PREFIX);
    }

    @Override
    public Duration maxAge() {
        return Duration.ofDays(7);
    }

    @Override
    public int requestsPerRun() {
        // Queries, not concepts. One covers the whole referenced set.
        return 4;
    }

    /**
     * How many concepts one query may name.
     *
     * <p>A cap is new. The method this replaced interpolated <em>every</em>
     * referenced concept into a single {@code VALUES} clause with no limit —
     * fine at the 32 concepts it was written for, and an unbounded query string
     * as the referenced set grows. 500 keeps today's single-query behaviour
     * while bounding it, and matches what Wikidata is already held to.
     */
    @Override
    public int batchSize() {
        return 500;
    }

    @Override
    public Request request(List<String> batch) {
        val values = batch.stream().map(uri -> "<" + uri + ">").collect(joining(" "));
        val query = """
            CONSTRUCT { ?concept ?p ?o }
            WHERE {
              VALUES ?concept { %s }
              GRAPH ?g { ?concept ?p ?o }
            }
            """.formatted(values);
        // Encoded here rather than handed over as a URI template. The method
        // this replaced passed "?query={query}" and relied on RestTemplate
        // expanding it, which is the same mechanism that turned a GtR grant
        // reference's %2F into %252F -- see Request for why that is no longer
        // expressible.
        return Request.get(endpoint + "?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8),
            "text/turtle");
    }
}
