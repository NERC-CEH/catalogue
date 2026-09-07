package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProvider.SourceGraph;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * The namespaces the source graphs use, and the one writer of their provenance.
 *
 * <p>Each provider used to compose its own {@code void:Dataset} header, and
 * {@code /.well-known/void} composed a second description of the same graph from
 * a hardcoded template. The two disagreed: the header written into the ORCID
 * graph called it identities as published by the authority, while the VoID
 * document called it concept labels and claimed a {@code skos:prefLabel}
 * partition that graph does not have. Writing both from one
 * {@link SourceGraph} is what stops that recurring.
 */
final class SourceGraphs {

    static final String VOID = "http://rdfs.org/ns/void#";
    static final String PROV = "http://www.w3.org/ns/prov#";
    private static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    /**
     * Vocabularies named in a {@link SourceGraph} declaration but not otherwise
     * held anywhere. The ones a {@link ReferenceSource} also builds predicate
     * URIs from stay with that source, where they read better.
     */
    static final String FOAF = "http://xmlns.com/foaf/0.1/";
    static final String SCHEMA = "http://schema.org/";
    static final String WDT = "http://www.wikidata.org/prop/direct/";

    /** The terms ORCID, ROR and Wikidata all release their public records under. */
    static final String CC0 = "https://creativecommons.org/publicdomain/zero/1.0/";

    private SourceGraphs() {
    }

    /**
     * Writes what this graph is, what it draws on, and when the copy was taken.
     *
     * <p>{@code dcterms:publisher} is asserted alongside a licence rather than
     * on its own: GeoNames' licence requires attribution and the graph is where
     * a consumer will look for it, and naming the publisher of a graph whose
     * terms we have not established would be claiming more than we know.
     */
    static void addProvenance(Model model, SourceGraph source, Clock clock) {
        val graph = model.getResource(source.graph());
        model.add(graph, RDF.type, model.getResource(VOID + "Dataset"));
        model.add(graph, DCTerms.title, source.title());
        model.add(graph, DCTerms.description, source.description());
        source.vocabularies().forEach(vocabulary ->
            model.add(graph, model.getProperty(VOID + "vocabulary"), model.getResource(vocabulary)));
        if (source.licence() != null) {
            model.add(graph, DCTerms.license, model.getResource(source.licence()));
            model.add(graph, DCTerms.publisher, source.title());
        }
        model.add(graph, model.getProperty(PROV + "generatedAtTime"),
            model.createTypedLiteral(
                Instant.now(clock).truncatedTo(ChronoUnit.SECONDS).toString(),
                XSD + "dateTime"));
    }
}
