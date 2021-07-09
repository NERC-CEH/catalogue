package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static java.lang.String.format;

@Profile("upload:hubbub")
@Slf4j
@ToString(of = "uploadLocation")
@Service
public class UploadDocumentService {
    private final HubbubService hubbubService;
    private final String uploadLocation;
    private final ExecutorService threadPool;

    private static final String[] VISIBLE_STATUS = new String[]{"VALID", "MOVING_FROM", "MOVING_TO", "VALIDATING_HASH", "NO_HASH", "WRITING", "REMOVED_UNKNOWN", "ZIPPED_UNKNOWN", "ZIPPED_UNKNOWN_MISSING", "MOVED_UNKNOWN", "MOVED_UNKNOWN_MISSING", "UNKNOWN", "UNKNOWN_MISSING", "MISSING", "MISSING_UNKNOWN", "CHANGED_MTIME", "CHANGED_HASH", "INVALID", "MOVING_FROM_ERROR", "MOVING_TO_ERROR"};

    public UploadDocumentService(
        HubbubService hubbubService,
        @Value("${hubbub.location}") String uploadLocation,
        ExecutorService threadPool
    ) {
        this.hubbubService = hubbubService;
        this.uploadLocation = uploadLocation;
        this.threadPool = threadPool;
        log.info("Creating {}", this);
    }

    public UploadDocument get(String id) {
        return get(id, 1, 1, 1, new String[0]);
    }

    public UploadDocument get(String id, int documentsPage, int datastorePage, int supportingDocumentsPage) {
        return get(id, documentsPage, datastorePage, supportingDocumentsPage, VISIBLE_STATUS);
    }

    private UploadDocument get(String id, int documentsPage, int datastorePage, int supportingDocumentsPage, String... status) {
        val dropboxRes = hubbubService.get(format("/dropbox/%s", id), documentsPage, status);
        log.debug("dropbox is {}", dropboxRes);
        val eidchubRes = hubbubService.get(format("/eidchub/%s", id), datastorePage, status);
        log.debug("eidchub is {}", eidchubRes);
        val supportingDocumentRes = hubbubService.get(format("/supporting-documents/%s", id), supportingDocumentsPage, status);
        log.debug("supporting documents is {}", supportingDocumentRes);
        val document = new UploadDocument(id, dropboxRes, eidchubRes, supportingDocumentRes);
        log.debug("Getting {} for status {}", document, status);
        return document;
    }

    @SneakyThrows
    public void getCsv(PrintWriter writer, String id) {
        log.debug("Getting CSV for {}", id);
        val first = hubbubService.get(id);
        val total = first.getPagination().getTotal();
        val eidchub = hubbubService.get(format("/eidchub/%s", id), 1, total).getData();
        eidchub.stream()
                .filter(fileInfo -> fileInfo.getStatus().equals("VALID"))
                .forEach(fileInfo -> {
                    val path = fileInfo.getTruncatedPath();
                    val hash = fileInfo.getHash();
                    writer.println(format("%s,%s", path, hash));
                });
    }

    public UploadDocument add(String id, String filename, MultipartFile f) {
        log.debug("Adding {} to {}", filename, id);
        threadPool.execute(() -> {
            try (InputStream in = f.getInputStream()) {
                val directory = folders.get("documents");
                val path = directory.getPath() + "/" + id + "/" + filename;
                val file = new File(path);
                file.setReadable(true);
                file.setWritable(false, true);
                file.setExecutable(false);
                writing(id, format("/dropbox/%s/%s", id, filename), in.available());
                FileUtils.copyInputStreamToFile(in, file);
                accept(id, format("/dropbox/%s/%s", id, filename));
                validateFile(id, format("/dropbox/%s/%s", id, filename));
            } catch (IOException err) {
                // TODO: check why this exception is being swallowed
                log.error(format("Error adding file (id=%s filename=%s)", id, filename), err);
            }
        });
        return get(id);
    }

    public UploadDocument delete(String id, String filename) {
        log.debug("Deleting {} from {}", filename, id);
        hubbubService.delete(filename);
        return get(id);
    }

    private UploadDocument get(String id, int documentsPage, int datastorePage, int supportingDocumentsPage, String... status) {
        val dropboxRes = hubbubService.get(format("/dropbox/%s", id), documentsPage, status);
        log.debug("dropbox is {}", dropboxRes);
        val eidchubRes = hubbubService.get(format("/eidchub/%s", id), datastorePage, status);
        log.debug("eidchub is {}", eidchubRes);
        val supportingDocumentRes = hubbubService.get(format("/supporting-documents/%s", id), supportingDocumentsPage, status);
        log.debug("supporting documents is {}", supportingDocumentRes);
        val document = new UploadDocument(id, dropboxRes, eidchubRes, supportingDocumentRes);
        log.debug("Getting {} for status {}", document, status);
        return document;
    }

    public void move(String path, String to) {
        threadPool.execute(() -> hubbubService.postQuery("/move", path, "to", to));
    }

    public void moveAllToDataStore(String id) {
        threadPool.execute(() -> hubbubService.post("/move_all", id));
    }

    public UploadDocument validateFile(String id, String filename) {
        log.debug("Validating {} for {}", filename, id);
        hubbubService.postQuery("/validate", filename, "force", "true");
        return get(id);
    }

    public void validate(String path) {
        hubbubService.postQuery("/validate", path, "force", "true");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File newFileWithPermissionsSet(String id, String filename) {
        val path = uploadLocation + "/" + id + "/" + filename;
        log.debug("new file {}", path);
        val file = new File(path);
        file.setReadable(true);
        file.setWritable(false, true);
        file.setExecutable(false);
        return file;
    }

    private void writing(String path, int size) {
        hubbubService.postQuery("/writing", path, "size", format("%d", size));
    }
}