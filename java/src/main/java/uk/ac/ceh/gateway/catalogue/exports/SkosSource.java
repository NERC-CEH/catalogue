package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What the three fetching vocabularies have in common: everything except where
 * to ask.
 *
 * <p>Phase 1 of dri-one #350 published the labels the application already held
 * locally. These fetch the rest — definitions, alternate labels, and the
 * broader/narrower hierarchy — from the authorities themselves, for the
 * vocabularies that publish it in a form we can read.
 *
 * <h2>Only what is said about the concept</h2>
 *
 * <p>An authority's response describes more than the concept asked for. NVS
 * returns registry provenance and mappings to other vocabularies; AGROVOC
 * returns its own subvocabulary structure. Republishing all of it would mean
 * carrying blank-node structures and assertions about third-party URIs that
 * nobody asked us to mirror, and would make the graph's size a function of
 * whatever the authority happens to include.
 *
 * <p>So the retrieved model is reduced to {@link #PUBLISHED} — the SKOS
 * properties that describe a concept — with the concept itself as subject.
 * Everything else is discarded.
 */
abstract class SkosSource implements VocabularySource {

    /**
     * The properties worth republishing. Deliberately a whitelist: an authority
     * may add anything to its own records, and a graph that mirrors all of it
     * stops being a description of the concepts the catalogue references.
     */
    private static final Set<Property> PUBLISHED = Set.of(
        SKOS.prefLabel, SKOS.altLabel, SKOS.definition, SKOS.note,
        SKOS.notation, SKOS.broader, SKOS.narrower, SKOS.related, SKOS.inScheme
    );

    /** The same for all three: these really are SKOS concept descriptions. */
    @Override
    public String description() {
        return "Concept descriptions as published by the authority: preferred and alternate labels, "
            + "definitions, notations and the broader/narrower hierarchy.";
    }

    @Override
    public List<String> vocabularies() {
        return List.of(SKOS.getURI());
    }

    /**
     * Splits the response per concept.
     *
     * <p>Each concept is read from the response as a whole but extracted
     * separately, which matters for both transports. Dereferencing gives one
     * response per concept, so only that response may describe it. The SPARQL
     * CONSTRUCT names every concept in the batch, so its single document
     * legitimately describes all of them — but even there the per-concept
     * extraction matters, because a concept appears in that document as the
     * object of its neighbours' {@code skos:broader} whether or not the store
     * held anything about it.
     *
     * <p>A parse failure is deliberately not caught: an unreadable body is an
     * error page or a partial response, so it must count as transient and hold
     * the graph back rather than being remembered as the authority holding
     * nothing.
     */
    @Override
    public Map<String, Model> describe(List<String> batch, String body) {
        val response = ModelFactory.createDefaultModel();
        RDFDataMgr.read(response, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
            Lang.TURTLE);

        val byConcept = new LinkedHashMap<String, Model>();
        for (val conceptUri : batch) {
            byConcept.put(conceptUri, describeOne(response, conceptUri));
        }
        return byConcept;
    }

    /**
     * The published SKOS statements about one concept, and its type.
     *
     * <p>Nothing is written unless at least one such statement was found. The
     * test used to be {@code containsResource}, which is true when the concept
     * appears <em>anywhere</em> in the source — including as the object of
     * someone else's {@code skos:broader}. Being mentioned by a neighbour is not
     * being described, and treating it as such made an empty extraction look
     * like a successful retrieval, so the type triple alone was cached over a
     * good description.
     */
    private static Model describeOne(Model response, String conceptUri) {
        val description = ModelFactory.createDefaultModel();
        val concept = response.getResource(conceptUri);
        val published = response.listStatements(concept, null, (RDFNode) null).toList().stream()
            .filter(statement -> PUBLISHED.contains(statement.getPredicate()))
            // A literal, or a URI: never a blank node, which would drag the
            // authority's internal structure in behind it.
            .filter(statement -> !statement.getObject().isAnon())
            .toList();
        if (published.isEmpty()) {
            return description;
        }
        description.add(description.getResource(conceptUri), RDF.type, SKOS.Concept);
        published.forEach(description::add);
        return description;
    }
}
