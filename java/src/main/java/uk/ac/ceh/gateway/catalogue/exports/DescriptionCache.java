package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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
 *
 * <h2>Surviving the pod, without putting a database on a file share</h2>
 *
 * <p>The store itself is pod-local and does not outlive the container: every
 * persistent volume in the cluster is a CIFS share mounted {@code nobrl}, with
 * byte-range locking disabled, and TDB2 relies on exactly that locking to keep
 * one JVM's hands off another's database. A single-replica Deployment is no
 * protection either — the default rolling update starts the new pod before the
 * old one stops, so two JVMs would briefly share the directory. Jena's own
 * guidance is that a TDB database must not live on a network filesystem, and
 * the precedent in this stack agrees: {@code jena.location} is unmounted and
 * Fuseki, which persists a real TDB2, declares no volumes at all.
 *
 * <p>So what goes on the share is not the database but a snapshot of it: one
 * N-Quads file, written whole and read whole. That is a plain sequential file
 * — no locking, no memory mapping, nothing CIFS handles badly — and it is what
 * lets a recreated pod start warm instead of spending days refetching 2,686
 * entities. N-Quads because it carries the graph names, so each entity's
 * description and the retrieval times come back in the graphs they were in.
 *
 * <p>The snapshot is treated as disposable throughout. If it is missing,
 * unreadable, half-written by a pod that died mid-save, or trampled by two pods
 * saving at once, the cache simply starts empty and refills — which is the
 * behaviour there was before it existed. Nothing here may ever fail an export.
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
    private final Path snapshot;
    /** Whether anything has been stored since the snapshot was last written. */
    private volatile boolean changed;

    /**
     * Annotated because there are two constructors and Spring will not choose
     * between them: without it the context fails to start with "no default
     * constructor found".
     */
    @Autowired
    public DescriptionCache(
        @Qualifier("descriptionCacheDataset") Dataset dataset,
        @Value("${jena.descriptionCache.snapshot:}") String snapshot
    ) {
        this(dataset, Clock.systemUTC(), snapshot);
    }

    DescriptionCache(Dataset dataset, Clock clock) {
        this(dataset, clock, "");
    }

    DescriptionCache(Dataset dataset, Clock clock, String snapshot) {
        this.dataset = dataset;
        this.clock = clock;
        this.snapshot = snapshot == null || snapshot.isBlank() ? null : Path.of(snapshot);
        log.info("Creating{}", this.snapshot == null ? " without a snapshot" : " with snapshot " + this.snapshot);
        load();
    }

    /**
     * Reads the snapshot back, if there is one. Called from the constructor
     * rather than a lifecycle hook so that the cache is never observable in a
     * half-loaded state: the first {@link #get} cannot run before this has.
     */
    private void load() {
        if (snapshot == null || !Files.isReadable(snapshot)) {
            return;
        }
        // Parsed into a dataset of its own first. A half-written file throws
        // part-way through, and it must not take the live cache with it.
        val loaded = DatasetFactory.create();
        try {
            RDFDataMgr.read(loaded, snapshot.toUri().toString(), Lang.NQUADS);
        } catch (Exception ex) {
            log.warn("Could not read the description cache snapshot at {}, starting empty: {}",
                snapshot, ex.getMessage());
            return;
        }
        dataset.begin(ReadWrite.WRITE);
        try {
            val names = loaded.listNames();
            var graphs = 0;
            while (names.hasNext()) {
                val name = names.next();
                dataset.replaceNamedModel(name, loaded.getNamedModel(name));
                graphs++;
            }
            dataset.commit();
            log.info("Recovered {} graphs from the description cache snapshot at {}", graphs, snapshot);
        } catch (Exception ex) {
            dataset.abort();
            log.warn("Could not load the description cache snapshot at {}: {}", snapshot, ex.getMessage());
        } finally {
            dataset.end();
        }
    }

    /**
     * Writes the snapshot, if anything has changed since the last time.
     *
     * <p>Called once per authority that fetched something, not once per
     * description — so up to five times in a run across the vocabularies and the
     * two identity authorities, each rewriting the whole file. At a few thousand
     * entities that is a couple of megabytes a time, which is why it is guarded
     * by the change flag rather than called unconditionally.
     *
     * <p>Written to a sibling temporary file and moved into place, so a reader
     * sees either the previous snapshot or the new one. The move is atomic where
     * the filesystem supports it; on a CIFS share it may not be, which is the
     * one window in which a crash could leave an unreadable file — recovered
     * from by {@link #load} starting empty.
     */
    public void save() {
        if (snapshot == null || !changed) {
            return;
        }
        // A unique name per write. Two pods overlap during a rolling update,
        // and a shared temporary path would let them interleave into one file.
        val temporary = snapshot.resolveSibling(
            snapshot.getFileName() + "." + UUID.randomUUID() + ".part");
        dataset.begin(ReadWrite.READ);
        try {
            if (snapshot.getParent() != null) {
                Files.createDirectories(snapshot.getParent());
            }
            try (val out = Files.newOutputStream(temporary)) {
                RDFDataMgr.write(out, dataset, Lang.NQUADS);
            }
            move(temporary, snapshot);
            changed = false;
            log.info("Wrote the description cache snapshot to {}", snapshot);
        } catch (Exception ex) {
            // A snapshot that cannot be written costs a cold start after the next
            // pod recreation. It must never cost the export.
            log.warn("Could not write the description cache snapshot to {}: {}", snapshot, ex.getMessage());
            try {
                Files.deleteIfExists(temporary);
            } catch (Exception ignored) {
                log.debug("Could not remove the partial snapshot at {}", temporary);
            }
        } finally {
            dataset.end();
        }
    }

    private static void move(Path from, Path to) throws Exception {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            // CIFS does not offer one. The replace is still a single operation as
            // far as this process is concerned; it is simply not guaranteed to be
            // one to a concurrent reader.
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * @param uri    the entity whose description is wanted
     * @param maxAge how old a copy the caller will accept
     * @return the cached description, or empty if there is none or it is older
     *         than {@code maxAge}
     */
    public Optional<Model> get(String uri, Duration maxAge) {
        try {
            dataset.begin(ReadWrite.READ);
        } catch (Exception ex) {
            // Nothing here may fail an export. A cache that cannot be read is a
            // slow export; an exception escaping into describe() would cost the
            // whole provider its graphs.
            log.warn("Could not read the description cache: {}", ex.getMessage());
            return Optional.empty();
        }
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
        } catch (Exception ex) {
            log.warn("Could not read the cached description of {}: {}", uri, ex.getMessage());
            return Optional.empty();
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
        try {
            dataset.begin(ReadWrite.WRITE);
        } catch (Exception ex) {
            log.warn("Could not open the description cache for writing: {}", ex.getMessage());
            return;
        }
        try {
            dataset.replaceNamedModel(uri, description);
            val metadata = dataset.getNamedModel(METADATA_GRAPH);
            val entity = metadata.getResource(uri);
            val property = metadata.getProperty(RETRIEVED_AT);
            metadata.removeAll(entity, property, null);
            metadata.add(entity, property, metadata.createTypedLiteral(
                Instant.now(clock).toString(), "http://www.w3.org/2001/XMLSchema#dateTime"));
            dataset.commit();
            changed = true;
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
        try {
            dataset.begin(ReadWrite.READ);
        } catch (Exception ex) {
            log.warn("Could not read the description cache: {}", ex.getMessage());
            return 0L;
        }
        try {
            var count = 0L;
            val names = dataset.listNames();
            while (names.hasNext()) {
                if (!METADATA_GRAPH.equals(names.next())) {
                    count++;
                }
            }
            return count;
        } catch (Exception ex) {
            log.warn("Could not count cached descriptions: {}", ex.getMessage());
            return 0L;
        } finally {
            dataset.end();
        }
    }
}
