package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
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
@ToString(exclude = {"sources", "retriever"})
public class ReferenceGraphService implements SourceGraphProvider {

    private static final String GN = "http://www.geonames.org/ontology#";
    private static final String WGS84 = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    private static final String BIBO = "http://purl.org/ontology/bibo/";

    private final List<ReferenceSource> sources;
    private final AuthorityRetriever retriever;
    private final WithheldGraphLog withheldGraphLog;
    private final Clock clock;

    /** @see VocabularyGraphService for why this annotation is needed. */
    @Autowired
    public ReferenceGraphService(
        List<ReferenceSource> sources,
        AuthorityRetriever retriever,
        WithheldGraphLog withheldGraphLog
    ) {
        this(sources, retriever, withheldGraphLog, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the clock in the provenance header. */
    ReferenceGraphService(
        List<ReferenceSource> sources,
        AuthorityRetriever retriever,
        WithheldGraphLog withheldGraphLog,
        Clock clock
    ) {
        this.sources = List.copyOf(sources);
        this.retriever = retriever;
        this.withheldGraphLog = withheldGraphLog;
        this.clock = clock;
        log.info("Creating with {} sources", this.sources.size());
    }

    @Override
    public List<SourceGraph> sourceGraphs() {
        return sources.stream()
            .map(ReferenceGraphService::sourceGraph)
            .toList();
    }

    /** Everything said about one authority's graph, declared by the source itself. */
    private static SourceGraph sourceGraph(ReferenceSource source) {
        return new SourceGraph(source.graph(), source.title(), source.description(),
            source.vocabularies(), source.licence());
    }

    @Override
    public Map<String, String> graphs(Set<String> referencedIris) {
        val turtleByGraph = new LinkedHashMap<String, String>();

        for (val source : sources) {
            val wanted = referencedIris.stream()
                .filter(source::describes)
                .sorted()
                .toList();
            if (wanted.isEmpty()) {
                continue;
            }

            val described = retriever.describe(wanted, source);
            if (described.isEmpty()) {
                log.warn("Nothing retrieved for {}, leaving its graph as it is", source.graph());
                continue;
            }
            if (!described.isComplete()) {
                withheldGraphLog.withheld(source.graph(), wanted.size(),
                    described.deferred(), described.transientFailures());
                continue;
            }

            val model = described.model();
            SourceGraphs.addProvenance(model, sourceGraph(source), clock);
            turtleByGraph.put(source.graph(), serialise(model));
            withheldGraphLog.published(source.graph());
        }
        return turtleByGraph;
    }

    private static String serialise(Model model) {
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("owl", OWL.getURI());
        model.setNsPrefix("void", SourceGraphs.VOID);
        model.setNsPrefix("prov", SourceGraphs.PROV);
        model.setNsPrefix("gn", GN);
        model.setNsPrefix("wgs84_pos", WGS84);
        model.setNsPrefix("bibo", BIBO);
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        val writer = new StringWriter();
        RDFDataMgr.write(writer, model, Lang.TURTLE);
        return writer.toString();
    }
}
