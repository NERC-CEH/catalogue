package uk.ac.ceh.gateway.catalogue.datapreviewer;

/**
 * A supporting document or data file made ready to show in the preview.
 *
 * @param kind      how the file is shown
 * @param text      the extracted text when {@code kind} is {@link Kind#TEXT}, otherwise null; table
 *                  rows from RTF come out one per line with tab-separated cells
 * @param table     the first rows when {@code kind} is {@link Kind#TABLE}, otherwise null
 * @param truncated true when {@code text} was cut at the character limit
 * @param summary   the header summary when {@code kind} is {@link Kind#SUMMARY} (a NetCDF file),
 *                  otherwise null
 */
public record DocumentPreview(Kind kind, String text, CsvSample table, boolean truncated, NetcdfSummary summary) {

    public enum Kind { TEXT, TABLE, SUMMARY, NOT_PREVIEWABLE, TOO_LARGE }

    public DocumentPreview(Kind kind, String text, CsvSample table, boolean truncated) {
        this(kind, text, table, truncated, null);
    }

    static DocumentPreview text(String text, boolean truncated) {
        return new DocumentPreview(Kind.TEXT, text, null, truncated);
    }

    static DocumentPreview table(CsvSample table) {
        return new DocumentPreview(Kind.TABLE, null, table, false);
    }

    static DocumentPreview summary(NetcdfSummary summary) {
        return new DocumentPreview(Kind.SUMMARY, null, null, false, summary);
    }

    static DocumentPreview notPreviewable() {
        return new DocumentPreview(Kind.NOT_PREVIEWABLE, null, null, false);
    }

    static DocumentPreview tooLarge() {
        return new DocumentPreview(Kind.TOO_LARGE, null, null, false);
    }
}
