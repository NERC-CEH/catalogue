package uk.ac.ceh.gateway.catalogue.metrics;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Pins the checksum of every applied metrics migration, so editing one (even a comment or whitespace)
 * fails the build instead of failing Flyway's validation in a deployed environment, where it switches
 * metrics off.
 *
 * <p>Needs no database: the checksum is computed the way Flyway's {@code ChecksumCalculator} does it
 * (CRC32 over each line's UTF-8 bytes, line breaks excluded, leading BOM stripped).
 * {@code JDBCMetricsServiceTest#flywayRecordedThePinnedV1Checksum} checks that this matches what Flyway
 * actually records in {@code flyway_schema_history}.</p>
 *
 * <p><strong>If this fails:</strong> do not update the constant. Revert the edit to the applied
 * migration and put the change in a new {@code V<n>__*.sql} file. Only update a constant if the
 * migration has provably never been applied to any environment.</p>
 */
class FlywayMigrationChecksumTest {
    static final int V1_CHECKSUM = -1267176912;

    @Test
    void v1HasNotBeenEdited() {
        assertThat(checksum("db/metrics/V1__create_metrics_tables.sql"), is(V1_CHECKSUM));
    }

    @SneakyThrows
    static int checksum(String classpathResource) {
        val stream = FlywayMigrationChecksumTest.class.getClassLoader().getResourceAsStream(classpathResource);
        assertThat(classpathResource + " is not on the classpath", stream, is(notNullValue()));
        val crc32 = new CRC32();
        try (val reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line != null && line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
            while (line != null) {
                crc32.update(line.getBytes(StandardCharsets.UTF_8));
                line = reader.readLine();
            }
        }
        return (int) crc32.getValue();
    }
}
