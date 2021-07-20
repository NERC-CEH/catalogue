package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.google.common.collect.ImmutableSet;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController.DATASTORE;
import static uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController.METADATA;

@Profile("upload:hubbub")
@Slf4j
@ToString(of = "uploadLocation")
@Service
public class UploadService {
    private final HubbubService hubbubService;
    private final String uploadLocation;
    private final Pattern acceptablePathStarts = Pattern.compile("^/(dropbox|eidchub|supporting-documents)/.*");
    private final Set<String> acceptableDestinations = ImmutableSet.of(DATASTORE, METADATA);

    static final String[] VISIBLE_STATUS = new String[]{
        "CHANGED_HASH",
        "CHANGED_MTIME",
        "INVALID",
        "MISSING",
        "MISSING_UNKNOWN",
        "MOVING_FROM",
        "MOVING_FROM_ERROR",
        "MOVING_TO",
        "MOVING_TO_ERROR",
        "MOVED_UNKNOWN",
        "MOVED_UNKNOWN_MISSING",
        "NO_HASH",
        "REMOVED_UNKNOWN",
        "UNKNOWN",
        "UNKNOWN_MISSING",
        "VALID",
        "VALIDATING_HASH",
        "WRITING",
        "ZIPPED_UNKNOWN",
        "ZIPPED_UNKNOWN_MISSING",
    };

    public UploadService(
        HubbubService hubbubService,
        @Value("${hubbub.location}") String uploadLocation
    ) {
        this.hubbubService = hubbubService;
        this.uploadLocation = uploadLocation;
        log.info("Creating");
    }

    public void accept(String path) {
        if (hasAcceptablePathStart(path)) {
            hubbubService.post("/accept", path);
        } else {
            throw new UploadException("Bad path: " + path);
        }
    }

    public void cancel(String path) {
        if (hasAcceptablePathStart(path)) {
            hubbubService.post("/cancel", path);
        } else {
            throw new UploadException("Bad path: " + path);
        }
    }

    private boolean hasAcceptablePathStart(String path) {
        return acceptablePathStarts.matcher(path).matches();
    }

    @SneakyThrows
    public void csv(PrintWriter writer, String id) {
        val path = format("/eidchub/%s", id);
        log.debug("Getting CSV for {}", path);
        val first = hubbubService.get(path);
        val total = first.getPagination().getTotal();
        val eidchub = hubbubService.get(path, 1, total).getData();
        eidchub.stream()
            .filter(fileInfo -> fileInfo.getStatus().equals("VALID"))
            .forEach(fileInfo -> {
                val truncatedPath = fileInfo.getTruncatedPath();
                val hash = fileInfo.getHash();
                writer.println(format("%s,%s", truncatedPath, hash));
            });
    }

    public void delete(String path) {
        if (hasAcceptablePathStart(path)) {
            hubbubService.delete(path);
        }  else {
            throw new UploadException("Bad path: " + path);
        }
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

    public void move(String path, String destination) {
        if (hasAcceptablePathStart(path) && hasAcceptableDestination(destination)) {
            hubbubService.postQuery("/move", path, "to", destination);
        } else {
            throw new UploadException(format("Bad path: %s or destination: %s", path, destination));
        }
    }

    private boolean hasAcceptableDestination(String destination) {
        return acceptableDestinations.contains(destination);
    }

    public void moveAllToDataStore(String id) {
        hubbubService.post("/move_all", id);
    }

    @SneakyThrows
    public void upload(String id, MultipartFile multipartFile) {
        val filename = multipartFile.getOriginalFilename();
        log.debug("Adding {} to {}", filename, id);
        val path = format("%s/%s/%s", uploadLocation, id, filename);
        log.debug("new file {}", path);
        val file = new File(path);
        val dropboxKey = format("/dropbox/%s/%s", id, filename);
        try (InputStream in = multipartFile.getInputStream()) {
            writing(dropboxKey, in.available());
            FileUtils.copyInputStreamToFile(in, file);
        }
        accept(dropboxKey);
        validate(dropboxKey);
    }

    public void validate(String path) {
        hubbubService.postQuery("/validate", path, "force", "true");
    }

    private void writing(String path, int size) {
        hubbubService.postQuery("/writing", path, "size", format("%d", size));
    }
}