package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Publishes one named graph per authority, holding what that authority says
 * about the entities the catalogue's records cite.
 *
 * <p>The catalogue's own graph says almost nothing about the 11,600 external
 * URIs it references, because dri-one #320 forbids asserting record text onto a
 * shared identifier. None of what this publishes is record text: it is what each
 * authority says about its own entities, republished unchanged into a graph of
 * its own, so attribution is structural rather than conventional — a consumer
 * asks the catalogue graph what the catalogue asserts and GeoNames' graph what
 * GeoNames asserts, without unpicking one from the other by predicate.
 *
 * <p>This was three services: one for the researchers and organisations of #350
 * phase 3, one for the works, grants, places and sites of phase 4, and one for
 * the Wikidata subjects of phase 5. They were the same class three times, and
 * they stayed that way because each phase's sources had a different shape.
 * Uniform sources make them one.
 *
 * <h2>A graph is published whole or not at all</h2>
 *
 * <p>The export writes each graph with a single PUT, which replaces it. That
 * makes a partially-filled run dangerous rather than merely incomplete: a run
 * that had reached only 200 of 561 organisations would replace a full ROR graph
 * with a third of one, and the endpoint would then lose and regain descriptions
 * every time the pod was recreated. So a graph is left alone unless the run
 * behind it is complete.
 *
 * <p>"Complete" counts both entities the run never reached and entities the
 * authority could not serve. Only an entity the authority <em>definitively</em>
 * does not hold is excused, because a 404 for a mistyped identifier would
 * otherwise hold the graph back for ever.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = {"sources", "retriever"})
public class SourceGraphService implements SourceGraphProvider {

    private final List<AuthoritySource> sources;
    private final AuthorityRetriever retriever;
    private final SourceGraphProgress progress;
    private final Clock clock;

    /** @see VocabularyGraphService for why this annotation is needed. */
    @Autowired
    public SourceGraphService(
        List<AuthoritySource> sources,
        AuthorityRetriever retriever,
        SourceGraphProgress progress
    ) {
        this(sources, retriever, progress, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the clock in the provenance header. */
    SourceGraphService(
        List<AuthoritySource> sources,
        AuthorityRetriever retriever,
        SourceGraphProgress progress,
        Clock clock
    ) {
        // The vocabularies are assembled by VocabularyGraphService instead, and
        // that is not an oversight: two sources feed each of those graphs -- the
        // labels the keyword harvest already holds, and the SKOS retrieved from
        // the authority -- and since the export's PUT replaces a graph rather
        // than adding to it, two writers aiming at the same graph name would
        // mean the second silently replacing the first.
        this.sources = sources.stream()
            .filter(source -> !(source instanceof VocabularySource))
            .toList();
        this.retriever = retriever;
        this.progress = progress;
        this.clock = clock;
        log.info("Creating with {} sources", this.sources.size());
    }

    @Override
    public List<SourceGraph> sourceGraphs() {
        return sources.stream().map(SourceGraphService::sourceGraph).toList();
    }

    /** Everything said about one authority's graph, declared by the source itself. */
    private static SourceGraph sourceGraph(AuthoritySource source) {
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
                // Nothing cites this authority, which is a graph working
                // correctly rather than one nobody has got to yet. Recorded so
                // the maintenance page can tell those two apart.
                progress.nothingReferenced(source.graph());
                continue;
            }

            val described = retriever.describe(wanted, source);
            if (described.isEmpty()) {
                // Nothing at all, from the authority or the cache. Publishing an
                // empty graph would replace whatever is already there with less.
                log.warn("Nothing retrieved for {}, leaving its graph as it is", source.graph());
                progress.nothingRetrieved(source.graph(), wanted.size());
                continue;
            }
            if (!described.isComplete()) {
                // This run holds only part of what the authority has to say, and
                // a later run will do better. Leaving the graph alone costs a
                // run's freshness and nothing else.
                progress.withheld(source.graph(), wanted.size(),
                    described.deferred(), described.transientFailures());
                continue;
            }

            val declared = sourceGraph(source);
            val model = described.model();
            // Counted before the provenance header goes in, which adds the
            // graph's own void:Dataset as a subject of its own.
            val entities = SourceGraphs.entities(model);
            SourceGraphs.addProvenance(model, declared, clock);
            turtleByGraph.put(source.graph(), SourceGraphs.serialise(model, declared));
            progress.published(source.graph(), entities);
        }
        return turtleByGraph;
    }
}
