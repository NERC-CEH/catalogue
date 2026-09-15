package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser;
import uk.ac.ceh.gateway.catalogue.vocabularies.Keyword;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

/**
 * Assembles the named graph published for each external vocabulary authority.
 *
 * <p>The catalogue's graph references 11,600 external URIs and says almost
 * nothing about them, because dri-one #320 correctly forbids asserting record
 * text onto a shared identifier. None of what this publishes is record text: it
 * is what the authorities themselves say about their own concepts, republished
 * unchanged into a graph of their own.
 *
 * <p>Two sources feed each graph, and the point of assembling them here rather
 * than in two services is that they would otherwise fight. The export writes a
 * graph with a single all-or-nothing PUT, so two writers aiming at
 * {@code http://onto.nerc.ac.uk/CAST/} would mean the second silently replacing
 * the first.
 *
 * <ol>
 *   <li><b>Labels the application already holds</b> (#350 phase 1). The keyword
 *       harvest fetches these from the authorities weekly and stores them in the
 *       Solr {@code keywords} collection for the editor's keyword picker — 8,661
 *       of them at the time of writing. They cost no network request.</li>
 *   <li><b>Full SKOS from the authority</b> (#350 phase 2), for the vocabularies
 *       that publish it in a form we can read: definitions, alternate labels and
 *       the broader/narrower hierarchy, which the label-only harvest cannot
 *       give. Only for concepts the catalogue actually references — 132 of them
 *       across NVS, AGROVOC and CAST.</li>
 * </ol>
 *
 * <p>Attribution is structural: a consumer asks the catalogue graph what the
 * catalogue asserts and the EnvThes graph what eLTER asserts, without unpicking
 * one from the other by predicate.
 *
 * <p>No {@code dcterms:license} is asserted. The authorities license their
 * content on differing terms and we have not established them; claiming the
 * wrong one would be worse than claiming none. Recording them is a follow-up,
 * and is needed before this data is redistributed further.
 *
 * <h2>A graph is published whole or not at all</h2>
 *
 * <p>Because the PUT replaces a graph rather than adding to it, publishing a
 * degraded version is worse than publishing nothing: the endpoint loses what it
 * had. Three ways a run can be degraded, and what happens to each:
 *
 * <ul>
 *   <li><b>The harvested labels are missing</b> — Solr unreachable, or a harvest
 *       that returned nothing. The graph is held back, since labels are the bulk
 *       of what most of these graphs hold.</li>
 *   <li><b>Retrieval failed entirely</b> — held back, as before.</li>
 *   <li><b>Retrieval partly failed</b> — held back too, now that
 *       {@link AuthorityRetriever.Descriptions#isComplete()} exists to say so.
 *       These graphs were the last that did not consult it, which was the gap
 *       this javadoc recorded as knowingly open: a run starting with an empty
 *       cache and meeting a partly-unavailable authority published a thin graph.
 *       Note the retriever has already fallen back to a cached copy of any age
 *       per concept before reporting, so an incomplete run means some concept
 *       has no description at all rather than merely a stale one.</li>
 * </ul>
 *
 * <p>Only a concept the authority <em>definitively</em> does not hold is
 * excused, which is what makes this the right shape. A completeness
 * <em>threshold</em> — publish once some proportion is described — would freeze
 * a graph the first time a concept was permanently withdrawn, because the
 * withdrawal would count against the proportion for ever. A 404 counts against
 * nothing.
 *
 * <p>Which graphs this reaches is narrower than it sounds. Only three of the
 * seven authorities fetch at all: NVS and AGROVOC, which dereference one concept
 * at a time and so genuinely can be partly complete, and CAST, which asks for
 * every concept in one query and therefore either succeeds or fails whole. The
 * other four are label-only, and a harvest cannot be incomplete — it is read
 * from Solr in a single paged sweep or not at all, which the branch above
 * already handles.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = {"solrClient", "uriNormaliser", "sources", "retriever"})
public class VocabularyGraphService implements SourceGraphProvider {

    private static final String COLLECTION = "keywords";

    /** Solr's default row limit is 10; the whole collection is ~8,700 documents. */
    private static final int PAGE_SIZE = 1000;

    /**
     * What a vocabulary graph holds. The same for all of them, unlike the other
     * providers': these really are SKOS concept descriptions, which is what the
     * VoID document used to claim about every source graph regardless.
     */
    private static final String DESCRIPTION =
        "Concept descriptions as published by the authority: preferred and alternate labels, "
            + "definitions, notations and the broader/narrower hierarchy.";

    /**
     * One external vocabulary and how its graph is built.
     *
     * @param graph        the named graph it is published to, and the authority's own namespace
     * @param title        a human-readable name, for the VoID description
     * @param localVocabId its id in the Solr keyword index, or null if it is not harvested
     */
    public record Authority(
        String graph,
        String title,
        String localVocabId
    ) {}

    /**
     * The schemes matter and are not uniform — see {@link UriNormaliser}'s host
     * policies, checked against each authority: the NERC and eLTER vocabularies
     * mint {@code http}, the newer UKCEH ones {@code https}.
     *
     * <p>NVS and AGROVOC are here only for retrieval: neither is in the keyword
     * harvest, so before #350 phase 2 the catalogue referenced 100 of their
     * concepts and published nothing at all about any of them.
     *
     * <p>Research activities and FDRI are label-only for now. They sit on the
     * same UKCEH endpoint as CAST so adding retrieval is one more query, but
     * #350 scoped phase 2 to three vocabularies and this follows it.
     */
    private static final List<Authority> AUTHORITIES = List.of(
        new Authority("http://www.eionet.europa.eu/gemet/",
            "GEMET, the GEneral Multilingual Environmental Thesaurus", "gemet"),
        new Authority("http://vocabs.lter-europe.net/EnvThes/",
            "EnvThes, the eLTER environmental thesaurus", "envThes"),
        new Authority("http://onto.nerc.ac.uk/CAST/",
            "CAST, the NERC CEH categories and subjects thesaurus", "cast"),
        new Authority("https://digital.ceh.ac.uk/vocab/ra/",
            "UKCEH research activities", "research-activity"),
        new Authority("https://digital.ceh.ac.uk/vocab/fdri/",
            "FDRI, Floods and Droughts Research Infrastructure terms", "fdri"),
        new Authority("http://vocab.nerc.ac.uk/",
            "NVS, the NERC Vocabulary Server", null),
        new Authority("http://aims.fao.org/aos/agrovoc/",
            "AGROVOC, the FAO multilingual thesaurus", null)
    );

    private final SolrClient solrClient;
    private final UriNormaliser uriNormaliser;
    private final List<VocabularySource> sources;
    private final AuthorityRetriever retriever;
    private final SourceGraphProgress progress;
    private final Clock clock;

    /**
     * Annotated because there are two constructors and Spring will not choose
     * between them: without this the context fails to start with "no default
     * constructor found", which the production-context tests catch.
     */
    @Autowired
    public VocabularyGraphService(
        SolrClient solrClient,
        UriNormaliser uriNormaliser,
        List<VocabularySource> sources,
        AuthorityRetriever retriever,
        SourceGraphProgress progress
    ) {
        this(solrClient, uriNormaliser, sources, retriever, progress, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the clock in the provenance header. */
    VocabularyGraphService(
        SolrClient solrClient,
        UriNormaliser uriNormaliser,
        List<VocabularySource> sources,
        AuthorityRetriever retriever,
        SourceGraphProgress progress,
        Clock clock
    ) {
        this.solrClient = solrClient;
        this.uriNormaliser = uriNormaliser;
        this.sources = List.copyOf(sources);
        this.retriever = retriever;
        this.progress = progress;
        this.clock = clock;
        log.info("Creating with {} fetching vocabularies", this.sources.size());
    }

    /** The source that fetches for this authority, if any does. */
    private Optional<VocabularySource> sourceFor(Authority authority) {
        return sources.stream()
            .filter(source -> source.graph().equals(authority.graph()))
            .findFirst();
    }

    /**
     * The graphs this service publishes to, whether or not there is currently
     * anything to put in them.
     *
     * <p>Separate from {@link #graphs} on purpose: that reports what there is to
     * publish right now, whereas this declares what the endpoint offers, which
     * is what the VoID description at {@code /.well-known/void} advertises. One
     * list, so the description cannot drift from what is actually written.
     */
    @Override
    public List<SourceGraph> sourceGraphs() {
        return authorities().stream().map(VocabularyGraphService::sourceGraph).toList();
    }

    /**
     * No licence: the authorities license their content on differing terms and we
     * have not established them, and the wrong claim would be worse than none.
     * Recording them is a follow-up, and is needed before this data is
     * redistributed further.
     */
    private static SourceGraph sourceGraph(Authority authority) {
        return new SourceGraph(authority.graph(), authority.title(), DESCRIPTION,
            List.of(SKOS.getURI()), null);
    }

    /** The full descriptors, which only this class needs. */
    private static List<Authority> authorities() {
        return AUTHORITIES.stream().sorted(Comparator.comparing(Authority::graph)).toList();
    }

    /**
     * @param referencedConcepts every concept URI the catalogue's own graph
     *                           refers to, so retrieval is limited to concepts
     *                           something actually cites
     * @return the Turtle to publish, keyed by graph, in a stable order. A graph
     *         is absent rather than empty where there is nothing to say, so a
     *         previous run's content is left alone instead of being replaced
     *         with less.
     */
    @Override
    public Map<String, String> graphs(Set<String> referencedConcepts) {
        val localLabels = readLocalLabels();
        val turtleByGraph = new LinkedHashMap<String, String>();

        for (val authority : authorities()) {
            val model = ModelFactory.createDefaultModel();

            if (authority.localVocabId() != null) {
                val harvested = localLabels.getOrDefault(authority.localVocabId(), List.of());
                if (harvested.isEmpty()) {
                    // A harvested vocabulary with no labels is a fault, not an
                    // empty vocabulary: Solr unreachable, or a harvest that
                    // silently brought back nothing (dri-one #349 found two of
                    // those). Either way the labels are the bulk of what this
                    // graph holds, and CAST also carries retrieved SKOS — so
                    // going ahead would replace a graph of labels and
                    // definitions with one holding definitions alone, and
                    // publish the fault. Leaving the previous graph in place
                    // loses a day's freshness and nothing else.
                    log.warn("No harvested labels for {}, leaving its graph as it is", authority.graph());
                    // How many labels there should have been is exactly what the
                    // failed harvest did not tell us, so the count is left at
                    // zero and the state carries the message on its own.
                    progress.nothingRetrieved(authority.graph(), 0);
                    continue;
                }
                addLocalLabels(model, harvested);
            }

            val source = sourceFor(authority);
            if (source.isPresent()) {
                val wanted = referencedConcepts.stream()
                    .filter(source.get()::describes)
                    .sorted()
                    .toList();
                if (!wanted.isEmpty()) {
                    val described = retriever.describe(wanted, source.get());
                    if (described.isEmpty()) {
                        // Every retrieval failed. Publishing what is left would
                        // replace a good graph with a poorer one, so leave the
                        // previous version in place instead.
                        log.warn("No concept descriptions retrieved for {}, leaving its graph as it is",
                            authority.graph());
                        progress.nothingRetrieved(authority.graph(), wanted.size());
                        continue;
                    }
                    if (!described.isComplete()) {
                        // The gap this class recorded as knowingly open until
                        // the retrievers were unified. A run that reached only
                        // some of the concepts, or met a failure a later run may
                        // not, would replace the graph with less than it holds
                        // -- and since the export's PUT replaces rather than
                        // adds, the endpoint would visibly lose and regain
                        // definitions over the following days.
                        //
                        // Only a concept the authority definitively does not
                        // hold is excused, so a withdrawn concept cannot freeze
                        // the graph, which is what made a completeness
                        // *threshold* the wrong answer here.
                        progress.withheld(authority.graph(), wanted.size(),
                            described.deferred(), described.transientFailures());
                        continue;
                    }
                    model.add(described.model());
                }
            }

            if (model.isEmpty()) {
                // No labels harvested for it and nothing citing its concepts, so
                // there was never anything to publish. Recorded so the
                // maintenance page does not show it as a graph nobody has
                // reached, which is a different and worrying thing.
                progress.nothingReferenced(authority.graph());
                continue;
            }
            // Counted before the provenance header goes in, which adds the
            // graph's own void:Dataset as a subject of its own.
            val entities = SourceGraphs.entities(model);
            SourceGraphs.addProvenance(model, sourceGraph(authority), clock);
            turtleByGraph.put(authority.graph(), SourceGraphs.serialise(model, sourceGraph(authority)));
            progress.published(authority.graph(), entities);
        }
        return turtleByGraph;
    }

    private void addLocalLabels(Model model, Collection<Keyword> keywords) {
        for (val keyword : keywords) {
            val uri = uriNormaliser.normalise(keyword.getUrl());
            if (uri.isEmpty() || keyword.getLabel() == null || keyword.getLabel().isBlank()) {
                continue;
            }
            // Where a malformed keyword URL enters the graph. Jena would write
            // it with only a WARN, so it would reach the endpoint and each
            // consumer would find it separately. Reported, never repaired --
            // dri-one #331 settled that URI quality is not corrected on our
            // side, and guessing what a depositor meant would be exactly that.
            if (!Iris.isPublishable(uri)) {
                log.warn("Harvested keyword URL is not usable as an IRI, skipping: {}", uri);
                continue;
            }
            val concept = model.getResource(uri);
            model.add(concept, RDF.type, SKOS.Concept);
            model.add(concept, SKOS.prefLabel, keyword.getLabel());
        }
    }

    private Map<String, List<Keyword>> readLocalLabels() {
        val byVocabulary = new LinkedHashMap<String, List<Keyword>>();
        var start = 0;
        try {
            while (true) {
                val query = new SolrQuery();
                query.setQuery("*:*");
                query.setStart(start);
                query.setRows(PAGE_SIZE);
                // A stable sort, so paging cannot skip or repeat a document.
                query.setSort("url", SolrQuery.ORDER.asc);
                val page = solrClient.query(COLLECTION, query, POST).getBeans(Keyword.class);
                page.forEach(keyword -> byVocabulary
                    .computeIfAbsent(keyword.getVocabId(), id -> new ArrayList<>())
                    .add(keyword));
                if (page.size() < PAGE_SIZE) {
                    break;
                }
                start += PAGE_SIZE;
            }
        } catch (Exception ex) {
            // Deliberately wide, and deliberately not rethrown. RemoteSolrException
            // is a sibling of SolrServerException rather than a subclass, so a 4xx
            // from Solr escapes a narrower catch. Losing the harvested labels is a
            // degraded export; failing the whole export over it is worse.
            log.warn("Could not read vocabulary labels from Solr: {}", ex.getMessage());
            return Map.of();
        }
        log.info("Read {} harvested vocabulary labels",
            byVocabulary.values().stream().mapToInt(List::size).sum());
        return byVocabulary;
    }
}
