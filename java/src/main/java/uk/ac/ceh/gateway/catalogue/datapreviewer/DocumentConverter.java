package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.exception.WriteLimitReachedException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/**
 * Turns a supporting document into something the documents pane can show: text for RTF, Word,
 * PDF and plain text, a table for CSV, or a reason it cannot be shown.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentConverter {

    private static final Set<String> TIKA_EXTENSIONS = Set.of("rtf", "doc", "docx", "pdf");
    private static final Parser PARSER = new AutoDetectParser();
    private static final ThreadFactory THREADS = Thread.ofPlatform().name("document-converter").daemon().factory();

    // Tika's RTF parser drops table structure: every \cell ends a paragraph, so a dictionary table
    // comes out one cell per line and the columns can no longer be told apart. Rewriting \cell to
    // \tab and \row to \par before parsing puts each row on one line with tab-separated cells.
    // An escaped backslash (\\) is matched first so that literal text such as "\\cell" is left alone.
    private static final Pattern RTF_TABLE_WORDS = Pattern.compile("\\\\\\\\|\\\\(cell|row)(?![a-zA-Z])-?\\d* ?");
    private static final Pattern TRAILING_TABS = Pattern.compile("\\t+$", Pattern.MULTILINE);

    private final CsvSampler csvSampler;
    private final DataPreviewProperties properties;

    public DocumentPreview convert(Path file) {
        String extension = extension(file);
        try {
            if (extension.equals("csv")) {
                try (InputStream in = Files.newInputStream(file)) {
                    CsvSample sample = csvSampler.sample(in, properties.maxCsvBytes(), properties.previewRows());
                    return sample.previewable() ? DocumentPreview.table(sample) : DocumentPreview.notPreviewable();
                }
            }
            if (!extension.equals("txt") && !TIKA_EXTENSIONS.contains(extension)) {
                return DocumentPreview.notPreviewable();
            }
            if (Files.size(file) > properties.maxDocumentBytes()) {
                return DocumentPreview.tooLarge();
            }
            if (extension.equals("txt")) {
                return truncate(CsvSampler.decode(Files.readAllBytes(file)));
            }
            return extractWithTimeout(file, extension.equals("rtf"));
        } catch (IOException | RuntimeException e) {
            log.warn("Could not convert supporting document {}: {}", file, e.toString());
            return DocumentPreview.notPreviewable();
        }
    }

    private DocumentPreview truncate(String text) {
        int max = properties.maxDocumentChars();
        if (text.length() <= max) {
            return DocumentPreview.text(text, false);
        }
        // Never end on half of a surrogate pair.
        int end = max > 0 && Character.isHighSurrogate(text.charAt(max - 1)) ? max - 1 : max;
        return DocumentPreview.text(text.substring(0, end), true);
    }

    private DocumentPreview extractWithTimeout(Path file, boolean rtf) {
        // One executor per conversion, so a parse that ignores interruption cannot hold up the next.
        ExecutorService executor = Executors.newSingleThreadExecutor(THREADS);
        Future<DocumentPreview> future = executor.submit(() -> extract(file, rtf));
        try {
            return future.get(properties.conversionTimeout().toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("Converting supporting document {} took longer than {}", file, properties.conversionTimeout());
            return DocumentPreview.notPreviewable();
        } catch (ExecutionException e) {
            log.warn("Could not convert supporting document {}: {}", file, e.getCause().toString());
            return DocumentPreview.notPreviewable();
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            return DocumentPreview.notPreviewable();
        } finally {
            executor.shutdownNow();
        }
    }

    private DocumentPreview extract(Path file, boolean rtf) throws IOException, SAXException, TikaException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getFileName().toString());
        BodyContentHandler handler = new BodyContentHandler(properties.maxDocumentChars());
        boolean truncated = false;
        try (InputStream in = rtf
            ? TikaInputStream.get(new ByteArrayInputStream(rewriteRtfTables(Files.readAllBytes(file))))
            : TikaInputStream.get(file, metadata)) {
            PARSER.parse(in, handler, metadata, new ParseContext());
        } catch (IOException | SAXException | TikaException e) {
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

    private static byte[] rewriteRtfTables(byte[] rtf) {
        // RTF is 7-bit text with escapes, so a Latin-1 round trip leaves every other byte unchanged.
        String source = new String(rtf, StandardCharsets.ISO_8859_1);
        String rewritten = RTF_TABLE_WORDS.matcher(source).replaceAll(match -> {
            String word = match.group();
            if (word.equals("\\\\")) {
                return Matcher.quoteReplacement(word);
            }
            return Matcher.quoteReplacement(word.startsWith("\\row") ? "\\par " : "\\tab ");
        });
        return rewritten.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String extension(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1);
    }
}
