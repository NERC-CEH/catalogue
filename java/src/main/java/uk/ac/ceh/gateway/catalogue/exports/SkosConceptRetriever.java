package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Retrieves what an authority says about its own concepts.
 *
 * <p>Phase 1 of dri-one #350 published the labels the application already held
 * locally. This fetches the rest — definitions, alternate labels, and the
 * broader/narrower hierarchy — from the authorities themselves, for the
 * vocabularies that publish it in a form we can read.
 *
 * <h2>Only what is said about the concept</h2>
 *
 * <p>An authority's response describes more than the concept asked for. NVS
 * returns registry provenance and mappings to other vocabularies; AGROVOC
 * returns its own subvocabulary structure. Republishing all of it would mean
 * carrying blank-node structures and assertions about third-party URIs that
 * nobody asked us to mirror, and would make the graph's size a function of
 * whatever the authority happens to include.
 *
 * <p>So the retrieved model is reduced to {@link #PUBLISHED} — the SKOS
 * properties that describe a concept — with the concept itself as subject.
 * Everything else is discarded.
 *
 * <h2>Why there is a cache, having argued there should not be</h2>
 *
 * <p>Phase 2 shipped without one, on the grounds that 132 concepts against a
 * daily export is a hundred requests a day and not worth a persistent store.
 * That reasoning was about politeness and cost, and on those terms it still
 * holds. What it missed is that the cache is load-bearing for correctness.
 *
 * <p>The export publishes each graph with a single PUT, which replaces it. So a
 * run in which a third of NVS happened to time out did not merely fetch less —
 * it replaced a complete graph with a partial one, and the endpoint lost
 * descriptions until the next successful run put them back. Without somewhere
 * to keep the previous answer there is nothing to fall back on, and no way for
 * the caller to tell a thin run from a genuinely smaller vocabulary.
 *
 * <p>So a concept's description is kept, and a copy of any age is used when the
 * authority cannot be reached. A transient failure then costs freshness rather
 * than content, which is the whole point.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = "restTemplate")
public class SkosConceptRetriever {

    /** How an authority's concept descriptions can be obtained. */
    public enum Retrieval {
        /** Dereference the concept URI itself, asking for Turtle. */
        CONTENT_NEGOTIATION,
        /** One CONSTRUCT against the UKCEH vocabulary server for the whole batch. */
        UKCEH_SPARQL
    }

    /**
     * The properties worth republishing. Deliberately a whitelist: an authority
     * may add anything to its own records, and a graph that mirrors all of it
     * stops being a description of the concepts the catalogue references.
     */
    private static final Set<Property> PUBLISHED = Set.of(
        SKOS.prefLabel, SKOS.altLabel, SKOS.definition, SKOS.note,
        SKOS.notation, SKOS.broader, SKOS.narrower, SKOS.related, SKOS.inScheme
    );

    /**
     * How old a cached concept description may be before it is fetched again.
     *
     * <p>Shorter than the fortnight {@link IdentityRetriever#MAX_AGE} allows a
     * person, and deliberately: a researcher's name is settled, whereas a
     * thesaurus can gain a definition or move a concept in its hierarchy on any
     * release. At 132 concepts a week this is still a couple of dozen requests
     * a day.
     */
    static final Duration MAX_AGE = Duration.ofDays(7);

    /**
     * The age limit for the fallback: any copy at all. Spelt this way rather
     * than as some large number of days chosen to look reasonable, because the
     * intent is explicitly "whatever you have".
     */
    private static final Duration FOREVER = ChronoUnit.FOREVER.getDuration();

    private final RestTemplate restTemplate;
    private final String ukcehSparqlEndpoint;
    private final DescriptionCache cache;

    public SkosConceptRetriever(
        @Qualifier("authorities") RestTemplate restTemplate,
        @Value("${ukceh.sparql.endpoint}") String ukcehSparqlEndpoint,
        DescriptionCache cache
    ) {
        this.restTemplate = restTemplate;
        this.ukcehSparqlEndpoint = ukcehSparqlEndpoint;
        this.cache = cache;
        log.info("Creating");
    }

    /**
     * @param conceptUris the concepts to describe, all from one authority
     * @param retrieval   how that authority publishes its descriptions
     * @return a model holding only {@link #PUBLISHED} statements about those
     *         concepts, taken from the cache where a copy is fresh and from the
     *         authority otherwise. A concept absent from the result is one
     *         neither source could supply — the caller decides what to do about
     *         that, but a later run cannot help it either.
     */
    public Model describe(Collection<String> conceptUris, Retrieval retrieval) {
        val combined = ModelFactory.createDefaultModel();
        val wanted = new ArrayList<String>();
        var cached = 0;

        // Dropped before anything else touches them. A concept URI is
        // interpolated into the SPARQL VALUES clause below, so one containing a
        // brace, a pipe or a backslash makes the whole query a syntax error --
        // which returns nothing, which the caller's publish-whole-or-not-at-all
        // guard turns into a permanently frozen graph. One bad keyword in one
        // record would stop a whole vocabulary indefinitely.
        //
        // It is also not a usable cache key: the cache stores a description in a
        // named graph keyed by this string.
        val usable = conceptUris.stream().filter(Iris::isPublishable).toList();
        if (usable.size() < conceptUris.size()) {
            log.warn("Ignoring {} concept URIs that are not usable as IRIs: {}",
                conceptUris.size() - usable.size(),
                conceptUris.stream().filter(uri -> !Iris.isPublishable(uri)).toList());
        }

        for (val conceptUri : usable) {
            val fresh = cache.get(conceptUri, MAX_AGE);
            if (fresh.isPresent()) {
                combined.add(fresh.get());
                cached++;
            } else {
                wanted.add(conceptUri);
            }
        }
        if (wanted.isEmpty()) {
            log.info("{} concept descriptions, all from cache", cached);
            return combined;
        }

        val responses = responses(wanted, retrieval);

        var fetched = 0;
        var stale = 0;
        var unavailable = 0;
        for (val conceptUri : wanted) {
            val description = ModelFactory.createDefaultModel();
            // Each concept is read from the response that described it, never
            // from the union of the batch. Sharing one model across concepts
            // let a statement in B's response be published as though A's
            // authority had asserted it, and -- worse -- made A look described
            // when its own fetch had failed, which cached the mistake.
            val response = responses.get(conceptUri);
            if (response != null) {
                copyStatementsAbout(response, conceptUri, description);
            }
            if (!description.isEmpty()) {
                cache.put(conceptUri, description);
                combined.add(description);
                fetched++;
                continue;
            }
            // Nothing came back for this one. A copy of any age is better than
            // dropping the concept from the graph: this is the case that used to
            // shrink a published graph whenever a vocabulary server had a bad
            // minute. Note the description is NOT re-cached, so its age keeps
            // counting and the next run tries the authority again.
            val held = cache.get(conceptUri, FOREVER);
            if (held.isPresent()) {
                combined.add(held.get());
                stale++;
            } else {
                unavailable++;
            }
        }
        if (fetched > 0) {
            cache.save();
        }
        log.info("{} concept descriptions: {} fetched, {} from cache, {} stale, {} unavailable",
            usable.size(), fetched, cached, stale, unavailable);
        return combined;
    }

    /**
     * What each concept was described by, keyed by concept. A concept absent
     * from the map was not described at all.
     *
     * <p>The two transports differ in how far a response can legitimately
     * reach. Dereferencing gives one response per concept, and only that
     * response may describe it. The SPARQL CONSTRUCT names every concept in the
     * batch, so its single document legitimately describes all of them — but
     * even there the per-concept extraction matters, because a concept appears
     * in that document as the object of its neighbours' {@code skos:broader}
     * whether or not the store held anything about it.
     */
    private Map<String, Model> responses(Collection<String> conceptUris, Retrieval retrieval) {
        if (retrieval == Retrieval.UKCEH_SPARQL) {
            val batch = viaSparql(conceptUris);
            val byConcept = new LinkedHashMap<String, Model>();
            conceptUris.forEach(uri -> byConcept.put(uri, batch));
            return byConcept;
        }
        val byConcept = new LinkedHashMap<String, Model>();
        var failed = 0;
        for (val conceptUri : conceptUris) {
            val retrieved = fetchTurtle(conceptUri);
            if (retrieved == null) {
                failed++;
                continue;
            }
            byConcept.put(conceptUri, retrieved);
        }
        if (failed > 0) {
            log.warn("Could not retrieve {} of {} concept descriptions", failed, conceptUris.size());
        }
        return byConcept;
    }

    private Model fetchTurtle(String conceptUri) {
        try {
            val headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.valueOf("text/turtle")));
            val response = restTemplate.exchange(
                conceptUri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            val body = response.getBody();
            if (body == null || body.isBlank()) {
                return null;
            }
            val model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), Lang.TURTLE);
            return model;
        } catch (Exception ex) {
            // Deliberately wide. A vocabulary server can fail in every way an
            // HTTP call can, and can serve RDF that does not parse; none of that
            // should stop the other concepts, or the export.
            log.debug("Could not retrieve {}: {}", conceptUri, ex.getMessage());
            return null;
        }
    }

    /**
     * One CONSTRUCT for the whole batch. The UKCEH server holds these concepts
     * directly, so asking 32 times over HTTP would be wasteful where a single
     * query with a VALUES clause does the same job.
     */
    private Model viaSparql(Collection<String> conceptUris) {
        if (conceptUris.isEmpty()) {
            return ModelFactory.createDefaultModel();
        }
        val values = conceptUris.stream().map(uri -> "<" + uri + ">").reduce("", (a, b) -> a + " " + b);
        val query = """
            CONSTRUCT { ?concept ?p ?o }
            WHERE {
              VALUES ?concept { %s }
              GRAPH ?g { ?concept ?p ?o }
            }
            """.formatted(values);
        try {
            val headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.valueOf("text/turtle")));
            val response = restTemplate.exchange(
                ukcehSparqlEndpoint + "?query={query}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class,
                query
            );
            val body = response.getBody();
            if (body == null || body.isBlank()) {
                return ModelFactory.createDefaultModel();
            }
            val retrieved = ModelFactory.createDefaultModel();
            RDFDataMgr.read(retrieved, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), Lang.TURTLE);
            return retrieved;
        } catch (Exception ex) {
            log.warn("Could not retrieve {} concept descriptions from {}: {}",
                conceptUris.size(), ukcehSparqlEndpoint, ex.getMessage());
            return ModelFactory.createDefaultModel();
        }
    }

    /**
     * Copies the published SKOS statements about one concept, and its type.
     *
     * <p>Nothing is written unless at least one such statement was found. The
     * test used to be {@code containsResource}, which is true when the concept
     * appears <em>anywhere</em> in the source — including as the object of
     * someone else's {@code skos:broader}. Being mentioned by a neighbour is not
     * being described, and treating it as such made an empty extraction look
     * like a successful retrieval, so the type triple alone was cached over a
     * good description and the fallback to the stored copy was skipped.
     */
    private static void copyStatementsAbout(Model source, String conceptUri, Model target) {
        val concept = source.getResource(conceptUri);
        val published = source.listStatements(concept, null, (RDFNode) null).toList().stream()
            .filter(statement -> PUBLISHED.contains(statement.getPredicate()))
            // A literal, or a URI: never a blank node, which would drag the
            // authority's internal structure in behind it.
            .filter(statement -> !statement.getObject().isAnon())
            .toList();
        if (published.isEmpty()) {
            return;
        }
        target.add(target.getResource(conceptUri), RDF.type, SKOS.Concept);
        published.forEach(target::add);
    }
}
