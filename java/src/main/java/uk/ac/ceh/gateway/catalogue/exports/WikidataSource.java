package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * What Wikidata says about the concepts the records use as subjects.
 *
 * <p>The 2,064 Wikidata URIs in the catalogue's graph are referenced 2,980 times
 * and would otherwise carry nothing but the reference: no label, no description,
 * no indication of what kind of thing they are. A consumer reading
 * {@code dcterms:subject <…/entity/Q663181>} could learn nothing without leaving
 * the endpoint. It now finds "Speckled Wood", a species of insect, with its
 * taxon name.
 *
 * <h2>Why this one batches</h2>
 *
 * <p>This is the source where asking per entity fails outright: a single entity's
 * RDF is <b>324 KB</b> (measured on {@code Q26612}), so dereferencing the
 * referenced set would move roughly <b>670 MB</b> across the network every time
 * the cache aged out — to extract a label, a description and a type.
 *
 * <p>One {@code CONSTRUCT} against the Query Service names 500 entities in a
 * {@code VALUES} clause and returns only the handful of properties wanted:
 * measured at <b>10 seconds and 109 KB</b> for 500, which puts the whole
 * referenced set at five queries and half a megabyte. Three orders of magnitude
 * less traffic for the same answers.
 *
 * <h2>What is asked for, and why</h2>
 *
 * <p>These URIs are overwhelmingly species — 495 of a 500-entity sample carry a
 * taxon name — which shapes the query: the English label and description, so a
 * keyword reads as something; English aliases, which are the common-name synonyms
 * a keyword search would otherwise miss; {@code P225}, the taxon name, because 10
 * of that sample have <em>no</em> English label at all and the scientific name is
 * the only name half of them have; and {@code P31}, what the thing is, with the
 * label of the type itself.
 */
@Profile("exports")
@Component
class WikidataSource implements AuthoritySource {

    /** The namespace Wikidata mints, and the one the catalogue references. */
    static final String PREFIX = "http://www.wikidata.org/entity/";

    /**
     * An entity id, and the only shape worth asking about.
     *
     * <p>Production holds two URIs under this namespace that are not entities: a
     * bare {@code http://www.wikidata.org/entity} with no id at all, and
     * {@code .../entity/(Q1054552} with a stray opening parenthesis. Neither will
     * ever resolve. Both are dropped here rather than sent, because a
     * {@code VALUES} clause would accept them silently and the result would be a
     * remembered empty answer for a typo.
     */
    private static final Pattern ENTITY_ID = Pattern.compile("Q[1-9][0-9]*");

    private static final String INSTANCE_OF = SourceGraphs.WDT + "P31";

    private final String endpoint;
    private final String userAgent;
    private final int batchSize;
    private final int queriesPerRun;

    WikidataSource(
        @Value("${wikidata.endpoint:https://query.wikidata.org/sparql}") String endpoint,
        @Value("${wikidata.userAgent}") String userAgent,
        @Value("${wikidata.batchSize:500}") int batchSize,
        @Value("${wikidata.queriesPerRun:8}") int queriesPerRun
    ) {
        this.endpoint = endpoint;
        this.userAgent = userAgent;
        this.batchSize = batchSize;
        this.queriesPerRun = queriesPerRun;
    }

    @Override
    public String graph() {
        return PREFIX;
    }

    @Override
    public String title() {
        return "Wikidata, the free knowledge base";
    }

    @Override
    public String description() {
        return "Subject concepts as Wikidata describes them: English label, aliases and description, "
            + "what kind of thing each is, and the taxon name where it is a species.";
    }

    @Override
    public List<String> vocabularies() {
        // schema:description and the wdt: properties as well as SKOS. The taxon
        // name is wdt:P225 and is the only name a tenth of these entities have,
        // so a consumer told to expect SKOS alone would miss it.
        return List.of(SKOS.getURI(), RDFS.getURI(), SourceGraphs.SCHEMA, SourceGraphs.WDT);
    }

    @Override
    public String licence() {
        // The one licence that is genuinely simple: Wikidata releases all of its
        // structured data under CC0.
        return SourceGraphs.CC0;
    }

    @Override
    public boolean describes(String iri) {
        return iri.startsWith(PREFIX)
            && ENTITY_ID.matcher(iri.substring(PREFIX.length())).matches();
    }

    @Override
    public Duration maxAge() {
        // Labels and descriptions are edited, but a species' name is not
        // fast-moving, and at five queries a fill there is no reason to be
        // miserly either way.
        return Duration.ofDays(30);
    }

    @Override
    public int requestsPerRun() {
        // Queries, not entities. Eight covers the referenced set five times
        // over, so a first fill completes in one run with headroom as it grows.
        return queriesPerRun;
    }

    @Override
    public int batchSize() {
        return batchSize;
    }

    @Override
    public Request request(List<String> batch) {
        // POST because the CONSTRUCT is too long for a query string, and the
        // Query Service asks every client to identify itself and blocks those
        // that do not. That is not optional politeness.
        return Request.post(endpoint, "text/turtle", "application/sparql-query",
            construct(batch), Map.of(HttpHeaders.USER_AGENT, userAgent));
    }

    /**
     * Splits the one response per entity.
     *
     * <p>A parse failure is deliberately not caught: a whole batch of 500 rides
     * on this query, so an unreadable body must count as transient and hold the
     * graph back rather than being remembered as 500 negatives.
     */
    @Override
    public Map<String, Model> describe(List<String> batch, String body) {
        val result = ModelFactory.createDefaultModel();
        RDFDataMgr.read(result, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
            Lang.TURTLE);

        val byIri = new LinkedHashMap<String, Model>();
        for (val iri : batch) {
            byIri.put(iri, descriptionOf(result, iri));
        }
        return byIri;
    }

    /**
     * One entity's statements, plus the labels of the types it is an instance of.
     *
     * <p>That second part is a deliberate one-hop expansion, and deliberately
     * bounded: only the objects of {@code P31}, and only their labels. Without it
     * {@code P31} points at an entity nothing in the graph names, so a consumer
     * reading "instance of Q16521" learns nothing. With it, and with nine
     * distinct types across five hundred entities, the whole graph gains nine
     * extra triples.
     */
    private static Model descriptionOf(Model result, String iri) {
        val description = ModelFactory.createDefaultModel();
        val entity = result.getResource(iri);
        result.listStatements(entity, null, (RDFNode) null)
            .forEachRemaining(description::add);
        if (description.isEmpty()) {
            // Asked, and Wikidata holds nothing under that id -- a deleted or
            // merged entity. The retriever remembers that rather than asking
            // again every run.
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
