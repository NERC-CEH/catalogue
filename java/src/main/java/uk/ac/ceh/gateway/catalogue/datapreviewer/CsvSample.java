package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.util.List;

/**
 * The start of a table, from delimited text or a spreadsheet, ready to show as a preview table.
 *
 * @param header      column names from the first record
 * @param rows        data rows, each padded to the header width
 * @param moreRows    true when the file holds rows beyond those returned
 * @param previewable false when the file is not usable as a table (binary, empty, no line breaks)
 * @param sheet       for a workbook, the name of the sheet shown; otherwise null
 * @param sheets      for a workbook, the names of all its sheets in order; otherwise empty
 */
public record CsvSample(
    List<String> header,
    List<List<String>> rows,
    boolean moreRows,
    boolean previewable,
    String sheet,
    List<String> sheets
) {

    public CsvSample {
        sheets = sheets == null ? List.of() : List.copyOf(sheets);
    }

    public CsvSample(List<String> header, List<List<String>> rows, boolean moreRows, boolean previewable) {
        this(header, rows, moreRows, previewable, null, List.of());
    }

    public static CsvSample notPreviewable() {
        return new CsvSample(List.of(), List.of(), false, false);
    }

    /** This sample, labelled as {@code sheet} of a workbook with {@code sheets}. */
    public CsvSample inSheet(String sheet, List<String> sheets) {
        return new CsvSample(header, rows, moreRows, previewable, sheet, sheets);
    }
}
