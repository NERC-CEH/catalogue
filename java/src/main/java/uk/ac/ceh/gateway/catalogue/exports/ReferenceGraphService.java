package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Publishes what the authorities say about the works, grants, places and sites
 * the catalogue's records cite — dri-one #350 phase 4.
 *
 * <p>One named graph per authority, as in the earlier phases, so a consumer can
 * ask the catalogue graph what the catalogue asserts and GeoNames' graph what
 * GeoNames asserts without unpicking one from the other by predicate.
 *
 * <p>1,197 entities across the three authorities implemented here: 882 external
 * DOIs, 254 GeoNames features and 61 DEIMS sites. Grants are the fourth and are
 * not yet included — see {@link ReferenceSource} implementations for what is
 * registered.
 *
 * <h2>Same publishing rule as phase 3</h2>
 *
 * <p>The export writes each graph with a single PUT, which replaces it, so a
 * graph is published only when the run behind it is complete. A run that never
 * reached some entities, or met a rate limit or a timeout, would otherwise
 * replace a full graph with a partial one and then restore it over the
 * following days.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = "referenceRetriever")
public class ReferenceGraphService implements SourceGraphProvider {

    private static final String VOID = "http://rdfs.org/ns/void#";
    private static final String PROV = "http://www.w3.org/ns/prov#";
    private static final String GN = "http://www.geonames.org/ontology#";
    private static final String WGS84 = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    private static final String BIBO = "http://purl.org/ontology/bibo/";

    private final ReferenceRetriever referenceRetriever;
    private final Clock clock;

    /** @see VocabularyGraphService for why this annotation is needed. */
    @Autowired
    public ReferenceGraphService(ReferenceRetriever referenceRetriever) {
        this(referenceRetriever, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the clock in the provenance header. */
    ReferenceGraphService(ReferenceRetriever referenceRetriever, Clock clock) {
        this.referenceRetriever = referenceRetriever;
        this.clock = clock;
        log.info("Creating");
    }

    @Override
    public List<SourceGraph> sourceGraphs() {
        return referenceRetriever.sources().stream()
            .map(source -> new SourceGraph(source.graph(), source.title()))
            .toList();
    }

    @Override
    public Map<String, String> graphs(Set<String> referencedIris) {
        val turtleByGraph = new LinkedHashMap<String, String>();

        for (val source : referenceRetriever.sources()) {
            val wanted = referencedIris.stream()
                .filter(source::describes)
                .sorted()
                .toList();
            if (wanted.isEmpty()) {
                continue;
            }

            val described = referenceRetriever.describe(wanted, source);
            if (described.isEmpty()) {
                log.warn("Nothing retrieved for {}, leaving its graph as it is", source.graph());
                continue;
            }
            if (!described.isComplete()) {
                log.info(
                    "Not publishing {} yet: of {} entities, {} are still to be fetched and "
                        + "{} could not be served",
                    source.graph(), wanted.size(),
                    described.deferred(), described.transientFailures()
                );
                continue;
            }

            val model = described.model();
            addProvenance(model, source);
            turtleByGraph.put(source.graph(), serialise(model));
        }
        return turtleByGraph;
    }

    private void addProvenance(Model model, ReferenceSource source) {
        val graph = model.getResource(source.graph());
        model.add(graph, RDF.type, model.getResource(VOID + "Dataset"));
        model.add(graph, DCTerms.title, source.title());
        model.add(graph, DCTerms.description,
            "Descriptions as published by the authority, republished unchanged; "
                + "the catalogue asserts nothing of its own here.");
        if (source.licence() != null) {
            model.add(graph, DCTerms.license, model.getResource(source.licence()));
            // GeoNames' licence requires attribution, and the graph is where a
            // consumer will look for it.
            model.add(graph, DCTerms.publisher, source.title());
        }
        model.add(graph, model.getProperty(PROV + "generatedAtTime"),
            model.createTypedLiteral(
                Instant.now(clock).truncatedTo(ChronoUnit.SECONDS).toString(),
                "http://www.w3.org/2001/XMLSchema#dateTime"));
    }

    private static String serialise(Model model) {
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("owl", OWL.getURI());
        model.setNsPrefix("void", VOID);
        model.setNsPrefix("prov", PROV);
        model.setNsPrefix("gn", GN);
        model.setNsPrefix("wgs84_pos", WGS84);
        model.setNsPrefix("bibo", BIBO);
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        val writer = new StringWriter();
        RDFDataMgr.write(writer, model, Lang.TURTLE);
        return writer.toString();
    }
}
