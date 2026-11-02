package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.io.OutputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DocumentConverterTest {

    @TempDir
    Path tmp;

    private static DataPreviewProperties properties(long maxDocumentBytes, int maxDocumentChars, Duration timeout) {
        return new DataPreviewProperties(
            true, null, null, 10, 50, 262144, maxDocumentBytes, maxDocumentChars, timeout,
            Duration.ofSeconds(3), Duration.ofSeconds(10));
    }

    private final List<DocumentConverter> converters = new ArrayList<>();

    @AfterEach
    void shutDownConverters() {
        converters.forEach(DocumentConverter::shutdown);
    }

    private DocumentConverter converter(long maxDocumentBytes, int maxDocumentChars, Duration timeout) {
        return track(new DocumentConverter(new CsvSampler(), properties(maxDocumentBytes, maxDocumentChars, timeout)));
    }

    private DocumentConverter converter(Duration timeout, DocumentConverter.Extractor extractor) {
        return track(new DocumentConverter(new CsvSampler(), properties(20971520, 200000, timeout), extractor));
    }

    private DocumentConverter track(DocumentConverter converter) {
        converters.add(converter);
        return converter;
    }

    // Stands in for a PDFBox or POI parse, which carries on regardless of interruption.
    private static DocumentConverter.Extractor blockingUntil(CountDownLatch started, CountDownLatch release) {
        return (file, rtf) -> {
            started.countDown();
            while (true) {
                try {
                    release.await();
                    return DocumentPreview.text("released", false);
                } catch (InterruptedException ignored) {
                    // keep going, as the real parsers do
                }
            }
        };
    }

    private DocumentConverter converter() {
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
    @ValueSource(strings = {"script.py", "archive.zip", "old.xls", "photo.jpg", "README"})
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
    void longTxtIsTruncated() throws Exception {
        Path txt = Files.writeString(tmp.resolve("long.txt"), "a".repeat(1_000_000));

        DocumentPreview preview = converter(20971520, 10, Duration.ofSeconds(20)).convert(txt);

        assertThat(preview.kind(), is(DocumentPreview.Kind.TEXT));
        assertThat(preview.text(), is("a".repeat(10)));
        assertThat(preview.truncated(), is(true));
    }

    @Test
    void txtIsReadNoFurtherThanFourBytesPerCharacter() throws Exception {
        // Three characters may take up to twelve bytes. A byte that is not UTF-8 beyond that point
        // would, if read, send the whole text to the Latin-1 fallback.
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write("€€€€".getBytes(StandardCharsets.UTF_8));
        bytes.write(0xff);
        Path txt = Files.write(tmp.resolve("tail.txt"), bytes.toByteArray());

        DocumentPreview preview = converter(20971520, 3, Duration.ofSeconds(20)).convert(txt);

        assertThat(preview.text(), is("€€€"));
        assertThat(preview.truncated(), is(true));
    }

    @Test
    void txtCutMidCharacterDecodesAsUtf8() throws Exception {
        // Two characters allow eight bytes, which ends one byte into the third "€".
        Path txt = Files.writeString(tmp.resolve("euro.txt"), "€€€", StandardCharsets.UTF_8);

        DocumentPreview preview = converter(20971520, 2, Duration.ofSeconds(20)).convert(txt);

        assertThat(preview.text(), is("€€"));
        assertThat(preview.text(), not(containsString("\uFFFD")));
        assertThat(preview.truncated(), is(true));
    }

    @Test
    void txtCutMidCharacterAfterAsciiKeepsWholeCharacters() throws Exception {
        // Eight bytes end two bytes into the "€", whose start is then dropped as well.
        Path txt = Files.writeString(tmp.resolve("mixed.txt"), "abcdef€gh", StandardCharsets.UTF_8);

        DocumentPreview preview = converter(20971520, 2, Duration.ofSeconds(20)).convert(txt);

        assertThat(preview.text(), is("ab"));
        assertThat(preview.truncated(), is(true));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void utf8BoundaryDropsAnIncompleteFourByteSequence(int kept) {
        byte[] emoji = "\uD83C\uDF0D".getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[2 + kept];
        bytes[0] = 'a';
        bytes[1] = 'b';
        System.arraycopy(emoji, 0, bytes, 2, kept);

        assertThat(DocumentConverter.utf8Boundary(bytes), is(2));
    }

    @Test
    void utf8BoundaryKeepsCompleteAndNonUtf8Endings() {
        assertThat(DocumentConverter.utf8Boundary("ab\uD83C\uDF0D".getBytes(StandardCharsets.UTF_8)), is(6));
        assertThat(DocumentConverter.utf8Boundary("ab€".getBytes(StandardCharsets.UTF_8)), is(5));
        assertThat(DocumentConverter.utf8Boundary(new byte[] {'a', (byte) 0xb0}), is(2));
        assertThat(DocumentConverter.utf8Boundary(new byte[0]), is(0));
    }

    @Test
    void txtExactlyAtByteLimitIsNotTruncated() throws Exception {
        Path txt = Files.writeString(tmp.resolve("exact.txt"), "€€", StandardCharsets.UTF_8);

        DocumentPreview preview = converter(20971520, 2, Duration.ofSeconds(20)).convert(txt);

        assertThat(preview.text(), is("€€"));
        assertThat(preview.truncated(), is(false));
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
    void readFailureOfTxtPropagates() {
        Path missing = tmp.resolve("gone.txt");

        assertThrows(UncheckedIOException.class, () -> converter().convert(missing));
    }

    @Test
    void readFailureOfCsvPropagates() {
        Path missing = tmp.resolve("gone.csv");

        assertThrows(UncheckedIOException.class, () -> converter().convert(missing));
    }

    @Test
    void readFailureDuringConversionPropagates() throws Exception {
        DocumentConverter converter = converter(Duration.ofSeconds(20), (file, rtf) -> {
            throw new IOException("Host is down");
        });

        assertThrows(UncheckedIOException.class, () -> converter.convert(fixture("tiny.pdf")));
    }

    @Test
    void contentFailureDuringConversionIsNotPreviewable() throws Exception {
        DocumentConverter converter = converter(Duration.ofSeconds(20), (file, rtf) -> {
            throw new TikaException("Unexpected end of file");
        });

        assertThat(converter.convert(fixture("tiny.pdf")).kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }

    @Test
    void interruptedWaitPropagatesAndKeepsTheInterrupt() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        DocumentConverter converter = converter(Duration.ofSeconds(20), blockingUntil(started, release));
        Path rtf = fixture("dictionary.rtf");
        try {
            Thread.currentThread().interrupt();

            assertThrows(IllegalStateException.class, () -> converter.convert(rtf));
            assertThat(Thread.interrupted(), is(true));
        } finally {
            release.countDown();
        }
    }

    @Test
    void unreadableDocumentIsNotPreviewable() throws Exception {
        Path pdf = Files.writeString(tmp.resolve("broken.pdf"), "%PDF-1.4\nnot really a PDF\n");

        DocumentPreview preview = converter().convert(pdf);

        assertThat(preview.kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }

    @Test
    void rejectsWhenAllWorkersBusy() throws Exception {
        CountDownLatch started = new CountDownLatch(DocumentConverter.WORKERS);
        CountDownLatch release = new CountDownLatch(1);
        DocumentConverter converter = converter(Duration.ofSeconds(20), blockingUntil(started, release));
        Path rtf = fixture("dictionary.rtf");
        List<Future<DocumentPreview>> running = new ArrayList<>();
        try (ExecutorService callers = Executors.newFixedThreadPool(DocumentConverter.WORKERS)) {
            try {
                for (int i = 0; i < DocumentConverter.WORKERS; i++) {
                    running.add(callers.submit(() -> converter.convert(rtf)));
                }
                assertThat(started.await(5, TimeUnit.SECONDS), is(true));

                assertThrows(PreviewBusyException.class, () -> converter.convert(rtf));
            } finally {
                release.countDown();
            }
            for (Future<DocumentPreview> conversion : running) {
                assertThat(conversion.get(5, TimeUnit.SECONDS).text(), is("released"));
            }
        }
    }

    @Test
    void timedOutParseKeepsItsWorkerUntilItFinishes() throws Exception {
        CountDownLatch started = new CountDownLatch(DocumentConverter.WORKERS);
        CountDownLatch release = new CountDownLatch(1);
        DocumentConverter converter = converter(Duration.ofMillis(200), blockingUntil(started, release));
        Path rtf = fixture("dictionary.rtf");
        try {
            for (int i = 0; i < DocumentConverter.WORKERS; i++) {
                assertThat(converter.convert(rtf).kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
            }
            assertThat(started.await(5, TimeUnit.SECONDS), is(true));

            assertThrows(PreviewBusyException.class, () -> converter.convert(rtf));
        } finally {
            release.countDown();
        }
    }

    @Test
    void freesWorkerAfterConversion() throws Exception {
        DocumentConverter converter = converter();
        Path rtf = fixture("dictionary.rtf");

        for (int i = 0; i < DocumentConverter.WORKERS + 1; i++) {
            assertThat(converter.convert(rtf).kind(), is(DocumentPreview.Kind.TEXT));
        }
    }

    // Delimited text and workbooks

    @ParameterizedTest
    @ValueSource(strings = {"table.tsv", "table.tab"})
    void tabDelimitedBecomesTable(String name) throws Exception {
        Path tsv = Files.writeString(tmp.resolve(name), "site\tcount\nA1\t3\n");

        DocumentPreview preview = converter().convertData(tsv, name);

        assertThat(preview.kind(), is(DocumentPreview.Kind.TABLE));
        assertThat(preview.table().header(), contains("site", "count"));
    }

    @Test
    void pipeDelimitedBecomesTable() throws Exception {
        Path psv = Files.writeString(tmp.resolve("t.psv"), "site|count\nA1|3\n");

        assertThat(converter().convertData(psv, "t.psv").table().rows(), contains(List.of("A1", "3")));
    }

    @Test
    void semicolonCsvIsSplitOnSemicolons() throws Exception {
        Path csv = Files.writeString(tmp.resolve("eu.csv"), "site;value\nA1;1,5\n");

        assertThat(converter().convertData(csv, "eu.csv").table().rows(), contains(List.of("A1", "1,5")));
    }

    @Test
    void singleColumnCsvIsStillATable() throws Exception {
        Path csv = Files.writeString(tmp.resolve("one.csv"), "count\n1\n2\n");

        DocumentPreview preview = converter().convertData(csv, "one.csv");

        assertThat(preview.kind(), is(DocumentPreview.Kind.TABLE));
        assertThat(preview.table().rows(), contains(List.of("1"), List.of("2")));
    }

    @Test
    void tabularTxtDataBecomesTable() throws Exception {
        Path txt = Files.writeString(tmp.resolve("rain.txt"), "year\train\n1990\t85.2\n1991\t60.1\n");

        DocumentPreview preview = converter().convertData(txt, "rain.txt");

        assertThat(preview.kind(), is(DocumentPreview.Kind.TABLE));
        assertThat(preview.table().rows(), contains(List.of("1990", "85.2"), List.of("1991", "60.1")));
    }

    @Test
    void spaceAlignedDatBecomesTable() throws Exception {
        Path dat = Files.writeString(tmp.resolve("rain.dat"), "year  rain\n1990  85.2\n1991  60.1\n");

        DocumentPreview preview = converter().convertData(dat, "rain.dat");

        assertThat(preview.kind(), is(DocumentPreview.Kind.TABLE));
        assertThat(preview.table().header(), contains("year", "rain"));
    }

    @Test
    void txtWithOneAgreeingRowIsShownAsText() throws Exception {
        Path txt = Files.writeString(tmp.resolve("hello.txt"), "Hello, world.\nGoodbye, moon.\n");

        assertThat(converter().convertData(txt, "hello.txt").kind(), is(DocumentPreview.Kind.TEXT));
    }

    @Test
    void readmeTxtDataIsShownAsText() throws Exception {
        DocumentPreview preview = converter().convertData(fixture("notes.txt"), "notes.txt");

        assertThat(preview.kind(), is(DocumentPreview.Kind.TEXT));
        assertThat(preview.text(), is("Notes on the survey.\nSecond line of notes.\n"));
        assertThat(preview.truncated(), is(false));
    }

    @Test
    void longReadmeTxtDataIsTruncated() throws Exception {
        Path txt = Files.writeString(tmp.resolve("long.txt"), "word ".repeat(100) + "\n");

        DocumentPreview preview = converter(20971520, 10, Duration.ofSeconds(20)).convertData(txt, "long.txt");

        assertThat(preview.kind(), is(DocumentPreview.Kind.TEXT));
        assertThat(preview.text(), is("word word "));
        assertThat(preview.truncated(), is(true));
    }

    @Test
    void binaryTxtDataIsNotPreviewable() throws Exception {
        Path txt = Files.write(tmp.resolve("odd.txt"), new byte[] {'a', 0, 'b'});

        assertThat(converter().convertData(txt, "odd.txt").kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"guide.pdf", "notes.docx", "map.shp", "old.xls"})
    void dataOfOtherFormatsIsNotPreviewable(String name) throws Exception {
        Path file = Files.writeString(tmp.resolve(name), "content");

        assertThat(converter().convertData(file, name).kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }

    @Test
    void supportingTxtStaysTextEvenWhenDelimited() throws Exception {
        // A data dictionary is read as a document; only data files are sniffed for tables.
        Path txt = Files.writeString(tmp.resolve("dict.txt"), "name\tunits\nrain\tmm\n");

        assertThat(converter().convert(txt, "dict.txt").kind(), is(DocumentPreview.Kind.TEXT));
    }

    @Test
    void supportingTsvBecomesTable() throws Exception {
        Path tsv = Files.writeString(tmp.resolve("dict.tsv"), "name\tunits\nrain\tmm\n");

        assertThat(converter().convert(tsv, "dict.tsv").table().rows(), contains(List.of("rain", "mm")));
    }

    private Path workbook(String name, WorkbookBuilder build) throws IOException {
        Path file = tmp.resolve(name);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = Files.newOutputStream(file)) {
            build.build(workbook);
            workbook.write(out);
        }
        return file;
    }

    @FunctionalInterface
    private interface WorkbookBuilder {
        void build(XSSFWorkbook workbook);
    }

    private static void row(Sheet sheet, int index, Object... values) {
        Row row = sheet.createRow(index);
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                continue;
            }
            if (values[i] instanceof Number number) {
                row.createCell(i).setCellValue(number.doubleValue());
            } else {
                row.createCell(i).setCellValue(values[i].toString());
            }
        }
    }

    @Test
    void workbookFirstSheetBecomesTable() throws Exception {
        Path xlsx = workbook("sites.xlsx", wb -> {
            Sheet sheet = wb.createSheet("Sites");
            row(sheet, 0, "site", "count");
            row(sheet, 1, "A1", 3);
            row(sheet, 2, "B2", 5.5);
        });

        DocumentPreview preview = converter().convertData(xlsx, "sites.xlsx");

        assertThat(preview.kind(), is(DocumentPreview.Kind.TABLE));
        assertThat(preview.table().header(), contains("site", "count"));
        assertThat(preview.table().rows(), contains(List.of("A1", "3"), List.of("B2", "5.5")));
        assertThat(preview.table().moreRows(), is(false));
        assertThat(preview.table().sheet(), is("Sites"));
        assertThat(preview.table().sheets(), contains("Sites"));
    }

    @Test
    void workbookSkipsEmptySheetsAndNamesTheOthers() throws Exception {
        Path xlsx = workbook("book.xlsx", wb -> {
            wb.createSheet("Cover");
            Sheet data = wb.createSheet("Data");
            row(data, 2, "site", "count");
            row(data, 3, "A1", 3);
            row(wb.createSheet("Notes"), 0, "Collected 2019");
        });

        DocumentPreview preview = converter().convertData(xlsx, "book.xlsx");

        assertThat(preview.table().sheet(), is("Data"));
        assertThat(preview.table().sheets(), contains("Cover", "Data", "Notes"));
        assertThat(preview.table().header(), contains("site", "count"));
    }

    @Test
    void workbookKeepsGapsBetweenCellsAsEmptyColumns() throws Exception {
        Path xlsx = workbook("gaps.xlsx", wb -> {
            Sheet sheet = wb.createSheet("S");
            row(sheet, 0, "a", "b", "c");
            row(sheet, 1, "1", null, "3");
            row(sheet, 2, "4");
        });

        DocumentPreview preview = converter().convertData(xlsx, "gaps.xlsx");

        assertThat(preview.table().rows(), contains(List.of("1", "", "3"), List.of("4", "", "")));
    }

    @Test
    void workbookStopsAtMaxRowsAndFlagsMore() throws Exception {
        Path xlsx = workbook("big.xlsx", wb -> {
            Sheet sheet = wb.createSheet("S");
            row(sheet, 0, "n");
            for (int i = 1; i <= 60; i++) {
                row(sheet, i, i);
            }
        });

        DocumentPreview preview = converter().convertData(xlsx, "big.xlsx");

        assertThat(preview.table().rows().size(), is(50));
        assertThat(preview.table().moreRows(), is(true));
    }

    @Test
    void emptyWorkbookIsNotPreviewable() throws Exception {
        Path xlsx = workbook("empty.xlsx", wb -> wb.createSheet("Blank"));

        assertThat(converter().convertData(xlsx, "empty.xlsx").kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }

    @Test
    void fileThatIsNotAWorkbookIsNotPreviewable() throws Exception {
        Path xlsx = Files.writeString(tmp.resolve("fake.xlsx"), "site,count\nA1,3\n");

        assertThat(converter().convertData(xlsx, "fake.xlsx").kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }

    @Test
    void workbookOverSizeLimitIsTooLarge() throws Exception {
        Path xlsx = workbook("sites.xlsx", wb -> row(wb.createSheet("S"), 0, "site"));

        assertThat(converter(10, 200000, Duration.ofSeconds(20)).convertData(xlsx, "sites.xlsx").kind(),
            is(DocumentPreview.Kind.TOO_LARGE));
    }

    @Test
    void supportingWorkbookBecomesTable() throws Exception {
        Path xlsx = workbook("dictionary.xlsx", wb -> {
            Sheet sheet = wb.createSheet("Dictionary");
            row(sheet, 0, "name", "units");
            row(sheet, 1, "rain", "mm");
        });

        DocumentPreview preview = converter().convert(xlsx, "dictionary.xlsx");

        assertThat(preview.kind(), is(DocumentPreview.Kind.TABLE));
        assertThat(preview.table().rows(), contains(List.of("rain", "mm")));
    }

    @Test
    void workbookReadWaitsForAWorker() throws Exception {
        CountDownLatch started = new CountDownLatch(DocumentConverter.WORKERS);
        CountDownLatch release = new CountDownLatch(1);
        DocumentConverter converter = converter(Duration.ofSeconds(20), blockingUntil(started, release));
        Path rtf = fixture("dictionary.rtf");
        Path xlsx = workbook("sites.xlsx", wb -> row(wb.createSheet("S"), 0, "site"));
        try (ExecutorService callers = Executors.newFixedThreadPool(DocumentConverter.WORKERS)) {
            try {
                for (int i = 0; i < DocumentConverter.WORKERS; i++) {
                    callers.submit(() -> converter.convert(rtf));
                }
                assertThat(started.await(5, TimeUnit.SECONDS), is(true));

                assertThrows(PreviewBusyException.class, () -> converter.convertData(xlsx, "sites.xlsx"));
            } finally {
                release.countDown();
            }
        }
    }

    @Test
    void readFailureOfDataPropagates() {
        Path missing = tmp.resolve("gone.tsv");

        assertThrows(UncheckedIOException.class, () -> converter().convertData(missing, "gone.tsv"));
    }

    // Limits that hold against a crafted workbook

    @Test
    void workbookColumnsBeyondTheCapAreLeftOut() throws Exception {
        Path xlsx = workbook("wide.xlsx", wb -> {
            Sheet sheet = wb.createSheet("S");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("a");
            header.createCell(16_000).setCellValue("far");
            row(sheet, 1, "1");
        });

        DocumentPreview preview = converter().convertData(xlsx, "wide.xlsx");

        assertThat(preview.table().header(), contains("a"));
    }

    @Test
    void cellReferenceFarBeyondExcelIsIgnoredWithoutPadding() {
        XlsxSampler.Rows rows = new XlsxSampler.Rows(50);
        rows.startRow(0);
        rows.cell("A1", "a", null);
        rows.cell("ZZZZZZ1", "far", null);
        rows.endRow(0);
        rows.startRow(1);
        rows.cell("A2", "1", null);
        rows.endRow(1);

        CsvSample sample = rows.sample();

        assertThat(sample.header(), contains("a"));
        assertThat(sample.rows(), contains(List.of("1")));
    }

    @Test
    void blankRowsCountTowardsTheScanLimit() {
        XlsxSampler.Rows rows = new XlsxSampler.Rows(50);
        rows.startRow(0);
        rows.cell("A1", "a", null);
        rows.endRow(0);

        assertThrows(XlsxSampler.EnoughRows.class, () -> {
            for (int i = 1; ; i++) {
                rows.startRow(i);
                rows.endRow(i);
            }
        });

        CsvSample sample = rows.sample();
        assertThat(sample.header(), contains("a"));
        assertThat(sample.rows(), is(List.of()));
        assertThat(sample.moreRows(), is(true));
    }

    @Test
    void sharedStringsExpandingBeyondTheLimitAreTooLarge() throws Exception {
        Path xlsx = workbook("strings.xlsx", wb -> {
            Sheet sheet = wb.createSheet("S");
            row(sheet, 0, "site");
            for (int i = 1; i < 200; i++) {
                row(sheet, i, "a long distinct value number " + i);
            }
        });

        assertThrows(XlsxSampler.TooLargeException.class,
            () -> XlsxSampler.sample(xlsx, 50, 1024, XlsxSampler.MAX_STYLES_BYTES));
        assertThat(XlsxSampler.sample(xlsx, 50, XlsxSampler.MAX_STRINGS_BYTES, XlsxSampler.MAX_STYLES_BYTES).previewable(),
            is(true));
    }

    @Test
    void stylesExpandingBeyondTheLimitAreTooLargeThoughXmlBeansWrapsTheFailure() throws Exception {
        Path xlsx = workbook("styled.xlsx", wb -> row(wb.createSheet("S"), 0, "site"));

        assertThrows(XlsxSampler.TooLargeException.class,
            () -> XlsxSampler.sample(xlsx, 50, XlsxSampler.MAX_STRINGS_BYTES, 100));
    }

    @Test
    void repeatedAndOutOfOrderCellsCannotWidenARow() {
        XlsxSampler.Rows rows = new XlsxSampler.Rows(50);
        rows.startRow(0);
        rows.cell("B1", "b", null);
        for (int i = 0; i < 100_000; i++) {
            rows.cell("B1", "again", null);
            rows.cell("A1", "earlier", null);
            rows.cell(null, "unplaced", null);
        }
        rows.endRow(0);

        assertThat(rows.sample().header().size(), is(XlsxSampler.MAX_COLUMNS));
    }

    // NetCDF

    @Test
    void netcdfDataBecomesASummary() throws Exception {
        DocumentPreview preview = converter().convertData(fixture("climate4.nc"), "climate4.nc");

        assertThat(preview.kind(), is(DocumentPreview.Kind.SUMMARY));
        assertThat(preview.summary().format(), is("NetCDF-4 (HDF5)"));
        assertThat(preview.table(), is(nullValue()));
    }

    @Test
    void netcdfIsNotHeldToTheDocumentSizeLimit() throws Exception {
        // NetCDF files run to gigabytes; only the header is read.
        DocumentPreview preview = converter(10, 200000, Duration.ofSeconds(20)).convertData(fixture("rain3.nc"), "rain3.nc");

        assertThat(preview.kind(), is(DocumentPreview.Kind.SUMMARY));
    }

    @Test
    void fileNamedNetcdfThatIsNotIsNotPreviewable() throws Exception {
        Path nc = Files.writeString(tmp.resolve("fake.nc"), "not netcdf");

        assertThat(converter().convertData(nc, "fake.nc").kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }

    @Test
    void netcdfReadWaitsForAWorker() throws Exception {
        CountDownLatch started = new CountDownLatch(DocumentConverter.WORKERS);
        CountDownLatch release = new CountDownLatch(1);
        DocumentConverter converter = converter(Duration.ofSeconds(20), blockingUntil(started, release));
        Path rtf = fixture("dictionary.rtf");
        Path nc = fixture("climate4.nc");
        try (ExecutorService callers = Executors.newFixedThreadPool(DocumentConverter.WORKERS)) {
            try {
                for (int i = 0; i < DocumentConverter.WORKERS; i++) {
                    callers.submit(() -> converter.convert(rtf));
                }
                assertThat(started.await(5, TimeUnit.SECONDS), is(true));

                assertThrows(PreviewBusyException.class, () -> converter.convertData(nc, "climate4.nc"));
            } finally {
                release.countDown();
            }
        }
    }

    @Test
    void supportingNetcdfIsNotSummarised() throws Exception {
        assertThat(converter().convert(fixture("rain3.nc"), "rain3.nc").kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }
}
