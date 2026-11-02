package uk.ac.ceh.gateway.catalogue.datapreviewer;

import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.WriteLimitReachedException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Turns a supporting document or a data file into something the preview can show: text for RTF,
 * Word, PDF and plain text, a table for delimited text and Excel workbooks, a header summary for
 * NetCDF, or a reason it cannot be shown.
 *
 * <p>Tika conversions, workbook reads and NetCDF header reads run on a shared pool of
 * {@value #WORKERS} workers. PDFBox and POI ignore interruption (netcdf-java is told to stop through
 * its cancel hook instead), so a conversion that times out keeps its worker until the parse
 * finishes; the pool
 * size is therefore the bound on CPU and memory spent converting. When every worker is busy,
 * {@link #convert} throws {@link PreviewBusyException} rather than queueing.
 *
 * <p>A result describes the document, so it may be cached: an unsupported format, a file over the
 * size limit, content the parser rejects and a conversion that runs out of time all come back as
 * results. A fault that may clear by itself, reading the file or being interrupted, is thrown
 * instead, so it is never cached.
 */
@Slf4j
@Component
public class DocumentConverter {

    static final int WORKERS = 2;

    private static final Set<String> TIKA_EXTENSIONS = Set.of("rtf", "doc", "docx", "pdf");
    // Delimited text that is a table by its name, and the delimiter to assume when none is detected.
    private static final Map<String, CsvSampler.Delimiter> TABLE_EXTENSIONS = Map.of(
        "csv", CsvSampler.Delimiter.COMMA,
        "tsv", CsvSampler.Delimiter.TAB,
        "tab", CsvSampler.Delimiter.TAB,
        "psv", CsvSampler.Delimiter.PIPE);
    // Text that may hold a table: shown as one when a delimiter is detected, otherwise as text.
    private static final Set<String> MAYBE_TABLE_EXTENSIONS = Set.of("txt", "dat");
    private static final String WORKBOOK_EXTENSION = "xlsx";
    // Rows below the header that plain text must have to be shown as a table.
    private static final int MIN_SNIFFED_ROWS = 2;

    /** NetCDF data files, summarised from their header rather than shown as rows. */
    public static final Set<String> NETCDF_EXTENSIONS = Set.of("nc", "nc4", "netcdf");

    /** The extensions a data file may have for the preview to show it. */
    public static final Set<String> DATA_EXTENSIONS =
        Set.of("csv", "tsv", "tab", "psv", "txt", "dat", "xlsx", "nc", "nc4", "netcdf");
    private static final Parser PARSER = new AutoDetectParser();

    // Tika's RTF parser drops table structure: every \cell ends a paragraph, so a dictionary table
    // comes out one cell per line and the columns can no longer be told apart. Rewriting \cell to
    // \tab and \row to \par before parsing puts each row on one line with tab-separated cells.
    // An escaped backslash (\\) is matched first so that literal text such as "\\cell" is left alone.
    private static final Pattern RTF_TABLE_WORDS = Pattern.compile("\\\\\\\\|\\\\(cell|row)(?![a-zA-Z])(?:-?\\d+)?( ?)");
    private static final Pattern TRAILING_TABS = Pattern.compile("\\t+$", Pattern.MULTILINE);

    /** Extracts the text of a document Tika can read; replaceable in tests. */
    @FunctionalInterface
    interface Extractor {
        DocumentPreview extract(Path file, boolean rtf) throws Exception;
    }

    private final CsvSampler csvSampler;
    private final DataPreviewProperties properties;
    private final Extractor extractor;
    // Permits are taken before submitting and given back when the parse itself ends, so a parse that
    // outlives its timeout still counts against the bound. The pool's queue can therefore hold at
    // most the few tasks between a permit being taken and a worker picking them up.
    private final Semaphore permits = new Semaphore(WORKERS);
    private final ExecutorService executor = Executors.newFixedThreadPool(
        WORKERS, Thread.ofPlatform().name("document-converter-", 1).daemon().factory());

    @Autowired
    public DocumentConverter(CsvSampler csvSampler, DataPreviewProperties properties) {
        this.csvSampler = csvSampler;
        this.properties = properties;
        this.extractor = this::extract;
    }

    DocumentConverter(CsvSampler csvSampler, DataPreviewProperties properties, Extractor extractor) {
        this.csvSampler = csvSampler;
        this.properties = properties;
        this.extractor = extractor;
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    /**
     * @throws PreviewBusyException when every conversion worker is busy; try again later
     */
    public DocumentPreview convert(Path file) {
        return convert(file, file.getFileName().toString());
    }

    /**
     * Converts {@code file}, choosing how by the extension of {@code listedName}: the file may be
     * the real path behind a symbolic link whose own name differs from the one Hubbub lists.
     *
     * @throws PreviewBusyException when every conversion worker is busy; try again later
     */
    public DocumentPreview convert(Path file, String listedName) {
        String extension = extension(listedName);
        try {
            if (TABLE_EXTENSIONS.containsKey(extension)) {
                return table(file, TABLE_EXTENSIONS.get(extension));
            }
            if (extension.equals(WORKBOOK_EXTENSION)) {
                return workbook(file);
            }
            if (!extension.equals("txt") && !TIKA_EXTENSIONS.contains(extension)) {
                return DocumentPreview.notPreviewable();
            }
            if (Files.size(file) > properties.maxDocumentBytes()) {
                return DocumentPreview.tooLarge();
            }
            if (extension.equals("txt")) {
                return readText(file);
            }
            return extractWithTimeout(file, extension.equals("rtf"));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read supporting document " + file, e);
        }
    }

    /**
     * Converts a data file, choosing how by the extension of {@code listedName} as for
     * {@link #convert(Path, String)}. Plain text ({@code .txt}, {@code .dat}) is shown as a table
     * when a delimiter is detected, and otherwise as the text it starts with, as a readme would be.
     *
     * @throws PreviewBusyException when every conversion worker is busy; try again later
     */
    public DocumentPreview convertData(Path file, String listedName) {
        String extension = extension(listedName);
        try {
            if (TABLE_EXTENSIONS.containsKey(extension)) {
                return table(file, TABLE_EXTENSIONS.get(extension));
            }
            if (extension.equals(WORKBOOK_EXTENSION)) {
                return workbook(file);
            }
            if (MAYBE_TABLE_EXTENSIONS.contains(extension)) {
                return tableOrText(file);
            }
            if (NETCDF_EXTENSIONS.contains(extension)) {
                return netcdf(file);
            }
            return DocumentPreview.notPreviewable();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read data file " + file, e);
        }
    }

    private DocumentPreview table(Path file, CsvSampler.Delimiter fallback) throws IOException {
        Optional<CsvSampler.Text> text;
        try (InputStream in = Files.newInputStream(file)) {
            text = csvSampler.read(in, properties.maxTableBytes());
        }
        return text
            .map(t -> csvSampler.table(t, CsvSampler.detect(t.text()).orElse(fallback), properties.previewRows()))
            .filter(CsvSample::previewable)
            .map(DocumentPreview::table)
            .orElseGet(DocumentPreview::notPreviewable);
    }

    private DocumentPreview tableOrText(Path file) throws IOException {
        Optional<CsvSampler.Text> text;
        try (InputStream in = Files.newInputStream(file)) {
            text = csvSampler.read(in, properties.maxTableBytes());
        }
        if (text.isEmpty()) {
            return DocumentPreview.notPreviewable();
        }
        CsvSampler.Text read = text.get();
        Optional<CsvSampler.Delimiter> delimiter = CsvSampler.detect(read.text());
        if (delimiter.isPresent()) {
            CsvSample sample = csvSampler.table(read, delimiter.get(), properties.previewRows());
            // A header and one row agreeing could as well be two lines of prose.
            if (sample.previewable() && sample.rows().size() >= MIN_SNIFFED_ROWS) {
                return DocumentPreview.table(sample);
            }
        }
        return truncate(read.text(), read.capped());
    }

    /**
     * No size limit applies: only the header and the ends of the coordinates are read, and NetCDF
     * files run to many gigabytes. The parse stops when the conversion times out.
     */
    private DocumentPreview netcdf(Path file) {
        return bounded(file, () -> NetcdfSummariser.summarise(file, Thread.currentThread()::isInterrupted)
            .map(DocumentPreview::summary)
            .orElseGet(DocumentPreview::notPreviewable));
    }

    private DocumentPreview workbook(Path file) throws IOException {
        if (Files.size(file) > properties.maxDocumentBytes()) {
            return DocumentPreview.tooLarge();
        }
        return bounded(file, () -> {
            CsvSample sample;
            try {
                sample = XlsxSampler.sample(file, properties.previewRows());
            } catch (XlsxSampler.TooLargeException e) {
                log.info("Not previewing workbook {}: {}", file, e.getMessage());
                return DocumentPreview.tooLarge();
            }
            return sample.previewable() ? DocumentPreview.table(sample) : DocumentPreview.notPreviewable();
        });
    }

    /**
     * Reads no more of a text file than {@code maxDocumentChars} characters can take, at four
     * bytes each in UTF-8, so a large file is not held whole on the request thread.
     */
    private DocumentPreview readText(Path file) throws IOException {
        int limit = (int) Math.min(Integer.MAX_VALUE - 8, (long) properties.maxDocumentChars() * 4);
        byte[] bytes;
        boolean cut;
        try (InputStream in = Files.newInputStream(file)) {
            bytes = in.readNBytes(limit);
            cut = in.read() != -1;
        }
        int length = cut ? utf8Boundary(bytes) : bytes.length;
        return truncate(CsvSampler.decode(Arrays.copyOf(bytes, length)), cut);
    }

    /**
     * The length of {@code bytes} without a UTF-8 sequence left incomplete at the end. Bytes that
     * are not UTF-8 at all are left alone, for the Latin-1 fallback to decode.
     */
    static int utf8Boundary(byte[] bytes) {
        int end = bytes.length;
        // Step back over at most three continuation bytes (10xxxxxx) to the byte that leads them.
        int lead = end - 1;
        while (lead >= 0 && lead > end - 4 && (bytes[lead] & 0xc0) == 0x80) {
            lead--;
        }
        if (lead < 0) {
            return end;
        }
        int b = bytes[lead] & 0xff;
        int needed = b >= 0xf0 ? 4 : b >= 0xe0 ? 3 : b >= 0xc0 ? 2 : 1;
        return end - lead < needed ? lead : end;
    }

    private DocumentPreview truncate(String text, boolean cut) {
        int max = properties.maxDocumentChars();
        if (text.length() <= max) {
            return DocumentPreview.text(text, cut);
        }
        // Never end on half of a surrogate pair.
        int end = max > 0 && Character.isHighSurrogate(text.charAt(max - 1)) ? max - 1 : max;
        return DocumentPreview.text(text.substring(0, end), true);
    }

    private DocumentPreview extractWithTimeout(Path file, boolean rtf) {
        return bounded(file, () -> extractor.extract(file, rtf));
    }

    /** Runs {@code task} on the worker pool, within the conversion timeout. */
    private DocumentPreview bounded(Path file, Callable<DocumentPreview> task) {
        if (!permits.tryAcquire()) {
            throw new PreviewBusyException();
        }
        // Whoever claims the task first gives the permit back: the worker when it starts, or this
        // thread when the timeout cancels a task that never started.
        AtomicBoolean claimed = new AtomicBoolean();
        Future<DocumentPreview> future;
        try {
            future = executor.submit(() -> {
                if (!claimed.compareAndSet(false, true)) {
                    return null;
                }
                try {
                    return task.call();
                } finally {
                    permits.release();
                }
            });
        } catch (RuntimeException e) {
            permits.release();
            throw e;
        }
        try {
            return future.get(properties.conversionTimeout().toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            cancel(future, claimed);
            log.warn("Converting {} took longer than {}", file, properties.conversionTimeout());
            return DocumentPreview.notPreviewable();
        } catch (ExecutionException e) {
            // Tika passes on an IOException only when reading the file failed; a parser's own
            // IOException, as on a corrupt file, reaches us as a TikaException. Opening the file
            // and the RTF rewrite read it outside Tika, so their IOExceptions are read faults too.
            if (e.getCause() instanceof IOException io) {
                throw new UncheckedIOException("Could not read " + file, io);
            }
            log.warn("Could not convert {}: {}", file, e.getCause().toString());
            return DocumentPreview.notPreviewable();
        } catch (InterruptedException e) {
            cancel(future, claimed);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while converting " + file, e);
        }
    }

    private void cancel(Future<DocumentPreview> future, AtomicBoolean claimed) {
        future.cancel(true);
        if (claimed.compareAndSet(false, true)) {
            permits.release();
        }
    }

    private DocumentPreview extract(Path file, boolean rtf) throws Exception {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getFileName().toString());
        BodyContentHandler handler = new BodyContentHandler(properties.maxDocumentChars());
        boolean truncated = false;
        try (InputStream in = rtf ? rtfWithTables(file) : TikaInputStream.get(file, metadata)) {
            PARSER.parse(in, handler, metadata, new ParseContext());
        } catch (Exception e) {
            if (!WriteLimitReachedException.isWriteLimitReached(e)) {
                throw e;
            }
            truncated = true;
        }
        String text = handler.toString();
        if (rtf) {
            // Each row's last \cell leaves a tab at the end of its line.
            text = TRAILING_TABS.matcher(text).replaceAll("");
        }
        return DocumentPreview.text(text, truncated);
    }

    private static InputStream rtfWithTables(Path file) throws IOException {
        byte[] rtf = Files.readAllBytes(file);
        return TikaInputStream.get(new ByteArrayInputStream(rtf, 0, rewriteRtfTables(rtf)));
    }

    /**
     * Rewrites the table control words in place, so the file is held only once, and returns the new
     * length. A replacement is never longer than what it replaces: {@code \cell} becomes
     * {@code \tab}, {@code \row} becomes {@code \par}, each keeping its delimiting space.
     */
    private static int rewriteRtfTables(byte[] rtf) {
        // The matcher only ever reads ahead of the write position, so writing behind it is safe.
        Matcher m = RTF_TABLE_WORDS.matcher(new Latin1Chars(rtf));
        int read = 0;
        int write = 0;
        while (m.find()) {
            if (m.group(1) == null) {
                continue;
            }
            int length = m.start() - read;
            System.arraycopy(rtf, read, rtf, write, length);
            write += length;
            String replacement = (m.group(1).equals("row") ? "\\par" : "\\tab") + m.group(2);
            for (int i = 0; i < replacement.length(); i++) {
                rtf[write++] = (byte) replacement.charAt(i);
            }
            read = m.end();
        }
        System.arraycopy(rtf, read, rtf, write, rtf.length - read);
        return write + rtf.length - read;
    }

    /** RTF bytes seen as characters without copying them; RTF is 7-bit text with escapes. */
    private record Latin1Chars(byte[] bytes, int start, int end) implements CharSequence {

        Latin1Chars(byte[] bytes) {
            this(bytes, 0, bytes.length);
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public char charAt(int index) {
            return (char) (bytes[start + index] & 0xff);
        }

        @Override
        public CharSequence subSequence(int from, int to) {
            return new Latin1Chars(bytes, start + from, start + to);
        }

        @Override
        public String toString() {
            return new String(bytes, start, end - start, StandardCharsets.ISO_8859_1);
        }
    }

    static String extension(String listedName) {
        String name = listedName.toLowerCase(Locale.ROOT);
        int slash = name.lastIndexOf('/');
        name = name.substring(slash + 1);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1);
    }
}
