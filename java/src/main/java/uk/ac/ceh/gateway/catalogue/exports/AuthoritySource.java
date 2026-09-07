package uk.ac.ceh.gateway.catalogue.exports;

import org.apache.jena.rdf.model.Model;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * One external authority the catalogue republishes, and how to ask it.
 *
 * <p>These have almost nothing in common beyond being cited: some publish RDF
 * and some JSON, one wants a different path from the URI it describes, two
 * describe a thing under a different IRI from the one the catalogue holds, and
 * two answer about five hundred entities at a time. What they share is the
 * pipeline around them — which IRIs to ask about, the cache in front, the per-run
 * budget, the classification of a failure, and the decision about whether a
 * graph is fit to publish — so that lives in {@link AuthorityRetriever} once,
 * and each authority contributes only what is genuinely specific to it.
 *
 * <h2>The mapper's contract</h2>
 *
 * <p>{@link #describe} must return statements <em>about the IRIs it was asked
 * about</em>. That is not a formality: Crossref describes a paper as
 * {@code http://dx.doi.org/10.1016/…} and GeoNames a place as
 * {@code https://sws.geonames.org/2635167/} — with a trailing slash — while the
 * catalogue's graph holds {@code https://doi.org/10.1016/…} and
 * {@code https://sws.geonames.org/2635167}. A mapper that copied statements as
 * they arrived would produce a graph describing IRIs nothing refers to, which is
 * worse than useless: it would look like data while joining to nothing.
 */
public interface AuthoritySource {

    /** The named graph this authority's descriptions are published to. */
    String graph();

    /** A human-readable name, for the VoID description. */
    String title();

    /** What the graph holds, in one line, for the VoID description. */
    String description();

    /**
     * The namespaces of the terms {@link #describe} actually emits.
     *
     * <p>Declared by the source because the source is the only thing that knows:
     * these graphs share no vocabulary at all — Crossref's works are
     * {@code dcterms} and {@code bibo}, GeoNames' places {@code gn} and
     * {@code wgs84_pos}, ORCID's people {@code foaf} — and the VoID description
     * once claimed SKOS for every one of them.
     */
    List<String> vocabularies();

    /**
     * The licence the authority publishes under, or null where we have not
     * established it. Stated only when known — claiming the wrong one would be
     * worse than claiming none, which is why the vocabulary graphs still carry
     * no {@code dcterms:license}.
     */
    default String licence() {
        return null;
    }

    /** Whether this authority is the one that describes the given IRI. */
    boolean describes(String iri);

    /**
     * How old a held description may be before it is fetched again. Per source
     * because the answers age at wildly different rates: a published paper's
     * title and journal are fixed for ever, whereas a thesaurus can move a
     * concept on any release.
     */
    Duration maxAge();

    /**
     * How many <em>requests</em> one export may make of this authority.
     *
     * <p>Requests, not entities — the two differ once {@link #batchSize} does.
     * Subject to a convergence rule either way: a first fill must finish
     * comfortably inside {@link #maxAge()}, or the entities fetched first go
     * stale before the last are reached and the tail is never described at all.
     */
    int requestsPerRun();

    /**
     * How many entities one request may ask about. One by default.
     *
     * <p>Wikidata is the source where asking per entity fails outright: a single
     * entity's RDF is 324 KB, so dereferencing the 2,064 entities the catalogue
     * references would move roughly 670 MB across the network to extract a
     * label, a description and a type. One CONSTRUCT naming 500 entities
     * measured at 10 seconds and 109 KB.
     */
    default int batchSize() {
        return 1;
    }

    /**
     * Where and how to ask about these entities.
     *
     * @param batch at most {@link #batchSize()} IRIs, never empty
     */
    Request request(List<String> batch);

    /**
     * @param batch the IRIs asked about, and the subjects the returned
     *              statements must be about
     * @param body  the authority's response
     * @return what the authority said, keyed by the IRI it is about. An IRI
     *         absent from the map, or mapped to an empty model, is one the
     *         authority was reached about and had nothing usable to say.
     */
    Map<String, Model> describe(List<String> batch, String body);
}
