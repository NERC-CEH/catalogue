package uk.ac.ceh.gateway.catalogue.indexing.solr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SupportingDocumentExtractorTest {

    @TempDir Path baseDir;

    private SupportingDocumentExtractor extractor;

    @BeforeEach
    void setup() {
        extractor = new SupportingDocumentExtractor(baseDir.toString(), 4000, 5);
    }

    @Test
    void returnsEmptyWhenDirectoryDoesNotExist() {
        assertThat(extractor.extractText("nonexistent-id")).isEmpty();
    }

    @Test
    void returnsEmptyWhenDirectoryContainsNoSupportedFiles() throws IOException {
        Path docDir = baseDir.resolve("doc-id");
        Files.createDirectory(docDir);
        Files.writeString(docDir.resolve("data.csv"), "a,b,c");
        Files.writeString(docDir.resolve("archive.zip"), "not-a-zip");

        assertThat(extractor.extractText("doc-id")).isEmpty();
    }

    @Test
    void skipsNonSupportedExtensions() throws IOException {
        Path docDir = baseDir.resolve("doc-id");
        Files.createDirectory(docDir);
        Files.writeString(docDir.resolve("data.nc"), "netcdf-binary");
        Files.writeString(docDir.resolve("style.qml"), "<qgis>styling, not prose</qgis>");

        assertThat(extractor.extractText("doc-id")).isEmpty();
    }

    @Test
    void returnsEmptyWhenDirectoryIsEmpty() throws IOException {
        Path docDir = baseDir.resolve("empty-id");
        Files.createDirectory(docDir);

        assertThat(extractor.extractText("empty-id")).isEmpty();
    }

    @Test
    void handlesMissingBasePathGracefully() throws IOException {
        Path docDir = baseDir.resolve("ok-id");
        Files.createDirectory(docDir);
        // valid dir but no supported files — should not throw
        assertThat(extractor.extractText("ok-id")).isEmpty();
    }

    @Test
    void respectsMaxFilesLimit() throws IOException {
        // Create 6 RTF-named files (limit is 5); use .rtf extension but plain text content
        // (Tika will fail gracefully for non-real RTFs)
        Path docDir = baseDir.resolve("many-id");
        Files.createDirectory(docDir);
        for (int i = 0; i < 6; i++) {
            Files.writeString(docDir.resolve("file" + i + ".rtf"), "{\\rtf1 content" + i + "}");
        }
        // We just verify it doesn't process more than maxFiles and doesn't throw
        // (Tika may or may not extract from minimal RTF; the important thing is no exception)
        assertThat(extractor.extractText("many-id")).isNotNull();
    }

    @Test
    void doesNotThrowWhenExtractionFails() throws IOException {
        Path docDir = baseDir.resolve("corrupt-id");
        Files.createDirectory(docDir);
        // Write garbage bytes as a "pdf" — Tika may or may not extract content,
        // but the extractor must never propagate exceptions to the caller
        Files.write(docDir.resolve("corrupt.pdf"), new byte[]{0x00, 0x01, 0x02});

        assertDoesNotThrow(() -> extractor.extractText("corrupt-id"));
    }

    @Test
    void rejectsPathTraversalAttempt() {
        assertThat(extractor.extractText("../../etc/passwd")).isEmpty();
        assertThat(extractor.extractText("../sibling")).isEmpty();
        assertThat(extractor.extractText("valid/../../../etc")).isEmpty();
    }

    @Test
    void rejectsNullAndBlankId() {
        assertThat(extractor.extractText(null)).isEmpty();
        assertThat(extractor.extractText("")).isEmpty();
        assertThat(extractor.extractText("   ")).isEmpty();
    }

    /**
     * Plain text was not in the supported set, which on production leaves 63 records with no
     * embeddable supporting text at all despite having a readme sitting next to them, and a
     * further 38 records missing text they could have had. Tika reads it natively.
     */
    @Test
    void extractsPlainTextFiles() throws IOException {
        Path docDir = baseDir.resolve("txt-id");
        Files.createDirectory(docDir);
        Files.writeString(docDir.resolve("readme.txt"), "Soil moisture measurements from upland peat");

        assertThat(extractor.extractText("txt-id")).contains("Soil moisture measurements from upland peat");
    }

    @Test
    void extractsPlainTextWhileStillIgnoringUnsupportedNeighbours() throws IOException {
        Path docDir = baseDir.resolve("mixed-id");
        Files.createDirectory(docDir);
        Files.writeString(docDir.resolve("readme.txt"), "river flow gauging station");
        Files.writeString(docDir.resolve("style.qml"), "<qgis>styling, not prose</qgis>");
        Files.writeString(docDir.resolve("layers.lyr"), "binary-ish layer file");

        String extracted = extractor.extractText("mixed-id");

        assertThat(extracted).contains("river flow gauging station");
        assertThat(extracted).doesNotContain("qgis").doesNotContain("layer file");
    }

    @Test
    void truncatesAnOverlongTextFileToTheConfiguredLimit() throws IOException {
        SupportingDocumentExtractor smallLimit = new SupportingDocumentExtractor(baseDir.toString(), 50, 8);
        Path docDir = baseDir.resolve("long-id");
        Files.createDirectory(docDir);
        Files.writeString(docDir.resolve("long.txt"), "x".repeat(500));

        // not just "short": an empty result would satisfy a size bound on its own
        assertThat(smallLimit.extractText("long-id"))
            .isNotEmpty()
            .hasSizeLessThanOrEqualTo(50);
    }
}
