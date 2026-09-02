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
import java.util.Collection;
import java.util.List;
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
 * <h2>No cache</h2>
 *
 * <p>Deliberately none. The catalogue references 132 concepts across these
 * three vocabularies, and the export runs once a day: a hundred requests daily
 * does not justify a persistent store, its refresh policy, or its staleness
 * bugs. If the referenced set grows by an order of magnitude this is the first
 * thing to revisit.
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

    private final RestTemplate restTemplate;
    private final String ukcehSparqlEndpoint;

    public SkosConceptRetriever(
        @Qualifier("authorities") RestTemplate restTemplate,
        @Value("${ukceh.sparql.endpoint}") String ukcehSparqlEndpoint
    ) {
        this.restTemplate = restTemplate;
        this.ukcehSparqlEndpoint = ukcehSparqlEndpoint;
        log.info("Creating");
    }

    /**
     * @param conceptUris the concepts to describe, all from one authority
     * @param retrieval   how that authority publishes its descriptions
     * @return a model holding only {@link #PUBLISHED} statements about those
     *         concepts. Concepts that could not be retrieved are simply absent —
     *         the caller decides whether too many are missing to publish.
     */
    public Model describe(Collection<String> conceptUris, Retrieval retrieval) {
        return retrieval == Retrieval.UKCEH_SPARQL
            ? viaSparql(conceptUris)
            : viaContentNegotiation(conceptUris);
    }

    private Model viaContentNegotiation(Collection<String> conceptUris) {
        val combined = ModelFactory.createDefaultModel();
        var failed = 0;
        for (val conceptUri : conceptUris) {
            val retrieved = fetchTurtle(conceptUri);
            if (retrieved == null) {
                failed++;
                continue;
            }
            copyStatementsAbout(retrieved, conceptUri, combined);
        }
        if (failed > 0) {
            log.warn("Could not retrieve {} of {} concept descriptions", failed, conceptUris.size());
        }
        return combined;
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
            val filtered = ModelFactory.createDefaultModel();
            conceptUris.forEach(uri -> copyStatementsAbout(retrieved, uri, filtered));
            return filtered;
        } catch (Exception ex) {
            log.warn("Could not retrieve {} concept descriptions from {}: {}",
                conceptUris.size(), ukcehSparqlEndpoint, ex.getMessage());
            return ModelFactory.createDefaultModel();
        }
    }

    /** Copies the published SKOS statements about one concept, and its type. */
    private static void copyStatementsAbout(Model source, String conceptUri, Model target) {
        val concept = source.getResource(conceptUri);
        if (!source.containsResource(concept)) {
            return;
        }
        target.add(target.getResource(conceptUri), RDF.type, SKOS.Concept);
        source.listStatements(concept, null, (RDFNode) null).forEachRemaining(statement -> {
            if (PUBLISHED.contains(statement.getPredicate())
                // A literal, or a URI: never a blank node, which would drag the
                // authority's internal structure in behind it.
                && !statement.getObject().isAnon()) {
                target.add(statement);
            }
        });
    }
}
