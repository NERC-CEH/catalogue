package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProvider.SourceGraph;

import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
    static final String GN = "http://www.geonames.org/ontology#";
    static final String WGS84 = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    static final String BIBO = "http://purl.org/ontology/bibo/";

    /** The terms ORCID, ROR and Wikidata all release their public records under. */
    static final String CC0 = "https://creativecommons.org/publicdomain/zero/1.0/";

    /**
     * The conventional prefix for each namespace a source graph can declare.
     *
     * <p>Prefixes are cosmetic — an unmapped namespace simply appears in full —
     * but they are what makes a graph readable to someone who curls it. Derived
     * from {@link SourceGraph#vocabularies()} rather than hand-listed per
     * service, so a source that starts emitting a new vocabulary gets its prefix
     * by saying so once.
     */
    private static final Map<String, String> PREFIXES = Map.ofEntries(
        Map.entry(FOAF, "foaf"),
        Map.entry(SKOS.getURI(), "skos"),
        Map.entry(OWL.getURI(), "owl"),
        Map.entry(RDFS.getURI(), "rdfs"),
        Map.entry(DCTerms.getURI(), "dcterms"),
        Map.entry(GN, "gn"),
        Map.entry(WGS84, "wgs84_pos"),
        Map.entry(BIBO, "bibo"),
        Map.entry(SCHEMA, "schema"),
        Map.entry(WDT, "wdt"),
        Map.entry(VOID, "void"),
        Map.entry(PROV, "prov"),
        Map.entry(XSD, "xsd")
    );

    /** On every source graph, because {@link #addProvenance} writes them. */
    private static final List<String> ALWAYS = List.of(
        DCTerms.getURI(), VOID, PROV, XSD, RDFS.getURI());

    private SourceGraphs() {
    }

    /**
     * How many distinct entities a graph describes.
     *
     * <p>Counted from the graph rather than taken from the number of IRIs the
     * run asked about, because the two differ: an entity the authority
     * definitively does not hold is excused from the completeness check, so a
     * complete run can legitimately describe fewer entities than it set out to.
     * Reporting the number asked about would quietly overstate what was
     * published.
     *
     * <p>Must be called before {@link #addProvenance}, which adds the graph's
     * own {@code void:Dataset} as a subject. Blank nodes are not counted: they
     * are structure within an entity's description, not entities of their own.
     */
    static int entities(Model model) {
        return model.listSubjects().filterKeep(Resource::isURIResource).toSet().size();
    }

    /**
     * Serialised by Jena rather than assembled as text, so literal escaping is
     * the parser's problem and not ours. A single unescaped backslash in one
     * hand-built literal took down every export for a week (dri-one #344).
     */
    static String serialise(Model model, SourceGraph source) {
        Stream.concat(ALWAYS.stream(), source.vocabularies().stream())
            .distinct()
            .forEach(namespace -> {
                val prefix = PREFIXES.get(namespace);
                if (prefix != null) {
                    model.setNsPrefix(prefix, namespace);
                }
            });
        val writer = new StringWriter();
        RDFDataMgr.write(writer, model, Lang.TURTLE);
        return writer.toString();
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
