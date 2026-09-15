package uk.ac.ceh.gateway.catalogue.maintenance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.maintenance.MaintenanceResponse.EmbeddingProgress;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The state and the warning flag are decided here rather than in the template, for the reason
 * already written into {@link MaintenanceResponse.GraphProgress}: a template deciding which states
 * matter renders a later addition as neither. They come out of one method together, so a new case
 * cannot be forgotten.
 */
@DisplayName("EmbeddingProgress")
class EmbeddingProgressTest {

    @Test
    @DisplayName("every record embedded reads as ready")
    void fullCoverageIsReady() {
        EmbeddingProgress progress = EmbeddingProgress.from(1991, 1991, 0, 0);

        assertThat(progress.state()).isEqualTo("Ready");
        assertThat(progress.warning()).isFalse();
        assertThat(progress.percentage()).isEqualTo(100);
    }

    /**
     * A backfill runs at one batch per flush, so partial coverage is the normal state for a while
     * after a reindex and must not read as a fault.
     */
    @Test
    @DisplayName("partial coverage reads as filling, not a fault")
    void partialCoverageIsFilling() {
        EmbeddingProgress progress = EmbeddingProgress.from(1847, 1991, 144, 0);

        assertThat(progress.state()).isEqualTo("Filling");
        assertThat(progress.warning()).isFalse();
        assertThat(progress.percentage()).isEqualTo(92);
    }

    /**
     * Abandoned documents are the one genuinely actionable state: they have exhausted
     * max-attempts, so they will never gain a vector without being saved again or the index
     * rebuilt. Everything else resolves itself.
     */
    @Test
    @DisplayName("abandoned records are flagged, and counted in the state")
    void abandonedRecordsWarn() {
        EmbeddingProgress progress = EmbeddingProgress.from(1988, 1991, 0, 3);

        assertThat(progress.state()).isEqualTo("3 records abandoned");
        assertThat(progress.warning()).isTrue();
    }

    @Test
    @DisplayName("a single abandoned record is not pluralised")
    void oneAbandonedRecordReadsAsSingular() {
        assertThat(EmbeddingProgress.from(1990, 1991, 0, 1).state())
            .isEqualTo("1 record abandoned");
    }

    /**
     * Nothing embedded and nothing queued means semantic search returns nothing at all, which is
     * what the panel exists to make visible.
     */
    @Test
    @DisplayName("no embeddings at all is flagged")
    void noEmbeddingsWarns() {
        EmbeddingProgress progress = EmbeddingProgress.from(0, 1991, 0, 0);

        assertThat(progress.state()).isEqualTo("No embeddings");
        assertThat(progress.warning()).isTrue();
        assertThat(progress.percentage()).isZero();
    }

    /**
     * An empty index is already reported by the Indexing section above, and would otherwise show
     * here as the more alarming "No embeddings" as well.
     */
    @Test
    @DisplayName("an empty index is left to the indexing section to report")
    void emptyIndexIsNotFlaggedHere() {
        EmbeddingProgress progress = EmbeddingProgress.from(0, 0, 0, 0);

        assertThat(progress.state()).isEqualTo("No records indexed");
        assertThat(progress.warning()).isFalse();
        assertThat(progress.percentage()).isZero();
    }

    @Test
    @DisplayName("abandoned records outrank a filling index, being the actionable state")
    void abandonedOutranksFilling() {
        EmbeddingProgress progress = EmbeddingProgress.from(1800, 1991, 100, 2);

        assertThat(progress.state()).isEqualTo("2 records abandoned");
        assertThat(progress.warning()).isTrue();
    }
}
