package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.util.List;

/**
 * The start of a CSV file, ready to show as a preview table.
 *
 * @param header     column names from the first record
 * @param rows       data rows, each padded to the header width
 * @param moreRows   true when the file holds rows beyond those returned
 * @param previewable false when the file is not usable text (binary, empty, no line breaks)
 */
public record CsvSample(List<String> header, List<List<String>> rows, boolean moreRows, boolean previewable) {

    public static CsvSample notPreviewable() {
        return new CsvSample(List.of(), List.of(), false, false);
    }
}
