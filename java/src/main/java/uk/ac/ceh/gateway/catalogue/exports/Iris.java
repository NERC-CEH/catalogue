package uk.ac.ceh.gateway.catalogue.exports;

import org.apache.jena.irix.IRIx;

/**
 * Whether a string taken from outside can be used as an IRI.
 *
 * <p>Everything published in a source graph is built from data someone else
 * controls — an organisation's website as ROR recorded it, a keyword URL as a
 * depositor typed it — and a character that is merely awkward in a database is
 * illegal in an IRI. Two places that matters, both found in review:
 *
 * <ul>
 *   <li><b>Serialisation.</b> Jena writes a bad IRI with nothing louder than a
 *       {@code WARN}, so it reaches the endpoint. dri-one #344 is the standing
 *       reminder of what one unpublishable character in one record costs when
 *       the export's PUT is all-or-nothing.</li>
 *   <li><b>Query construction.</b> A concept URI is interpolated into a SPARQL
 *       {@code VALUES} clause. A {@code &#123;}, {@code |} or backslash there makes the
 *       whole query a syntax error, which returns nothing, which the
 *       publish-whole-or-not-at-all guard turns into a permanently frozen
 *       graph — one bad keyword in one record stopping a whole vocabulary.</li>
 * </ul>
 *
 * <p>Checking is all this does. Nothing is repaired or rewritten: dri-one #331
 * settled that URI quality is reported rather than corrected, and guessing what
 * a depositor meant by a malformed URI would be exactly that.
 */
final class Iris {

    private Iris() {
    }

    /**
     * @param candidate a string that is about to be used as an IRI
     * @return whether it can be, as an absolute reference. Relative and empty
     *         strings are rejected too: {@code IRIx.create} accepts both, but
     *         neither identifies anything in a published graph.
     */
    static boolean isPublishable(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        try {
            return IRIx.create(candidate).isReference();
        } catch (Exception ex) {
            // IRIException for the illegal characters, and defensively anything
            // else: this is a predicate, and it may not throw.
            return false;
        }
    }
}
