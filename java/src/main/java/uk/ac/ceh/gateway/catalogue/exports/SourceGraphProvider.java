package uk.ac.ceh.gateway.catalogue.exports;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Something that publishes a named graph on behalf of an external authority.
 *
 * <p>The catalogue's own graph says almost nothing about the 11,600 external
 * URIs it references, because dri-one #320 forbids asserting record text onto a
 * shared identifier. What these publish is not record text: it is what each
 * authority says about its own entities, in a graph of its own, so attribution
 * is structural rather than conventional.
 *
 * <p>There is one implementation per kind of authority — vocabularies in
 * {@link VocabularyGraphService}, people and organisations in
 * {@link IdentityGraphService} — and both the export and the VoID description
 * work through this interface, so adding an authority does not mean touching
 * either of them.
 *
 * <p>Nothing published through this may ever be written into the catalogue's own
 * graph, and nothing record-derived may be written into a source graph. Keeping
 * that boundary is the whole point of the separation.
 */
public interface SourceGraphProvider {

    /**
     * One graph a provider publishes to.
     *
     * @param graph the named graph, which is also the authority's own namespace
     * @param title a human-readable name, for the VoID description
     */
    record SourceGraph(String graph, String title) {}

    /**
     * The graphs this provider publishes to, whether or not there is currently
     * anything to put in them.
     *
     * <p>Separate from {@link #graphs} on purpose: that reports what there is to
     * publish right now, whereas this declares what the endpoint offers, which
     * is what {@code /.well-known/void} advertises. One declaration, so the
     * description cannot drift from what is actually written.
     */
    List<SourceGraph> sourceGraphs();

    /**
     * @param referencedIris every IRI the catalogue's own graph refers to, so a
     *                       provider describes only entities something cites
     * @return the Turtle to publish, keyed by graph. A graph is absent rather
     *         than empty where there is nothing to say, so a previous run's
     *         content is left alone instead of being replaced with less.
     */
    Map<String, String> graphs(Set<String> referencedIris);
}
