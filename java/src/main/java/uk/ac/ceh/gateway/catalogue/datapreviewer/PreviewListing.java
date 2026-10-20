package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import uk.ac.ceh.gateway.catalogue.datapreviewer.PreviewManifest.PreviewFile;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse.FileInfo;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

/**
 * Builds a record's {@link PreviewManifest} from Hubbub. Makes no access check: callers go through
 * {@link DataPreviewService}, which checks access before this cache is consulted.
 */
@Component
public class PreviewListing {

    public static final String DATA_PREVIEW_MANIFEST_CACHE = "data-preview-manifest";

    static final String DATA_DATASTORE = "eidchub";
    static final String DOCUMENTS_DATASTORE = "supporting-documents";
    static final int DATA_PAGE_SIZE = 10000;
    static final int DOCUMENTS_PAGE_SIZE = 1000;

    // Hubbub states in which the listed file may be absent, partial or about to move on the share.
    private static final Set<String> UNRELIABLE = Set.of("MISSING", "INVALID", "MOVING_FROM", "MOVING_TO", "WRITING");

    private final UploadService uploadService;
    private final DataPreviewProperties properties;

    public PreviewListing(UploadService uploadService, DataPreviewProperties properties) {
        this.uploadService = uploadService;
        this.properties = properties;
    }

    @Cacheable(cacheNames = DATA_PREVIEW_MANIFEST_CACHE, key = "#recordId")
    public PreviewManifest manifest(String recordId) {
        // Only the first page is read: a record with more than DATA_PAGE_SIZE files is reported as
        // having "at least" the CSVs found there, rather than walking a listing of unbounded length.
        HubbubResponse data = list(recordId, DATA_DATASTORE, DATA_PAGE_SIZE);
        List<FileInfo> dataFiles = data == null ? List.of() : files(data);
        List<FileInfo> csvs = dataFiles.stream()
            .filter(PreviewListing::reliable)
            .filter(PreviewListing::isCsv)
            .sorted(Comparator.comparing(FileInfo::getPath))
            .toList();
        HubbubResponse.Meta meta = data == null ? null : data.getMeta();
        boolean minimum = meta != null && meta.getLastPage() > 1;
        int totalFiles = meta != null ? meta.getTotalFiles() : dataFiles.size();

        HubbubResponse docs = list(recordId, DOCUMENTS_DATASTORE, DOCUMENTS_PAGE_SIZE);
        List<FileInfo> documents = docs == null ? List.of() : files(docs).stream()
            .filter(PreviewListing::reliable)
            .sorted(Comparator.comparing(FileInfo::getPath))
            .toList();

        return new PreviewManifest(
            entries(csvs.subList(0, Math.min(csvs.size(), properties.maxCsvFiles()))),
            csvs.size(),
            minimum,
            totalFiles,
            entries(documents));
    }

    /**
     * The shortest trailing part of each path, in whole segments, that no other path in the list
     * ends with. A path that is a suffix of another, or a duplicate, keeps its full length.
     */
    public static List<String> labels(List<String> paths) {
        List<String[]> segments = paths.stream().map(p -> p.split("/")).toList();
        List<String> labels = new ArrayList<>(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            String[] mine = segments.get(i);
            int length = 1;
            while (length < mine.length && sharesSuffix(segments, i, length)) {
                length++;
            }
            labels.add(String.join("/", Arrays.copyOfRange(mine, mine.length - length, mine.length)));
        }
        return labels;
    }

    private static boolean sharesSuffix(List<String[]> segments, int index, int length) {
        String[] mine = segments.get(index);
        for (int j = 0; j < segments.size(); j++) {
            if (j == index) {
                continue;
            }
            String[] other = segments.get(j);
            if (other.length < length) {
                continue;
            }
            boolean same = true;
            for (int k = 1; k <= length && same; k++) {
                same = mine[mine.length - k].equals(other[other.length - k]);
            }
            if (same) {
                return true;
            }
        }
        return false;
    }

    private HubbubResponse list(String recordId, String datastore, int size) {
        try {
            return uploadService.get(recordId, datastore, 1, size);
        } catch (HttpClientErrorException.NotFound e) {
            // Hubbub answers 404 for a record it holds no files for.
            return null;
        }
    }

    private static List<FileInfo> files(HubbubResponse response) {
        return response.getData() == null
            ? List.of()
            : response.getData().stream().filter(Objects::nonNull).filter(f -> f.getPath() != null).toList();
    }

    private static boolean reliable(FileInfo file) {
        return file.getStatus() == null || !UNRELIABLE.contains(file.getStatus().toUpperCase(Locale.ROOT));
    }

    private static boolean isCsv(FileInfo file) {
        String mimeType = file.getMimeType();
        return file.getPath().toLowerCase(Locale.ROOT).endsWith(".csv")
            || (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("text/csv"));
    }

    private static List<PreviewFile> entries(List<FileInfo> files) {
        List<String> labels = labels(files.stream().map(FileInfo::getPath).toList());
        List<PreviewFile> entries = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); i++) {
            FileInfo file = files.get(i);
            long bytes = file.getBytes() == null ? 0 : file.getBytes();
            entries.add(new PreviewFile(file.getPath(), labels.get(i), bytes, file.getSha256()));
        }
        return entries;
    }
}
