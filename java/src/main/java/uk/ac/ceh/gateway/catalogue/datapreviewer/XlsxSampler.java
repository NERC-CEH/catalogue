package uk.ac.ceh.gateway.catalogue.datapreviewer;

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
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Reads the first rows of the first non-empty sheet of an Excel workbook (.xlsx). Sheets are
 * streamed with POI's SAX reader and parsing stops once enough rows are read, so a sheet is never
 * held whole; the shared strings table is, which is why callers cap the file size. Values are
 * formatted as Excel shows them, and formulas give their last calculated result.
 */
public class XlsxSampler {

    private XlsxSampler() {}

    /**
     * @throws IOException when the file cannot be read; a file that is not a workbook is a
     *                     not-previewable result instead
     */
    public static CsvSample sample(Path file, int maxRows) throws IOException {
        try (OPCPackage workbook = OPCPackage.open(file.toFile(), PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(workbook);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(workbook);
            StylesTable styles = reader.getStylesTable();
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
            // Not a workbook, or a damaged one: a fact about the file, not a read fault.
            return CsvSample.notPreviewable();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // POI's own complaints about content it cannot make sense of.
            return CsvSample.notPreviewable();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new IllegalStateException("No XML parser for spreadsheets", e);
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
    private static final class EnoughRows extends RuntimeException {
        EnoughRows() {
            super(null, null, false, false);
        }
    }

    /** Collects non-blank rows, the first as the header, placing each cell in its own column. */
    private static final class Rows implements SheetContentsHandler {
        private final int maxRows;
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
            while (current.size() < column) {
                current.add("");
            }
            current.add(value == null ? "" : value);
        }

        @Override
        public void endRow(int rowNum) {
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
