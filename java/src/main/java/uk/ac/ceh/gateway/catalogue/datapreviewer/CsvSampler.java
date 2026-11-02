package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

/**
 * Reads the first rows of delimited text (comma, tab, semicolon, pipe or runs of spaces) without
 * ever holding more than a fixed number of bytes. Stateless and independent of configuration;
 * callers supply the limits.
 */
@Component
public class CsvSampler {

    /** A field separator: one character, or any run of spaces and tabs. */
    public record Delimiter(char character, boolean whitespace) {
        public static final Delimiter COMMA = of(',');
        public static final Delimiter TAB = of('\t');
        public static final Delimiter PIPE = of('|');
        public static final Delimiter WHITESPACE = new Delimiter(' ', true);

        public static Delimiter of(char character) {
            return new Delimiter(character, false);
        }
    }

    /**
     * The start of a text file, decoded.
     *
     * @param text   the whole lines read, without a byte-order mark
     * @param capped true when the file goes on beyond {@code text}
     */
    public record Text(String text, boolean capped) {}

    // Tried in this order; a later one must split more fields to be chosen.
    private static final char[] CANDIDATES = {',', '\t', ';', '|'};
    // How many records detection looks at, and the share that must agree on the field count.
    private static final int DETECTION_RECORDS = 30;
    private static final double AGREEMENT = 0.9;
    private static final Pattern WHITESPACE_RUN = Pattern.compile("[ \\t]+");
    private static final Pattern NUMBER = Pattern.compile("[-+]?(\\d+\\.?\\d*|\\.\\d+)([eE][-+]?\\d+)?");
    // Prose lines often have the same number of words, so runs of spaces are taken as a delimiter
    // only for at least this many records whose data rows are mostly numbers.
    private static final int WHITESPACE_MIN_RECORDS = 3;

    /** Samples a file known to be a table: the detected delimiter, or a comma when none is found. */
    public CsvSample sample(InputStream in, int maxBytes, int maxRows) {
        return read(in, maxBytes)
            .map(text -> table(text, detect(text.text()).orElse(Delimiter.COMMA), maxRows))
            .orElseGet(CsvSample::notPreviewable);
    }

    /**
     * Reads at most {@code maxBytes}, cut back to the last whole line when the file is longer.
     *
     * @return empty when the file is empty, binary, or has no line break within the limit
     */
    public Optional<Text> read(InputStream in, int maxBytes) {
        byte[] bytes;
        boolean capped;
        try {
            bytes = in.readNBytes(maxBytes);
            capped = bytes.length == maxBytes && in.read() != -1;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (bytes.length == 0 || contains(bytes, (byte) 0)) {
            return Optional.empty();
        }
        if (capped) {
            int end = lastLineBreak(bytes);
            if (end < 0) {
                return Optional.empty();
            }
            bytes = Arrays.copyOf(bytes, end + 1);
        }
        String text = decode(bytes);
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        return Optional.of(new Text(text, capped));
    }

    /**
     * The delimiter that splits the opening records of {@code text} into the same number of fields
     * (at least two) as its first record, in nearly every record. Delimiters inside double quotes
     * are not counted. Runs of spaces are tried only when no single character qualifies, and only for
     * numeric data, as space-aligned data files are and lines of prose are not.
     *
     * @return empty when the text does not look like a table, such as prose or a single column
     */
    public static Optional<Delimiter> detect(String text) {
        List<String> records = records(text);
        if (records.size() < 2) {
            return Optional.empty();
        }
        Delimiter best = null;
        int bestFields = 1;
        for (char candidate : CANDIDATES) {
            int fields = agreedFields(records, line -> countOutsideQuotes(line, candidate) + 1);
            if (fields > bestFields) {
                best = Delimiter.of(candidate);
                bestFields = fields;
            }
        }
        if (best == null && looksLikeSpaceAlignedNumbers(records)) {
            best = Delimiter.WHITESPACE;
        }
        return Optional.ofNullable(best);
    }

    /** The header and first rows of {@code text}, split on {@code delimiter}. */
    public CsvSample table(Text text, Delimiter delimiter, int maxRows) {
        List<String> header = null;
        List<List<String>> rows = new ArrayList<>();
        boolean more = text.capped();
        try {
            for (List<String> cells : delimiter.whitespace() ? whitespaceRecords(text.text()) : parse(text.text(), delimiter)) {
                if (cells.stream().allMatch(String::isBlank)) {
                    continue;
                }
                if (header == null) {
                    header = cells;
                } else if (rows.size() == maxRows) {
                    more = true;
                    break;
                } else {
                    rows.add(pad(cells, header.size()));
                }
            }
        } catch (UncheckedIOException | IllegalStateException e) {
            // Malformed quoting, typically a quoted field cut by the byte cap: keep what parsed.
            more = true;
        }
        if (header == null) {
            return CsvSample.notPreviewable();
        }
        return new CsvSample(header, rows, more, true);
    }

    private static Iterable<List<String>> parse(String text, Delimiter delimiter) {
        CSVFormat format = CSVFormat.RFC4180.builder()
            .setDelimiter(delimiter.character())
            .setIgnoreSurroundingSpaces(false)
            .get();
        CSVParser parser;
        try {
            parser = format.parse(new StringReader(text));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return () -> parser.stream().map(CSVRecord::toList).iterator();
    }

    private static List<List<String>> whitespaceRecords(String text) {
        return text.lines()
            .map(String::strip)
            .map(line -> line.isEmpty() ? List.<String>of() : List.of(WHITESPACE_RUN.split(line)))
            .toList();
    }

    private static boolean looksLikeSpaceAlignedNumbers(List<String> records) {
        if (records.size() < WHITESPACE_MIN_RECORDS
            || agreedFields(records, line -> WHITESPACE_RUN.split(line.strip()).length) < 2) {
            return false;
        }
        long tokens = 0;
        long numbers = 0;
        for (String line : records.subList(1, records.size())) {
            for (String token : WHITESPACE_RUN.split(line.strip())) {
                tokens++;
                if (NUMBER.matcher(token).matches()) {
                    numbers++;
                }
            }
        }
        return numbers * 2 > tokens;
    }

    /**
     * The field count of the first record, when it is at least two and at least {@link #AGREEMENT}
     * of the records share it; otherwise 1. Rows shorter than the header, as from trailing empty
     * fields being left off, count against the agreement.
     */
    private static int agreedFields(List<String> records, ToIntFunction<String> fieldCount) {
        int header = fieldCount.applyAsInt(records.getFirst());
        if (header < 2) {
            return 1;
        }
        long agreeing = records.stream().filter(line -> fieldCount.applyAsInt(line) == header).count();
        return agreeing >= Math.ceil(records.size() * AGREEMENT) ? header : 1;
    }

    /**
     * The first {@link #DETECTION_RECORDS} non-blank records. A line break inside double quotes
     * does not end a record. Fewer records are returned when the text ends sooner.
     */
    private static List<String> records(String text) {
        List<String> records = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length() && records.size() < DETECTION_RECORDS; i++) {
            char c = text.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            }
            if (!quoted && (c == '\n' || c == '\r')) {
                if (!current.isEmpty() && !current.toString().isBlank()) {
                    records.add(current.toString());
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (records.size() < DETECTION_RECORDS && !current.toString().isBlank()) {
            records.add(current.toString());
        }
        return records;
    }

    private static int countOutsideQuotes(String line, char delimiter) {
        int count = 0;
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            } else if (c == delimiter && !quoted) {
                count++;
            }
        }
        return count;
    }

    private static boolean contains(byte[] bytes, byte value) {
        for (byte b : bytes) {
            if (b == value) {
                return true;
            }
        }
        return false;
    }

    private static int lastLineBreak(byte[] bytes) {
        for (int i = bytes.length - 1; i >= 0; i--) {
            if (bytes[i] == '\n' || bytes[i] == '\r') {
                return i;
            }
        }
        return -1;
    }

    // Package-private so DocumentConverter decodes plain text the same way.
    static String decode(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        }
    }

    private static List<String> pad(List<String> cells, int width) {
        if (cells.size() >= width) {
            return cells;
        }
        List<String> padded = new ArrayList<>(cells);
        while (padded.size() < width) {
            padded.add("");
        }
        return padded;
    }
}
