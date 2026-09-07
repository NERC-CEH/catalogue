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
 * Publishes what ORCID and ROR say about the people and organisations the
 * catalogue names — dri-one #350 phase 3.
 *
 * <p>This is the phase that pays for #348. That change stopped the export
 * writing record-derived names onto ORCID URIs, which was right — 281 of them
 * had accumulated more than one name, one asserting a different person
 * entirely — but it left 2,125 ORCID nodes carrying nothing but a type. Here
 * they get the researcher's own name instead of a depositor's spelling of it.
 *
 * <p>For organisations the win is different. ROR publishes an organisation's
 * official name alongside its acronym and aliases, which is what lets a
 * differently-spelled record be recognised as the same institution. It is worth
 * being precise about how far that goes: ROR gives UKCEH as
 * {@code UK Centre for Ecology & Hydrology} and {@code UKCEH}, which covers two
 * of the four spellings #347 found fragmenting, and has no former-name type at
 * all, so {@code Institute of Terrestrial Ecology} will never come from there.
 * The remaining variants stay a data-cleanup matter.
 *
 * <p>The aliases are also multilingual — UKCEH's record carries a Welsh and a
 * French name — so each is published with the language tag ROR recorded, and
 * the acronym with none, because ROR records none and an acronym is not English
 * text. An untagged {@code Centre britannique pour l'Écologie et l'Hydrologie}
 * is not an alternative spelling a consumer can act on.
 *
 * <p>Both authorities publish under CC0, which is why no {@code dcterms:license}
 * appears on these graphs while the vocabulary ones are still unresolved — the
 * terms here are known, and stated.
 *
 * <h2>A graph is published whole or not at all</h2>
 *
 * <p>The export writes each graph with a single PUT, which replaces it. That
 * makes a partially-filled cache dangerous rather than merely incomplete: a run
 * that had only reached 200 of 561 organisations would replace a full ROR graph
 * with a third of one, and the endpoint would then lose and regain descriptions
 * every time the pod was recreated. So a graph is left alone unless the run
 * behind it is complete — see
 * {@link IdentityRetriever.Descriptions#isComplete()}.
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
public class IdentityGraphService implements SourceGraphProvider {

    private final List<IdentitySource> sources;
    private final AuthorityRetriever retriever;
    private final WithheldGraphLog withheldGraphLog;
    private final Clock clock;

    /** @see VocabularyGraphService for why this annotation is needed. */
    @Autowired
    public IdentityGraphService(
        List<IdentitySource> sources,
        AuthorityRetriever retriever,
        WithheldGraphLog withheldGraphLog
    ) {
        this(sources, retriever, withheldGraphLog, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the clock in the provenance header. */
    IdentityGraphService(
        List<IdentitySource> sources,
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
        return sources.stream().map(IdentityGraphService::sourceGraph).toList();
    }

    /** Everything said about one authority's graph, declared by the source itself. */
    private static SourceGraph sourceGraph(IdentitySource source) {
        return new SourceGraph(source.graph(), source.title(), source.description(),
            source.vocabularies(), source.licence());
    }

    @Override
    public Map<String, String> graphs(Set<String> referencedIris) {
        val turtleByGraph = new LinkedHashMap<String, String>();

        for (val source : sources) {
            // Which IRIs are the source's own concern now: an ORCID's account
            // node (…#orcid-id) is referenced by ORCID's own RDF, not by us, and
            // is not a person.
            val wanted = referencedIris.stream()
                .filter(source::describes)
                .sorted()
                .toList();
            if (wanted.isEmpty()) {
                continue;
            }

            val described = retriever.describe(wanted, source);
            if (described.isEmpty()) {
                // Nothing at all, from the authority or the cache. Publishing an
                // empty graph would replace whatever is already there with less.
                log.warn("No identities retrieved for {}, leaving its graph as it is", source.graph());
                continue;
            }
            if (!described.isComplete()) {
                // This run holds only part of what the authority has to say. The
                // export's PUT replaces a graph wholesale, so publishing now
                // would swap a full graph for a partial one and then swap it
                // back over the following days -- the endpoint would visibly
                // lose and regain descriptions. Leaving the graph alone costs
                // freshness for a run and nothing else.
                //
                // Both counts matter, and the second was originally missed: an
                // entity the authority failed to serve is just as absent from
                // this graph as one the budget never reached, and a timeout or
                // a rate limit is every bit as likely to succeed tomorrow.
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
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("skos", SKOS.getURI());
        model.setNsPrefix("owl", OWL.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("void", SourceGraphs.VOID);
        model.setNsPrefix("prov", SourceGraphs.PROV);
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        val writer = new StringWriter();
        RDFDataMgr.write(writer, model, Lang.TURTLE);
        return writer.toString();
    }
}
