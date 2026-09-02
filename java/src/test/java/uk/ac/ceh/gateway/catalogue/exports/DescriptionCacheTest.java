package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Remembering what an authority said (dri-one #350)")
class DescriptionCacheTest {

    private static final String ORCID = "https://orcid.org/0000-0002-0394-2998";

    private Dataset dataset;
    private Instant now;

    @BeforeEach
    void setUp() {
        dataset = TDB2Factory.createDataset();
        now = Instant.parse("2026-09-02T12:00:00Z");
    }

    @AfterEach
    void tearDown() {
        dataset.close();
    }

    /** A cache whose clock is wherever {@link #now} currently points. */
    private DescriptionCache cache() {
        return new DescriptionCache(dataset, Clock.fixed(now, ZoneOffset.UTC));
    }

    private static org.apache.jena.rdf.model.Model description(String label) {
        val model = ModelFactory.createDefaultModel();
        model.add(model.getResource(ORCID), RDFS.label, label);
        return model;
    }

    @Test
    @DisplayName("a description that was just stored is returned")
    void freshDescriptionIsReturned() {
        cache().put(ORCID, description("Claire Wood"));

        val held = cache().get(ORCID, Duration.ofDays(14));

        assertTrue(held.isPresent());
        assertTrue(held.get().contains(createResource(ORCID), RDFS.label, "Claire Wood"));
    }

    @Test
    @DisplayName("nothing is returned for an entity never stored")
    void unknownEntityIsAbsent() {
        assertThat(cache().get(ORCID, Duration.ofDays(14)).isPresent(), is(false));
    }

    @Test
    @DisplayName("a description older than the caller will accept is treated as absent")
    void staleDescriptionIsAbsent() {
        cache().put(ORCID, description("Claire Wood"));

        now = now.plus(Duration.ofDays(15));

        assertThat(
            "the caller asked for nothing older than a fortnight",
            cache().get(ORCID, Duration.ofDays(14)).isPresent(), is(false)
        );
    }

    @Test
    @DisplayName("but the same description is still there for a caller who will accept it")
    void staleDescriptionIsStillRetrievable() {
        cache().put(ORCID, description("Claire Wood"));
        now = now.plus(Duration.ofDays(15));

        val held = cache().get(ORCID, Duration.ofDays(365));

        assertTrue(
            held.isPresent(),
            "nothing is evicted: a stale name beats no name when an authority is unreachable"
        );
        assertTrue(held.get().contains(createResource(ORCID), RDFS.label, "Claire Wood"));
    }

    @Test
    @DisplayName("storing again replaces the previous copy rather than adding to it")
    void putReplaces() {
        cache().put(ORCID, description("C. Wood"));
        now = now.plus(Duration.ofDays(20));
        cache().put(ORCID, description("Claire Wood"));

        val held = cache().get(ORCID, Duration.ofDays(1)).orElseThrow();

        assertThat("a replaced description should not accumulate", held.size(), is(1L));
        assertTrue(held.contains(createResource(ORCID), RDFS.label, "Claire Wood"));
    }

    @Test
    @DisplayName("an empty description is remembered, so a silent authority is not asked again every run")
    void emptyDescriptionIsRemembered() {
        cache().put(ORCID, ModelFactory.createDefaultModel());

        val held = cache().get(ORCID, Duration.ofDays(14));

        assertTrue(
            held.isPresent(),
            "the authority was reached and had nothing to say; that is worth remembering"
        );
        assertThat(held.get().size(), is(0L));
    }

    @Test
    @DisplayName("the returned model outlives the transaction it was read in")
    void returnedModelIsDetached() {
        cache().put(ORCID, description("Claire Wood"));

        val held = cache().get(ORCID, Duration.ofDays(14)).orElseThrow();
        // A TDB2-backed model is only valid inside its transaction, so this
        // would throw if the cache handed one back rather than a copy.
        assertThat(held.listStatements().toList().size(), is(1));
    }

    @Nested
    @DisplayName("Surviving pod recreation via a snapshot on the share")
    class Snapshot {

        @TempDir
        Path directory;

        /** A cache backed by {@link #dataset}, snapshotting to {@code file}. */
        private DescriptionCache cache(Path file) {
            return new DescriptionCache(dataset, Clock.fixed(now, ZoneOffset.UTC), file.toString());
        }

        /** A cache with its own empty store, standing in for a recreated pod. */
        private DescriptionCache recreatedPod(Path file) {
            return new DescriptionCache(
                TDB2Factory.createDataset(), Clock.fixed(now, ZoneOffset.UTC), file.toString());
        }

        @Test
        @DisplayName("a description written before the pod died is there after it")
        void descriptionsSurvive() {
            val file = directory.resolve("cache.nq");
            val live = cache(file);
            live.put(ORCID, description("Claire Wood"));
            live.save();

            val held = recreatedPod(file).get(ORCID, Duration.ofDays(14));

            assertTrue(held.isPresent(), "without this the pod refetches 2,686 entities");
            assertTrue(held.get().contains(createResource(ORCID), RDFS.label, "Claire Wood"));
        }

        @Test
        @DisplayName("the retrieval time survives too, so a recovered copy still ages")
        void timestampsSurvive() {
            val file = directory.resolve("cache.nq");
            val live = cache(file);
            live.put(ORCID, description("Claire Wood"));
            live.save();

            // A recovered description whose timestamp had been lost would either
            // look brand new for ever or be refetched immediately. Neither is a
            // cache.
            val recovered = new DescriptionCache(
                TDB2Factory.createDataset(),
                Clock.fixed(now.plus(Duration.ofDays(15)), ZoneOffset.UTC),
                file.toString());

            assertThat(recovered.get(ORCID, Duration.ofDays(14)).isPresent(), is(false));
            assertTrue(recovered.get(ORCID, Duration.ofDays(365)).isPresent());
        }

        @Test
        @DisplayName("a remembered empty answer survives, though it has no triples to write")
        void emptyDescriptionsSurvive() {
            // The subtle one. An entity the authority had nothing to say about is
            // an empty named graph, and an empty graph contributes no quads at
            // all to the file. What carries it across is its timestamp in the
            // metadata graph, which is also what get() keys on -- so the negative
            // result survives a round trip that cannot represent it directly.
            val file = directory.resolve("cache.nq");
            val live = cache(file);
            live.put(ORCID, ModelFactory.createDefaultModel());
            live.save();

            val held = recreatedPod(file).get(ORCID, Duration.ofDays(14));

            assertTrue(held.isPresent(), "otherwise a silent authority is asked again every run");
            assertThat(held.get().size(), is(0L));
        }

        @Test
        @DisplayName("no snapshot configured writes no file")
        void snapshotIsOptional() {
            val cache = new DescriptionCache(dataset, Clock.fixed(now, ZoneOffset.UTC), "");
            cache.put(ORCID, description("Claire Wood"));
            cache.save();

            assertThat(directory.toFile().list().length, is(0));
        }

        @Test
        @DisplayName("nothing is written when nothing has changed")
        void saveIsSkippedWhenClean() throws Exception {
            val file = directory.resolve("cache.nq");
            val cache = cache(file);
            cache.put(ORCID, description("Claire Wood"));
            cache.save();
            val firstWrite = java.nio.file.Files.getLastModifiedTime(file);

            cache.save();

            assertThat(
                "a daily export that fetched nothing should not rewrite the file",
                java.nio.file.Files.getLastModifiedTime(file), is(firstWrite)
            );
        }

        @Test
        @DisplayName("an unreadable snapshot starts the cache empty rather than failing")
        void corruptSnapshotIsSurvivable() throws Exception {
            val file = directory.resolve("cache.nq");
            java.nio.file.Files.writeString(file, "this is not N-Quads <<<\n");

            val cache = recreatedPod(file);

            assertThat("a half-written file is the cost of a crash mid-save, not an outage",
                cache.get(ORCID, Duration.ofDays(14)).isPresent(), is(false));
            assertThat(cache.size(), is(0L));
        }

        @Test
        @DisplayName("a missing snapshot is not an error, since the first run has none")
        void missingSnapshotIsSurvivable() {
            val cache = recreatedPod(directory.resolve("never-written.nq"));

            assertThat(cache.size(), is(0L));
        }

        @Test
        @DisplayName("no partial file is left behind after a successful save")
        void noPartialFileRemains() {
            val file = directory.resolve("cache.nq");
            val live = cache(file);
            live.put(ORCID, description("Claire Wood"));
            live.save();

            assertThat(
                java.util.List.of(directory.toFile().list()),
                is(java.util.List.of("cache.nq"))
            );
        }
    }

    @Test
    @DisplayName("the retrieval times are kept out of the descriptions they describe")
    void metadataIsNotMixedIntoDescriptions() {
        cache().put(ORCID, description("Claire Wood"));

        val held = cache().get(ORCID, Duration.ofDays(14)).orElseThrow();

        assertThat(
            "a cached description should hold only what the authority said",
            held.size(), is(1L)
        );
        assertThat("and the timestamps live in a graph of their own", cache().size(), is(1L));
    }
}
