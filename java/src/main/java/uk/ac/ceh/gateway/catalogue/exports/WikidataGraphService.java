package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.Clock;
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
@ToString(exclude = {"source", "retriever"})
public class WikidataGraphService implements SourceGraphProvider {

    private final WikidataSource source;
    private final AuthorityRetriever retriever;
    private final WithheldGraphLog withheldGraphLog;
    private final Clock clock;

    /** @see VocabularyGraphService for why this annotation is needed. */
    @Autowired
    public WikidataGraphService(
        WikidataSource source,
        AuthorityRetriever retriever,
        WithheldGraphLog withheldGraphLog
    ) {
        this(source, retriever, withheldGraphLog, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the clock in the provenance header. */
    WikidataGraphService(
        WikidataSource source,
        AuthorityRetriever retriever,
        WithheldGraphLog withheldGraphLog,
        Clock clock
    ) {
        this.source = source;
        this.retriever = retriever;
        this.withheldGraphLog = withheldGraphLog;
        this.clock = clock;
        log.info("Creating");
    }

    /** Everything said about the graph, declared by the source itself. */
    private SourceGraph sourceGraph() {
        return new SourceGraph(source.graph(), source.title(), source.description(),
            source.vocabularies(), source.licence());
    }

    @Override
    public List<SourceGraph> sourceGraphs() {
        return List.of(sourceGraph());
    }

    @Override
    public Map<String, String> graphs(Set<String> referencedIris) {
        val wanted = referencedIris.stream()
            .filter(source::describes)
            .sorted()
            .toList();
        if (wanted.isEmpty()) {
            return Map.of();
        }

        val described = retriever.describe(wanted, source);
        if (described.isEmpty()) {
            log.warn("Nothing retrieved from Wikidata, leaving its graph as it is");
            return Map.of();
        }
        if (!described.isComplete()) {
            withheldGraphLog.withheld(WikidataSource.PREFIX, wanted.size(),
                described.deferred(), described.transientFailures());
            return Map.of();
        }

        val model = described.model();
        SourceGraphs.addProvenance(model, sourceGraph(), clock);
        val turtleByGraph = new LinkedHashMap<String, String>();
        turtleByGraph.put(WikidataSource.PREFIX, serialise(model));
        withheldGraphLog.published(WikidataSource.PREFIX);
        return turtleByGraph;
    }

    private static String serialise(Model model) {
        model.setNsPrefix("wd", WikidataSource.PREFIX);
        model.setNsPrefix("wdt", SourceGraphs.WDT);
        model.setNsPrefix("skos", SKOS.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("schema", SourceGraphs.SCHEMA);
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("void", SourceGraphs.VOID);
        model.setNsPrefix("prov", SourceGraphs.PROV);
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        val writer = new StringWriter();
        RDFDataMgr.write(writer, model, Lang.TURTLE);
        return writer.toString();
    }
}
