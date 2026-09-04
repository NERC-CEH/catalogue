package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
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
 * Publishes what Wikidata says about the concepts the records use as subjects —
 * dri-one #350 phase 5.
 *
 * <p>The 2,064 Wikidata URIs in the catalogue's graph are referenced 2,980 times
 * and carry nothing but the reference: no label, no description, no indication
 * of what kind of thing they are. A consumer reading
 * {@code dcterms:subject <…/entity/Q663181>} could learn nothing without leaving
 * the endpoint. It now finds "Speckled Wood", a species of insect, with its
 * taxon name.
 *
 * <p>One graph, {@code http://www.wikidata.org/entity/}, and the same publishing
 * rule as every phase since 3: a graph is written only when the run behind it is
 * complete, because the export's PUT replaces it wholesale.
 *
 * <h2>The one licence that is genuinely simple</h2>
 *
 * <p>Wikidata releases all of its structured data under CC0, so unlike the
 * vocabulary graphs this one can state its terms without qualification, as the
 * ORCID and ROR graphs do.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = "wikidataRetriever")
public class WikidataGraphService implements SourceGraphProvider {

    private static final String VOID = "http://rdfs.org/ns/void#";
    private static final String PROV = "http://www.w3.org/ns/prov#";
    private static final String WDT = "http://www.wikidata.org/prop/direct/";
    private static final String SCHEMA = "http://schema.org/";
    private static final String CC0 = "https://creativecommons.org/publicdomain/zero/1.0/";

    private static final String TITLE = "Wikidata, the free knowledge base";

    private final WikidataRetriever wikidataRetriever;
    private final Clock clock;

    /** @see VocabularyGraphService for why this annotation is needed. */
    @Autowired
    public WikidataGraphService(WikidataRetriever wikidataRetriever) {
        this(wikidataRetriever, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the clock in the provenance header. */
    WikidataGraphService(WikidataRetriever wikidataRetriever, Clock clock) {
        this.wikidataRetriever = wikidataRetriever;
        this.clock = clock;
        log.info("Creating");
    }

    @Override
    public List<SourceGraph> sourceGraphs() {
        return List.of(new SourceGraph(WikidataRetriever.PREFIX, TITLE));
    }

    @Override
    public Map<String, String> graphs(Set<String> referencedIris) {
        val wanted = referencedIris.stream()
            .filter(WikidataRetriever::describes)
            .sorted()
            .toList();
        if (wanted.isEmpty()) {
            return Map.of();
        }

        val described = wikidataRetriever.describe(wanted);
        if (described.isEmpty()) {
            log.warn("Nothing retrieved from Wikidata, leaving its graph as it is");
            return Map.of();
        }
        if (!described.isComplete()) {
            log.info(
                "Not publishing {} yet: of {} entities, {} are still to be fetched and "
                    + "{} could not be served",
                WikidataRetriever.PREFIX, wanted.size(),
                described.deferred(), described.transientFailures()
            );
            return Map.of();
        }

        val model = described.model();
        addProvenance(model);
        val turtleByGraph = new LinkedHashMap<String, String>();
        turtleByGraph.put(WikidataRetriever.PREFIX, serialise(model));
        return turtleByGraph;
    }

    private void addProvenance(Model model) {
        val graph = model.getResource(WikidataRetriever.PREFIX);
        model.add(graph, RDF.type, model.getResource(VOID + "Dataset"));
        model.add(graph, DCTerms.title, TITLE);
        model.add(graph, DCTerms.description,
            "Concept descriptions as published by Wikidata, republished unchanged; "
                + "the catalogue asserts nothing of its own here.");
        model.add(graph, DCTerms.license, model.getResource(CC0));
        model.add(graph, model.getProperty(PROV + "generatedAtTime"),
            model.createTypedLiteral(
                Instant.now(clock).truncatedTo(ChronoUnit.SECONDS).toString(),
                "http://www.w3.org/2001/XMLSchema#dateTime"));
    }

    private static String serialise(Model model) {
        model.setNsPrefix("wd", WikidataRetriever.PREFIX);
        model.setNsPrefix("wdt", WDT);
        model.setNsPrefix("skos", SKOS.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("schema", SCHEMA);
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("void", VOID);
        model.setNsPrefix("prov", PROV);
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        val writer = new StringWriter();
        RDFDataMgr.write(writer, model, Lang.TURTLE);
        return writer.toString();
    }
}
