package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DocumentConverterTest {

    @TempDir
    Path tmp;

    private static DataPreviewProperties properties(long maxDocumentBytes, int maxDocumentChars, Duration timeout) {
        return new DataPreviewProperties(
            true, null, null, 10, 50, 262144, maxDocumentBytes, maxDocumentChars, timeout);
    }

    private static DocumentConverter converter(long maxDocumentBytes, int maxDocumentChars, Duration timeout) {
        return new DocumentConverter(new CsvSampler(), properties(maxDocumentBytes, maxDocumentChars, timeout));
    }

    private static DocumentConverter converter() {
        return converter(20971520, 200000, Duration.ofSeconds(20));
    }

    private static Path fixture(String name) throws URISyntaxException {
        return Path.of(DocumentConverterTest.class.getResource("/datapreviewer/" + name).toURI());
    }

    private static List<String> lines(DocumentPreview preview) {
        return Arrays.asList(preview.text().split("\n"));
    }

    @Test
    void rtfTableKeepsEachRowOnOneLine() throws Exception {
        DocumentPreview preview = converter().convert(fixture("dictionary.rtf"));

        assertThat(preview.kind(), is(DocumentPreview.Kind.TEXT));
        assertThat(preview.text(), containsString("Data dictionary for deposition.csv"));
        assertThat(lines(preview), hasItem("Name\tUnits\tDescription"));
        assertThat(lines(preview), hasItem("easting\tmetres\tEasting for centre of 5x5 km square"));
        assertThat(lines(preview), hasItem("nox_f\tkeq ha⁻¹ year⁻¹\tOxidised nitrogen deposition to forest…"));
        assertThat(preview.text().lines().anyMatch(l -> l.matches("nox_f\\s+keq ha⁻¹ year⁻¹\\s+Oxidised.*")), is(true));
        assertThat(preview.truncated(), is(false));
    }

    @Test
    void rtfEscapedBackslashBeforeCellIsLiteralText() throws Exception {
        Path rtf = tmp.resolve("escaped.rtf");
        Files.writeString(rtf, "{\\rtf1\\ansi\\deff0{\\fonttbl{\\f0 Arial;}}\\f0 C:\\\\cell\\\\row\\par}", StandardCharsets.US_ASCII);

        DocumentPreview preview = converter().convert(rtf);

        assertThat(preview.kind(), is(DocumentPreview.Kind.TEXT));
        assertThat(preview.text(), containsString("C:\\cell\\row"));
    }

    @Test
    void txtIsReturnedAsText() throws Exception {
        DocumentPreview preview = converter().convert(fixture("notes.txt"));

        assertThat(preview.kind(), is(DocumentPreview.Kind.TEXT));
        assertThat(preview.text(), is("Notes on the survey.\nSecond line of notes.\n"));
        assertThat(preview.table(), is(nullValue()));
        assertThat(preview.truncated(), is(false));
    }

    @Test
    void txtFallsBackToLatin1() throws Exception {
        Path txt = tmp.resolve("latin1.txt");
        Files.write(txt, "Temperature in °C".getBytes(StandardCharsets.ISO_8859_1));

        DocumentPreview preview = converter().convert(txt);

        assertThat(preview.text(), is("Temperature in °C"));
    }

    @Test
    void csvBecomesTable() throws Exception {
        DocumentPreview preview = converter().convert(fixture("table.csv"));

        assertThat(preview.kind(), is(DocumentPreview.Kind.TABLE));
        assertThat(preview.table().header(), contains("site", "date", "count"));
        assertThat(preview.table().rows(), contains(List.of("A1", "2020-01-01", "3"), List.of("B2", "2020-01-02", "5")));
        assertThat(preview.text(), is(nullValue()));
    }

    @Test
    void uppercaseExtensionIsRecognised() throws Exception {
        Path csv = tmp.resolve("TABLE.CSV");
        Files.copy(fixture("table.csv"), csv);

        assertThat(converter().convert(csv).kind(), is(DocumentPreview.Kind.TABLE));
    }

    @Test
    void pdfTextIsExtracted() throws Exception {
        DocumentPreview preview = converter().convert(fixture("tiny.pdf"));

        assertThat(preview.kind(), is(DocumentPreview.Kind.TEXT));
        assertThat(preview.text(), containsString("Tiny supporting document"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"script.py", "archive.zip", "sheet.xlsx", "photo.jpg", "README"})
    void unknownExtensionIsNotPreviewable(String name) throws IOException {
        Path file = Files.writeString(tmp.resolve(name), "content");

        DocumentPreview preview = converter().convert(file);

        assertThat(preview.kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
        assertThat(preview.text(), is(nullValue()));
        assertThat(preview.table(), is(nullValue()));
    }

    @Test
    void overSizeLimitIsTooLarge() throws Exception {
        DocumentPreview preview = converter(10, 200000, Duration.ofSeconds(20)).convert(fixture("notes.txt"));

        assertThat(preview.kind(), is(DocumentPreview.Kind.TOO_LARGE));
        assertThat(preview.text(), is(nullValue()));
    }

    @Test
    void longTextIsTruncatedAtMaxChars() throws Exception {
        DocumentPreview preview = converter(20971520, 10, Duration.ofSeconds(20)).convert(fixture("notes.txt"));

        assertThat(preview.kind(), is(DocumentPreview.Kind.TEXT));
        assertThat(preview.text(), is("Notes on t"));
        assertThat(preview.truncated(), is(true));
    }

    @Test
    void longConvertedTextIsTruncatedAtMaxChars() throws Exception {
        DocumentPreview preview = converter(20971520, 15, Duration.ofSeconds(20)).convert(fixture("dictionary.rtf"));

        assertThat(preview.kind(), is(DocumentPreview.Kind.TEXT));
        assertThat(preview.text().length(), is(15));
        assertThat(preview.truncated(), is(true));
    }

    @Test
    void conversionOverTimeoutIsNotPreviewable() throws Exception {
        DocumentPreview preview = converter(20971520, 200000, Duration.ZERO).convert(fixture("dictionary.rtf"));

        assertThat(preview.kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }

    @Test
    void unreadableDocumentIsNotPreviewable() throws Exception {
        Path pdf = Files.writeString(tmp.resolve("broken.pdf"), "%PDF-1.4\nnot really a PDF\n");

        DocumentPreview preview = converter().convert(pdf);

        assertThat(preview.kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }
}
