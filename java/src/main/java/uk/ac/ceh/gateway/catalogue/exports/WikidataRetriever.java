package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Retrieves what Wikidata says about the concepts the records use as subjects —
 * dri-one #350 phase 5, and the last of them.
 *
 * <h2>Why this one cannot dereference</h2>
 *
 * <p>Every earlier phase asks its authority one question per entity. Wikidata is
 * the source where that fails outright: a single entity's RDF is <b>324 KB</b>
 * (measured on {@code Q26612}), so dereferencing the 2,064 entities the
 * catalogue references would move roughly <b>670 MB</b> across the network every
 * time the cache aged out — to extract a label, a description and a type.
 *
 * <p>So this batches instead. One {@code CONSTRUCT} against the Wikidata Query
 * Service names 500 entities in a {@code VALUES} clause and returns only the
 * handful of properties wanted: measured at <b>10 seconds and 109 KB</b> for
 * 500, which puts the whole referenced set at five queries and half a megabyte.
 * Three orders of magnitude less traffic for the same answers.
 *
 * <h2>What is asked for, and why</h2>
 *
 * <p>These URIs are almost entirely {@code dcterms:subject} keywords — 2,980
 * references from 2,064 entities — and they are overwhelmingly species: 495 of
 * a 500-entity sample carry a taxon name. That shapes the query:
 *
 * <ul>
 *   <li>the English label and description, so a keyword reads as something;</li>
 *   <li>English aliases, which are the common-name synonyms a keyword search
 *       would otherwise miss — measured at 0.6 per entity, so cheap;</li>
 *   <li>{@code P225}, the taxon name, because 10 of that sample have <em>no</em>
 *       English label at all and the scientific name is the only name half of
 *       them have — and arguably the better identifier for a species anyway;</li>
 *   <li>{@code P31}, what the thing is, together with the label of the type
 *       itself. There are only <b>9 distinct types</b> across 500 entities, so
 *       making {@code P31} readable costs almost nothing.</li>
 * </ul>
 *
 * <p>Everything else Wikidata holds is left there. The 324 KB an entity carries
 * is statements, qualifiers, references and sitelinks in every language; a
 * keyword needs none of it.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = {"restTemplate", "cache"})
public class WikidataRetriever {

    /** The namespace Wikidata mints, and the one the catalogue references. */
    static final String PREFIX = "http://www.wikidata.org/entity/";

    /**
     * An entity id, and the only shape worth asking about.
     *
     * <p>Production holds two URIs under this namespace that are not entities:
     * a bare {@code http://www.wikidata.org/entity} with no id at all, and
     * {@code .../entity/(Q1054552} with a stray opening parenthesis. Neither
     * will ever resolve. Both are dropped here rather than sent, because a
     * {@code VALUES} clause would accept them silently and the result would be
     * a remembered empty answer for a typo.
     */
    private static final Pattern ENTITY_ID = Pattern.compile("Q[1-9][0-9]*");

    private static final String WDT = "http://www.wikidata.org/prop/direct/";
    private static final String INSTANCE_OF = WDT + "P31";

    /**
     * How old a held description may be before it is asked for again. Labels and
     * descriptions are edited, but a species' name is not fast-moving, and at
     * five queries a fill there is no reason to be miserly either way.
     */
    static final Duration MAX_AGE = Duration.ofDays(30);

    /** The age limit for the fallback: any copy at all, however old. */
    private static final Duration FOREVER = ChronoUnit.FOREVER.getDuration();

    private final RestTemplate restTemplate;
    private final DescriptionCache cache;
    private final String endpoint;
    private final String userAgent;
    private final int batchSize;
    private final int queriesPerRun;

    public WikidataRetriever(
        @Qualifier("authorities") RestTemplate restTemplate,
        DescriptionCache cache,
        @Value("${wikidata.endpoint:https://query.wikidata.org/sparql}") String endpoint,
        @Value("${wikidata.userAgent}") String userAgent,
        @Value("${wikidata.batchSize:500}") int batchSize,
        @Value("${wikidata.queriesPerRun:8}") int queriesPerRun
    ) {
        this.restTemplate = restTemplate;
        this.cache = cache;
        this.endpoint = endpoint;
        this.userAgent = userAgent;
        this.batchSize = batchSize;
        this.queriesPerRun = queriesPerRun;
        log.info("Creating for {} in batches of {}, at most {} queries a run",
            endpoint, batchSize, queriesPerRun);
    }

    /** Whether this IRI is a Wikidata entity worth asking about. */
    public static boolean describes(String iri) {
        return iri.startsWith(PREFIX)
            && ENTITY_ID.matcher(iri.substring(PREFIX.length())).matches();
    }

    /** @see IdentityRetriever.Descriptions for why both counts are reported. */
    public record Descriptions(Model model, int deferred, int transientFailures) {
        public boolean isEmpty() {
            return model.isEmpty();
        }

        public boolean isComplete() {
            return deferred == 0 && transientFailures == 0;
        }
    }

    public Descriptions describe(Collection<String> iris) {
        val combined = ModelFactory.createDefaultModel();
        val wanted = new ArrayList<String>();
        var cached = 0;

        for (val iri : iris) {
            val fresh = cache.get(iri, MAX_AGE);
            if (fresh.isPresent()) {
                combined.add(fresh.get());
                cached++;
            } else {
                wanted.add(iri);
            }
        }
        if (wanted.isEmpty()) {
            log.info("Wikidata: {} descriptions, all from cache", cached);
            return new Descriptions(combined, 0, 0);
        }

        var fetched = 0;
        var absent = 0;
        var deferred = 0;
        var transientFailures = 0;
        var queries = 0;

        for (var start = 0; start < wanted.size(); start += batchSize) {
            val batch = wanted.subList(start, Math.min(start + batchSize, wanted.size()));

            if (queries >= queriesPerRun) {
                // Out of queries for this run. Anything held is still better
                // than nothing, and the rest are picked up by the next run.
                for (val iri : batch) {
                    val held = cache.get(iri, FOREVER);
                    if (held.isPresent()) {
                        combined.add(held.get());
                        cached++;
                    } else {
                        deferred++;
                    }
                }
                continue;
            }

            queries++;
            val result = query(batch);
            if (result == null) {
                // The whole batch failed, so 500 entities are undescribed at
                // once. A copy of any age beats losing them from the graph.
                for (val iri : batch) {
                    val held = cache.get(iri, FOREVER);
                    if (held.isPresent()) {
                        combined.add(held.get());
                        cached++;
                    } else {
                        transientFailures++;
                    }
                }
                continue;
            }

            for (val iri : batch) {
                val description = descriptionOf(result, iri);
                cache.put(iri, description);
                combined.add(description);
                if (description.isEmpty()) {
                    // Asked, and Wikidata holds nothing under that id — a
                    // deleted or merged entity. Remembered, so it is not asked
                    // again every run.
                    absent++;
                } else {
                    fetched++;
                }
            }
        }
        log.info("Wikidata: {} of {} fetched in {} queries, {} from cache, {} deferred, "
                + "{} temporarily unavailable, {} not held by Wikidata",
            fetched, iris.size(), queries, cached, deferred, transientFailures, absent);
        if (fetched > 0) {
            cache.save();
        }
        return new Descriptions(combined, deferred, transientFailures);
    }

    /**
     * One entity's statements, plus the labels of the types it is an instance
     * of.
     *
     * <p>That second part is a deliberate one-hop expansion, and deliberately
     * bounded: only the objects of {@code P31}, and only their labels. Without
     * it {@code P31} points at an entity nothing in the graph names, so a
     * consumer reading "instance of Q16521" learns nothing. With it, and with
     * nine distinct types across five hundred entities, the whole graph gains
     * nine extra triples.
     */
    private static Model descriptionOf(Model result, String iri) {
        val description = ModelFactory.createDefaultModel();
        val entity = result.getResource(iri);
        result.listStatements(entity, null, (RDFNode) null)
            .forEachRemaining(description::add);
        if (description.isEmpty()) {
            return description;
        }
        val instanceOf = result.getProperty(INSTANCE_OF);
        result.listObjectsOfProperty(entity, instanceOf).forEachRemaining(type -> {
            if (type.isURIResource()) {
                copyLabels(result, type.asResource(), description);
            }
        });
        return description;
    }

    private static void copyLabels(Model result, Resource type, Model description) {
        result.listStatements(type, RDFS.label, (RDFNode) null)
            .forEachRemaining(description::add);
    }

    /**
     * @return the batch's descriptions, or null if the query could not be run —
     *         which the caller must treat as transient, since a whole batch of
     *         500 entities rides on it
     */
    private Model query(List<String> batch) {
        try {
            val headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.valueOf("text/turtle")));
            headers.setContentType(MediaType.valueOf("application/sparql-query"));
            // The Wikidata Query Service asks every client to identify itself
            // and blocks those that do not. This is not optional politeness.
            headers.set(HttpHeaders.USER_AGENT, userAgent);

            val response = restTemplate.exchange(
                URI.create(endpoint), HttpMethod.POST,
                new HttpEntity<>(construct(batch), headers), String.class);
            val body = response.getBody();
            if (body == null || body.isBlank()) {
                log.warn("Empty response from {} for a batch of {}", endpoint, batch.size());
                return null;
            }
            val parsed = ModelFactory.createDefaultModel();
            RDFDataMgr.read(parsed, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
                Lang.TURTLE);
            return parsed;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                val retryAfter = ex.getResponseHeaders() == null
                    ? null : ex.getResponseHeaders().getFirst("Retry-After");
                log.warn("Wikidata rate limited us (429){}",
                    retryAfter == null ? "" : ", Retry-After: " + retryAfter);
            } else {
                log.warn("{} returned {} for a batch of {}", endpoint, ex.getStatusCode(), batch.size());
            }
            return null;
        } catch (HttpServerErrorException ex) {
            log.warn("{} returned {} for a batch of {}", endpoint, ex.getStatusCode(), batch.size());
            return null;
        } catch (Exception ex) {
            // A batch of 500 measured at 10 seconds against a 30-second read
            // timeout, so a timeout here means the service is struggling rather
            // than that the batch is too big.
            log.warn("Could not query {}: {}", endpoint, ex.getMessage());
            return null;
        }
    }

    /**
     * The query. Every clause is {@code OPTIONAL} because an entity missing one
     * of these must not drop out of the result entirely — 10 of a 500-entity
     * sample have no English label, and losing them would be losing exactly the
     * entities the taxon name exists to rescue.
     */
    private static String construct(List<String> batch) {
        val values = new StringBuilder();
        batch.forEach(iri -> values.append('<').append(iri).append("> "));
        return """
            PREFIX wdt: <http://www.wikidata.org/prop/direct/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
            PREFIX schema: <http://schema.org/>
            CONSTRUCT {
              ?item rdfs:label ?label ;
                    skos:prefLabel ?label ;
                    skos:altLabel ?alias ;
                    schema:description ?description ;
                    wdt:P31 ?type ;
                    wdt:P225 ?taxonName .
              ?type rdfs:label ?typeLabel .
            }
            WHERE {
              VALUES ?item { %s }
              OPTIONAL { ?item rdfs:label ?label FILTER(LANG(?label) = "en") }
              OPTIONAL { ?item skos:altLabel ?alias FILTER(LANG(?alias) = "en") }
              OPTIONAL { ?item schema:description ?description FILTER(LANG(?description) = "en") }
              OPTIONAL { ?item wdt:P225 ?taxonName }
              OPTIONAL {
                ?item wdt:P31 ?type .
                OPTIONAL { ?type rdfs:label ?typeLabel FILTER(LANG(?typeLabel) = "en") }
              }
            }
            """.formatted(values.toString().trim());
    }
}
