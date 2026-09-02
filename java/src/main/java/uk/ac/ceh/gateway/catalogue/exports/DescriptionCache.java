package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Remembers what an authority said about an entity, so the export does not ask
 * again on every run.
 *
 * <p>Phase 2 of dri-one #350 deliberately had no cache: 132 concepts against a
 * daily export is a hundred requests a day, and a persistent store would have
 * been more machinery than the volume justified. Phase 3 changes that. ORCID
 * publishes no bulk endpoint, so 2,125 researchers means 2,125 requests, and
 * with ROR that is 2,686 per export — enough that asking again daily is both
 * impolite to the authorities and minutes added to every run.
 *
 * <p>So each entity's description is held in its own named graph, keyed by the
 * entity's URI, with the time it was retrieved recorded alongside. A steady
 * state is a few dozen requests a day rather than 2,686: only entities that are
 * new, or whose copy has aged past the caller's limit, are fetched.
 *
 * <h2>Staleness is the caller's decision</h2>
 *
 * <p>This deliberately has no expiry policy of its own. How old a copy may be
 * before it is refetched differs by authority — a researcher's name changes
 * rarely, a vocabulary can be revised on any release — so {@link #get} takes
 * the age the caller will accept and this only reports what it holds and when
 * it was taken. Nothing is ever evicted: a stale copy is more useful than none
 * when an authority is unreachable, and the caller can choose to use it.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = "dataset")
public class DescriptionCache {

    /**
     * Where the retrieval times live. A graph of its own so that a cached
     * description holds only what the authority said, and nothing of ours.
     */
    private static final String METADATA_GRAPH = "urn:x-catalogue:description-cache";
    private static final String RETRIEVED_AT = "urn:x-catalogue:retrievedAt";

    private final Dataset dataset;
    private final Clock clock;

    /**
     * Annotated because there are two constructors and Spring will not choose
     * between them: without it the context fails to start with "no default
     * constructor found".
     */
    @Autowired
    public DescriptionCache(@Qualifier("descriptionCacheDataset") Dataset dataset) {
        this(dataset, Clock.systemUTC());
    }

    DescriptionCache(Dataset dataset, Clock clock) {
        this.dataset = dataset;
        this.clock = clock;
        log.info("Creating");
    }

    /**
     * @param uri    the entity whose description is wanted
     * @param maxAge how old a copy the caller will accept
     * @return the cached description, or empty if there is none or it is older
     *         than {@code maxAge}
     */
    public Optional<Model> get(String uri, Duration maxAge) {
        dataset.begin(ReadWrite.READ);
        try {
            val retrievedAt = retrievedAt(uri);
            if (retrievedAt.isEmpty()) {
                return Optional.empty();
            }
            if (Duration.between(retrievedAt.get(), Instant.now(clock)).compareTo(maxAge) > 0) {
                return Optional.empty();
            }
            // No containsNamedModel guard here on purpose. TDB2 does not report
            // an empty named graph as present, so testing for it would lose
            // exactly the negative result this cache exists to remember: that
            // the authority was reached and had nothing to say. The timestamp is
            // the record of having asked, so it is the only thing worth checking.
            //
            // Copied out, so the caller holds a model that outlives this
            // transaction. A TDB2 model is only valid inside one.
            val copy = ModelFactory.createDefaultModel();
            copy.add(dataset.getNamedModel(uri));
            return Optional.of(copy);
        } finally {
            dataset.end();
        }
    }

    /**
     * Records what an authority said about an entity, replacing any previous
     * copy. An empty description is stored too, and deliberately: it says the
     * authority was reached and had nothing to say, which is worth remembering
     * rather than retrying every run.
     */
    public void put(String uri, Model description) {
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.replaceNamedModel(uri, description);
            val metadata = dataset.getNamedModel(METADATA_GRAPH);
            val entity = metadata.getResource(uri);
            val property = metadata.getProperty(RETRIEVED_AT);
            metadata.removeAll(entity, property, null);
            metadata.add(entity, property, metadata.createTypedLiteral(
                Instant.now(clock).toString(), "http://www.w3.org/2001/XMLSchema#dateTime"));
            dataset.commit();
        } catch (Exception ex) {
            dataset.abort();
            // A cache that cannot be written is a slow export, not a broken one.
            log.warn("Could not cache the description of {}: {}", uri, ex.getMessage());
        } finally {
            dataset.end();
        }
    }

    /** Must be called inside a transaction. */
    private Optional<Instant> retrievedAt(String uri) {
        if (!dataset.containsNamedModel(METADATA_GRAPH)) {
            return Optional.empty();
        }
        val metadata = dataset.getNamedModel(METADATA_GRAPH);
        val statements = metadata.listStatements(
            metadata.getResource(uri), metadata.getProperty(RETRIEVED_AT), (RDFNode) null);
        if (!statements.hasNext()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(statements.next().getObject().asLiteral().getString()));
        } catch (Exception ex) {
            log.debug("Unreadable cache timestamp for {}, treating as absent", uri);
            return Optional.empty();
        }
    }

    /** For tests and diagnostics: how many descriptions are held. */
    public long size() {
        dataset.begin(ReadWrite.READ);
        try {
            var count = 0L;
            val names = dataset.listNames();
            while (names.hasNext()) {
                if (!METADATA_GRAPH.equals(names.next())) {
                    count++;
                }
            }
            return count;
        } finally {
            dataset.end();
        }
    }
}
