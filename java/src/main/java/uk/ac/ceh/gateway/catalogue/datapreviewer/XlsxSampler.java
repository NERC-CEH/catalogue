package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Reads the first rows of the first non-empty sheet of an Excel workbook (.xlsx). Sheets are
 * streamed with POI's SAX reader and parsing stops once enough rows are read, so a sheet is never
 * held whole. The shared strings and styles parts are held whole, so each is read through a limit on
 * its uncompressed size: a workbook is a zip, and the file size cap callers apply says nothing about
 * how far a crafted one expands. Columns and scanned rows are capped for the same reason. Values are
 * formatted as Excel shows them, and formulas give their last calculated result.
 */
public class XlsxSampler {

    /** The most uncompressed bytes read from the shared strings part, and from the styles part. */
    static final long MAX_PART_BYTES = 64L * 1024 * 1024;
    /** Columns beyond this are left out of the preview. */
    static final int MAX_COLUMNS = 1000;
    /** Rows read, blank ones included, before giving up on a sheet that has not filled the preview. */
    static final int MAX_SCANNED_ROWS = 10_000;

    private XlsxSampler() {}

    /** The workbook expands beyond what the preview will hold in memory. */
    public static final class TooLargeException extends IOException {
        TooLargeException(String message) {
            super(message);
        }
    }

    /**
     * @throws TooLargeException when a part the sampler must hold whole expands beyond
     *                           {@link #MAX_PART_BYTES}
     * @throws IOException       when the file cannot be read; a file that is not a workbook is a
     *                           not-previewable result instead
     */
    public static CsvSample sample(Path file, int maxRows) throws IOException {
        return sample(file, maxRows, MAX_PART_BYTES);
    }

    static CsvSample sample(Path file, int maxRows, long maxPartBytes) throws IOException {
        try (OPCPackage workbook = OPCPackage.open(file.toFile(), PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(workbook);
            ReadOnlySharedStringsTable strings = sharedStrings(workbook, maxPartBytes);
            StylesTable styles = styles(workbook, maxPartBytes);
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            List<String> names = new ArrayList<>();
            CsvSample shown = null;
            String shownName = null;
            while (sheets.hasNext()) {
                try (InputStream sheet = sheets.next()) {
                    names.add(sheets.getSheetName());
                    if (shown == null) {
                        CsvSample sample = sheet(sheet, styles, strings, maxRows);
                        if (sample.previewable()) {
                            shown = sample;
                            shownName = sheets.getSheetName();
                        }
                    }
                }
            }
            return shown == null ? CsvSample.notPreviewable() : shown.inSheet(shownName, names);
        } catch (ZipException | OpenXML4JException | OpenXML4JRuntimeException | SAXException e) {
            // A limit hit inside a SAX parse may come back wrapped.
            if (causedBy(e, TooLargeException.class) instanceof TooLargeException tooLarge) {
                throw tooLarge;
            }
            // Not a workbook, or a damaged one: a fact about the file, not a read fault.
            return CsvSample.notPreviewable();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // POI's own complaints about content it cannot make sense of.
            return CsvSample.notPreviewable();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new IllegalStateException("No XML parser for spreadsheets", e);
        }
    }

    private static ReadOnlySharedStringsTable sharedStrings(OPCPackage workbook, long maxPartBytes)
        throws IOException, SAXException, OpenXML4JException {
        List<PackagePart> parts = workbook.getPartsByContentType(XSSFRelation.SHARED_STRINGS.getContentType());
        if (parts.isEmpty()) {
            return new ReadOnlySharedStringsTable(InputStream.nullInputStream());
        }
        try (InputStream in = limited(parts.getFirst(), maxPartBytes, "shared strings")) {
            return new ReadOnlySharedStringsTable(in);
        }
    }

    private static StylesTable styles(OPCPackage workbook, long maxPartBytes) throws IOException, OpenXML4JException {
        List<PackagePart> parts = workbook.getPartsByContentType(XSSFRelation.STYLES.getContentType());
        if (parts.isEmpty()) {
            return new StylesTable();
        }
        try (InputStream in = limited(parts.getFirst(), maxPartBytes, "styles")) {
            return new StylesTable(in);
        }
    }

    private static InputStream limited(PackagePart part, long limit, String name) throws IOException {
        return new LimitedInputStream(part.getInputStream(), limit, name);
    }

    private static Throwable causedBy(Throwable e, Class<? extends Throwable> type) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (type.isInstance(cause)) {
                return cause;
            }
            if (cause instanceof SAXException sax && sax.getException() != null && type.isInstance(sax.getException())) {
                return sax.getException();
            }
        }
        return null;
    }

    /** Fails, rather than truncating, once more than {@code limit} bytes have been read. */
    private static final class LimitedInputStream extends FilterInputStream {
        private final long limit;
        private final String name;
        private long read;

        LimitedInputStream(InputStream in, long limit, String name) {
            super(in);
            this.limit = limit;
            this.name = name;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                count(1);
            }
            return b;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int n = super.read(buffer, offset, length);
            if (n > 0) {
                count(n);
            }
            return n;
        }

        private void count(int n) throws TooLargeException {
            read += n;
            if (read > limit) {
                throw new TooLargeException("Workbook " + name + " expands beyond " + limit + " bytes");
            }
        }
    }

    private static CsvSample sheet(InputStream sheet, StylesTable styles, ReadOnlySharedStringsTable strings, int maxRows)
        throws IOException, SAXException, javax.xml.parsers.ParserConfigurationException {
        Rows rows = new Rows(maxRows);
        XMLReader parser = XMLHelper.newXMLReader();
        parser.setContentHandler(new XSSFSheetXMLHandler(styles, strings, rows, new DataFormatter(), false));
        try {
            parser.parse(new InputSource(sheet));
        } catch (EnoughRows done) {
            // Stopped on purpose, with a row beyond those kept.
        }
        return rows.sample();
    }

    /** Thrown to stop the parse; carries no stack trace, as it is not a failure. */
    static final class EnoughRows extends RuntimeException {
        EnoughRows() {
            super(null, null, false, false);
        }
    }

    /**
     * Collects non-blank rows, the first as the header, placing each cell in its own column up to
     * {@link #MAX_COLUMNS}. Stops after {@link #MAX_SCANNED_ROWS} rows, blank or not, so a sheet of
     * empty rows is not read to its end.
     */
    static final class Rows implements SheetContentsHandler {
        private final int maxRows;
        private int scanned;
        private List<String> header;
        private final List<List<String>> rows = new ArrayList<>();
        private List<String> current;
        private boolean more;

        Rows(int maxRows) {
            this.maxRows = maxRows;
        }

        @Override
        public void startRow(int rowNum) {
            current = new ArrayList<>();
        }

        @Override
        public void cell(String reference, String value, XSSFComment comment) {
            int column = reference == null ? current.size() : new CellReference(reference).getCol();
            if (column < 0 || column >= MAX_COLUMNS) {
                return;
            }
            while (current.size() < column) {
                current.add("");
            }
            current.add(value == null ? "" : value);
        }

        @Override
        public void endRow(int rowNum) {
            if (++scanned > MAX_SCANNED_ROWS) {
                more = true;
                throw new EnoughRows();
            }
            if (current.stream().allMatch(String::isBlank)) {
                return;
            }
            if (header == null) {
                header = current;
                return;
            }
            if (rows.size() == maxRows) {
                more = true;
                throw new EnoughRows();
            }
            rows.add(current);
        }

        CsvSample sample() {
            if (header == null) {
                return CsvSample.notPreviewable();
            }
            int width = Math.max(header.size(), rows.stream().mapToInt(List::size).max().orElse(0));
            return new CsvSample(pad(header, width), rows.stream().map(row -> pad(row, width)).toList(), more, true);
        }

        private static List<String> pad(List<String> cells, int width) {
            List<String> padded = new ArrayList<>(cells);
            while (padded.size() < width) {
                padded.add("");
            }
            return padded;
        }
    }
}
