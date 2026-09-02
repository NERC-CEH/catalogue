package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
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

import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 *   <li><b>Retrieval partly failed</b> — no longer degrading, because
 *       {@link SkosConceptRetriever} now falls back to a cached copy per
 *       concept. What is missing from its result is what no run could get.</li>
 * </ul>
 *
 * <p>One gap remains, and is left open knowingly: a run that starts with an
 * empty cache and meets a partly-unavailable authority still publishes a thin
 * graph. Closing it would mean a completeness threshold, which would freeze a
 * graph for good the first time a concept was permanently withdrawn. The
 * cache's snapshot makes an empty cache rare, which is the cheaper half of the
 * problem to attack.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = {"solrClient", "uriNormaliser", "skosConceptRetriever"})
public class VocabularyGraphService implements SourceGraphProvider {

    private static final String COLLECTION = "keywords";

    /** Solr's default row limit is 10; the whole collection is ~8,700 documents. */
    private static final int PAGE_SIZE = 1000;

    private static final String VOID = "http://rdfs.org/ns/void#";
    private static final String PROV = "http://www.w3.org/ns/prov#";

    /**
     * One external vocabulary and how its graph is built.
     *
     * @param graph        the named graph it is published to, and the authority's own namespace
     * @param title        a human-readable name, for the VoID description
     * @param localVocabId its id in the Solr keyword index, or null if it is not harvested
     * @param retrieval    how to fetch full SKOS, or null if the authority does not offer it
     */
    public record Authority(
        String graph,
        String title,
        String localVocabId,
        SkosConceptRetriever.Retrieval retrieval
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
            "GEMET, the GEneral Multilingual Environmental Thesaurus", "gemet", null),
        new Authority("http://vocabs.lter-europe.net/EnvThes/",
            "EnvThes, the eLTER environmental thesaurus", "envThes", null),
        new Authority("http://onto.nerc.ac.uk/CAST/",
            "CAST, the NERC CEH categories and subjects thesaurus", "cast",
            SkosConceptRetriever.Retrieval.UKCEH_SPARQL),
        new Authority("https://digital.ceh.ac.uk/vocab/ra/",
            "UKCEH research activities", "research-activity", null),
        new Authority("https://digital.ceh.ac.uk/vocab/fdri/",
            "FDRI, Floods and Droughts Research Infrastructure terms", "fdri", null),
        new Authority("http://vocab.nerc.ac.uk/",
            "NVS, the NERC Vocabulary Server", null,
            SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION),
        new Authority("http://aims.fao.org/aos/agrovoc/",
            "AGROVOC, the FAO multilingual thesaurus", null,
            SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION)
    );

    private final SolrClient solrClient;
    private final UriNormaliser uriNormaliser;
    private final SkosConceptRetriever skosConceptRetriever;
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
        SkosConceptRetriever skosConceptRetriever
    ) {
        this(solrClient, uriNormaliser, skosConceptRetriever, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the clock in the provenance header. */
    VocabularyGraphService(
        SolrClient solrClient,
        UriNormaliser uriNormaliser,
        SkosConceptRetriever skosConceptRetriever,
        Clock clock
    ) {
        this.solrClient = solrClient;
        this.uriNormaliser = uriNormaliser;
        this.skosConceptRetriever = skosConceptRetriever;
        this.clock = clock;
        log.info("Creating");
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
        return authorities().stream()
            .map(authority -> new SourceGraph(authority.graph(), authority.title()))
            .toList();
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
                    continue;
                }
                addLocalLabels(model, harvested);
            }

            if (authority.retrieval() != null) {
                val wanted = referencedConcepts.stream()
                    .filter(uri -> uri.startsWith(authority.graph()))
                    .sorted()
                    .toList();
                if (!wanted.isEmpty()) {
                    val retrieved = skosConceptRetriever.describe(wanted, authority.retrieval());
                    if (retrieved.isEmpty()) {
                        // Every retrieval failed. Publishing what is left would
                        // replace a good graph with a poorer one, so leave the
                        // previous version in place instead.
                        log.warn("No concept descriptions retrieved for {}, leaving its graph as it is",
                            authority.graph());
                        continue;
                    }
                    model.add(retrieved);
                }
            }

            if (model.isEmpty()) {
                continue;
            }
            addProvenance(model, authority);
            turtleByGraph.put(authority.graph(), serialise(model));
        }
        return turtleByGraph;
    }

    private void addLocalLabels(Model model, Collection<Keyword> keywords) {
        for (val keyword : keywords) {
            val uri = uriNormaliser.normalise(keyword.getUrl());
            if (uri.isEmpty() || keyword.getLabel() == null || keyword.getLabel().isBlank()) {
                continue;
            }
            val concept = model.getResource(uri);
            model.add(concept, RDF.type, SKOS.Concept);
            model.add(concept, SKOS.prefLabel, keyword.getLabel());
        }
    }

    /** What this graph is, and when the copy of it was taken. */
    private void addProvenance(Model model, Authority authority) {
        val graph = model.getResource(authority.graph());
        model.add(graph, RDF.type, model.getResource(VOID + "Dataset"));
        model.add(graph, DCTerms.title, authority.title());
        model.add(graph, DCTerms.description,
            "Concept descriptions as published by the authority, republished unchanged; "
                + "the catalogue asserts nothing of its own here.");
        model.add(graph, model.getProperty(PROV + "generatedAtTime"),
            model.createTypedLiteral(
                Instant.now(clock).truncatedTo(ChronoUnit.SECONDS).toString(),
                "http://www.w3.org/2001/XMLSchema#dateTime"));
    }

    /**
     * Serialised by Jena rather than assembled as text, so literal escaping is
     * the parser's problem and not ours. A single unescaped backslash in one
     * hand-built literal took down every export for a week (dri-one #344).
     */
    private static String serialise(Model model) {
        model.setNsPrefix("skos", SKOS.getURI());
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("void", VOID);
        model.setNsPrefix("prov", PROV);
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        val writer = new StringWriter();
        RDFDataMgr.write(writer, model, Lang.TURTLE);
        return writer.toString();
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
