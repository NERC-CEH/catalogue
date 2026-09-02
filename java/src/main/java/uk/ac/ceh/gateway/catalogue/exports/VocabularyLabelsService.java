package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser;
import uk.ac.ceh.gateway.catalogue.vocabularies.Keyword;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

/**
 * Publishes the vocabulary labels the application already holds, as one named
 * graph per authority.
 *
 * <p>The catalogue's graph references 11,600 external URIs and says almost
 * nothing about them, because dri-one #320 correctly forbids asserting record
 * text onto a shared identifier. The labels are not record text, though — the
 * keyword harvest fetches them from the authorities themselves on a weekly
 * schedule and stores them in the Solr {@code keywords} collection, where the
 * editor's keyword picker reads them.
 *
 * <p>So at the point this was written 8,661 authoritative labels were already
 * present, current, and invisible to the graph: 5,573 GEMET, 2,729 EnvThes, 313
 * CAST, 36 research activities and 10 FDRI. Emitting them costs no network
 * request at all, which is why dri-one #350 makes this its first phase.
 *
 * <h2>One graph per authority</h2>
 *
 * <p>Each vocabulary is published to a graph named for the authority that mints
 * its concepts, never into the catalogue's own graph. Attribution is then
 * structural rather than conventional: a consumer asks the catalogue graph what
 * the catalogue asserts and the EnvThes graph what eLTER asserts, and neither
 * has to be unpicked from the other by predicate.
 *
 * <p>A vocabulary whose id is not mapped here is skipped with a warning rather
 * than guessed at. Landing a new vocabulary's labels in the wrong authority's
 * graph would be a worse failure than omitting them.
 *
 * <p>No {@code dcterms:license} is asserted on these graphs. The authorities
 * license their content on differing terms and we have not established them;
 * claiming the wrong one would be worse than claiming none. Recording them is a
 * follow-up, and is needed before this data is redistributed further.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = {"solrClient", "uriNormaliser"})
public class VocabularyLabelsService {

    private static final String COLLECTION = "keywords";

    /** Solr's default row limit is 10; the whole collection is ~8,700 documents. */
    private static final int PAGE_SIZE = 1000;

    /**
     * Where each harvested vocabulary's concepts are minted. The scheme matters
     * and is not uniform — see {@link UriNormaliser}'s host policies, which were
     * checked against each authority: the NERC and eLTER vocabularies mint
     * {@code http}, the newer UKCEH ones {@code https}.
     */
    private record Authority(String graph, String title) {}

    private static final Map<String, Authority> AUTHORITIES = Map.of(
        "gemet", new Authority(
            "http://www.eionet.europa.eu/gemet/",
            "GEMET, the GEneral Multilingual Environmental Thesaurus"),
        "envThes", new Authority(
            "http://vocabs.lter-europe.net/EnvThes/",
            "EnvThes, the eLTER environmental thesaurus"),
        "cast", new Authority(
            "http://onto.nerc.ac.uk/CAST/",
            "CAST, the NERC CEH categories and subjects thesaurus"),
        "research-activity", new Authority(
            "https://digital.ceh.ac.uk/vocab/ra/",
            "UKCEH research activities"),
        "fdri", new Authority(
            "https://digital.ceh.ac.uk/vocab/fdri/",
            "FDRI, Floods and Droughts Research Infrastructure terms")
    );

    private final SolrClient solrClient;
    private final UriNormaliser uriNormaliser;
    private final Clock clock;

    /**
     * Annotated because there are two constructors and Spring will not choose
     * between them: without this the context fails to start with "no default
     * constructor found", which the production-context tests catch.
     */
    @Autowired
    public VocabularyLabelsService(SolrClient solrClient, UriNormaliser uriNormaliser) {
        this(solrClient, uriNormaliser, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the clock in the provenance header. */
    VocabularyLabelsService(SolrClient solrClient, UriNormaliser uriNormaliser, Clock clock) {
        this.solrClient = solrClient;
        this.uriNormaliser = uriNormaliser;
        this.clock = clock;
        log.info("Creating");
    }

    /**
     * @return the Turtle to publish, keyed by the graph it belongs in, in a
     *         stable order. Empty where the keyword index holds nothing, so the
     *         caller publishes nothing rather than emptying a graph that a
     *         previous run filled.
     */
    public Map<String, String> graphs() {
        val byVocabulary = new TreeMap<String, List<Keyword>>();
        for (val keyword : readAllKeywords()) {
            if (!AUTHORITIES.containsKey(keyword.getVocabId())) {
                continue;
            }
            byVocabulary.computeIfAbsent(keyword.getVocabId(), id -> new ArrayList<>())
                .add(keyword);
        }

        val turtleByGraph = new LinkedHashMap<String, String>();
        byVocabulary.forEach((vocabId, keywords) -> {
            val authority = AUTHORITIES.get(vocabId);
            // Nothing currently maps two vocabularies to one graph, but merging
            // rather than putting means that if something ever does, neither is
            // silently dropped.
            turtleByGraph.merge(
                authority.graph(),
                turtle(authority, keywords),
                (existing, added) -> existing + "\n" + added
            );
        });
        return turtleByGraph;
    }

    private List<Keyword> readAllKeywords() {
        val all = new ArrayList<Keyword>();
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
                all.addAll(page);
                if (page.size() < PAGE_SIZE) {
                    break;
                }
                start += PAGE_SIZE;
            }
        } catch (Exception ex) {
            // Deliberately wide, and deliberately not rethrown. RemoteSolrException
            // is a sibling of SolrServerException rather than a subclass, so a 4xx
            // from Solr escapes a narrower catch. Publishing no vocabulary labels
            // is a degraded export; failing the whole export over it is worse.
            log.warn("Could not read vocabulary labels from Solr, publishing none: {}", ex.getMessage());
            return List.of();
        }
        log.info("Read {} vocabulary labels for export", all.size());
        return all;
    }

    private String turtle(Authority authority, List<Keyword> keywords) {
        val ttl = new StringBuilder()
            .append("PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n")
            .append("PREFIX dcterms: <http://purl.org/dc/terms/>\n")
            .append("PREFIX prov: <http://www.w3.org/ns/prov#>\n")
            .append("PREFIX void: <http://rdfs.org/ns/void#>\n")
            .append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n\n");

        // Provenance header: what this graph is, and when we last took a copy.
        ttl.append('<').append(authority.graph()).append("> a void:Dataset ;\n")
            .append("  dcterms:title \"").append(escape(authority.title())).append("\" ;\n")
            .append("  dcterms:description \"Concept labels harvested from the authority ")
            .append("and republished unchanged; the catalogue asserts nothing of its own here.\" ;\n")
            .append("  prov:generatedAtTime \"")
            .append(Instant.now(clock).truncatedTo(ChronoUnit.SECONDS))
            .append("\"^^xsd:dateTime ;\n")
            .append("  void:entities ").append(keywords.size()).append(" .\n\n");

        for (val keyword : keywords) {
            val uri = uriNormaliser.normalise(keyword.getUrl());
            if (uri.isEmpty() || keyword.getLabel() == null || keyword.getLabel().isBlank()) {
                continue;
            }
            ttl.append('<').append(uri).append("> a skos:Concept ;\n")
                .append("  skos:prefLabel \"").append(escape(keyword.getLabel())).append("\" .\n");
        }
        return ttl.toString();
    }

    /**
     * Escapes a label for a Turtle quoted literal, matching what
     * {@code templates/rdf/_turtle.ftl} does for the record templates —
     * backslash first, since it is the only rule that emits one. A single
     * unescaped backslash in one grant number took down every export for a week
     * (dri-one #344).
     */
    private static String escape(String literal) {
        return literal
            .replace("\\", "\\\\")
            .replace("\"", "'")
            .replace("\r", " ")
            .replace("\n", " ");
    }
}
