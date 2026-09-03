package uk.ac.ceh.gateway.catalogue.exports;

import org.apache.jena.rdf.model.Model;

import java.time.Duration;

/**
 * One authority describing things the catalogue's records cite — a paper, a
 * grant, a place, a monitoring site — and how to ask it.
 *
 * <p>dri-one #350 phase 4. The four authorities have almost nothing in common
 * beyond being cited: two publish RDF and two publish JSON, one wants a
 * different path from the URI it describes, and two describe the thing under a
 * different IRI from the one the catalogue holds. What they do share is the
 * pipeline around them — which URIs to ask about, the cache in front, the
 * per-run budget, and the decision about whether a graph is fit to publish — so
 * that lives in {@link ReferenceRetriever} and {@link ReferenceGraphService}
 * once, and each authority contributes only what is genuinely specific to it.
 *
 * <h2>The mapper's contract</h2>
 *
 * <p>{@link #describe} is handed the IRI the catalogue holds and the authority's
 * response, and must return statements <em>about that IRI</em>. That is not a
 * formality: Crossref describes a paper as {@code http://dx.doi.org/10.1016/…}
 * and GeoNames a place as {@code https://sws.geonames.org/2635167/} — with a
 * trailing slash — while the catalogue's graph holds
 * {@code https://doi.org/10.1016/…} and {@code https://sws.geonames.org/2635167}
 * respectively. A mapper that copied statements as they arrived would produce a
 * graph describing IRIs nothing in the catalogue refers to, which is worse than
 * useless: it would look like data while joining to nothing.
 */
interface ReferenceSource {

    /** The named graph this authority's descriptions are published to. */
    String graph();

    /** A human-readable name, for the VoID description. */
    String title();

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
     * Where to ask about it. Frequently not the IRI itself: GeoNames serves RDF
     * only from {@code <iri>/about.rdf} and returns HTML for the IRI however it
     * is asked, and DEIMS has a separate API path.
     */
    String requestUrl(String iri);

    /** What to ask for. */
    String accept();

    /**
     * @param iri  the IRI the catalogue holds, and the subject the returned
     *             statements must be about
     * @param body the authority's response
     * @return what the authority says about it, or an empty model if it said
     *         nothing usable
     */
    Model describe(String iri, String body);

    /**
     * How old a held description may be before it is fetched again. Per source
     * because the answers age at wildly different rates: a published paper's
     * title and journal are fixed for ever, whereas a monitoring site's record
     * is actively curated.
     */
    Duration maxAge();

    /**
     * How many of this authority's entities one export may fetch.
     *
     * <p>Subject to the same convergence rule as the identity budgets, and for
     * the same reason: a first fill must finish comfortably inside
     * {@link #maxAge()}, or the entities fetched first go stale before the last
     * are reached and the tail is never described at all.
     */
    int requestsPerRun();
}
