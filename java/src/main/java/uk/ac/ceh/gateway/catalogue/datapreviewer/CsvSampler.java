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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

/**
 * Reads the first rows of a CSV without ever holding more than a fixed number of bytes.
 * Stateless and independent of configuration; callers supply the limits.
 */
@Component
public class CsvSampler {

    private static final CSVFormat FORMAT = CSVFormat.RFC4180.builder()
        .setIgnoreSurroundingSpaces(false)
        .get();

    public CsvSample sample(InputStream in, int maxBytes, int maxRows) {
        byte[] bytes;
        boolean capped;
        try {
            bytes = in.readNBytes(maxBytes);
            capped = bytes.length == maxBytes && in.read() != -1;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (bytes.length == 0 || contains(bytes, (byte) 0)) {
            return CsvSample.notPreviewable();
        }
        if (capped) {
            int end = lastLineBreak(bytes);
            if (end < 0) {
                return CsvSample.notPreviewable();
            }
            bytes = Arrays.copyOf(bytes, end + 1);
        }

        String text = decode(bytes);
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }

        List<String> header = null;
        List<List<String>> rows = new ArrayList<>();
        boolean more = capped;
        try (CSVParser parser = FORMAT.parse(new StringReader(text))) {
            for (CSVRecord record : parser) {
                List<String> cells = record.toList();
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
        } catch (IOException | UncheckedIOException | IllegalStateException e) {
            // Malformed quoting, typically a quoted field cut by the byte cap: keep what parsed.
            more = true;
        }
        if (header == null) {
            return CsvSample.notPreviewable();
        }
        return new CsvSample(header, rows, more, true);
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

    private static String decode(byte[] bytes) {
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
