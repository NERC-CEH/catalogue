package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.util.List;

/**
 * What a record's data preview can show, built from its Hubbub listing.
 *
 * @param csvFiles          the first CSV data files in path order, at most {@code maxCsvFiles}
 * @param csvCount          how many CSV data files were listed
 * @param csvCountIsMinimum true when the listing ran beyond the page read, so {@code csvCount} is
 *                          a lower bound
 * @param totalFiles        how many data files the dataset holds, of any format
 * @param documents         the supporting documents in path order
 */
public record PreviewManifest(
    List<PreviewFile> csvFiles,
    int csvCount,
    boolean csvCountIsMinimum,
    int totalFiles,
    List<PreviewFile> documents
) {

    public PreviewManifest {
        csvFiles = List.copyOf(csvFiles);
        documents = List.copyOf(documents);
    }

    public static PreviewManifest empty() {
        return new PreviewManifest(List.of(), 0, false, 0, List.of());
    }

    /**
     * @param path   the Hubbub path, relative to the record's folder on the share
     * @param label  the shortest trailing part of the path that tells it apart from its neighbours
     * @param bytes  the size Hubbub reports, or 0 when it reports none
     * @param sha256 the checksum Hubbub reports, which keys the content cache
     */
    public record PreviewFile(String path, String label, long bytes, String sha256) {}
}
