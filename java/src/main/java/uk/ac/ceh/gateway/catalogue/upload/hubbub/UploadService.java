package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.String.format;
import static org.springframework.http.HttpMethod.*;
import static uk.ac.ceh.gateway.catalogue.util.Headers.withBasicAuth;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
@Service
public class UploadService {
    private final RestTemplate restTemplate;
    @ToString.Include
    private final String address;
    @ToString.Include
    private final String username;
    private final String password;
    @ToString.Include
    private final String uploadLocation;

    static final int PAGE_SIZE = 100000;

    public UploadService(
        @Qualifier("normal") RestTemplate restTemplate,
        @Value("${hubbub.url}") String address,
        @Value("${hubbub.username}") String username,
        @Value("${hubbub.password}") String password,
        @Value("${hubbub.location}") String uploadLocation
    ) {
        this.restTemplate = restTemplate;
        this.address = address;
        this.username = username;
        this.password = password;
        this.uploadLocation = uploadLocation;
        log.info("Creating");
    }

    public void accept(String datasetId, String datastore, String path, String user) {
        val urlTemplate = format("%s/accept/{datasetId}/{datastore}?path={path}&username={user}", address);
        restTemplate.exchange(
            urlTemplate,
            POST,
            new HttpEntity<>(withBasicAuth(username, password)),
            Void.class,
            datasetId,
            datastore,
            path,
            user
        );
    }

    public void cancel(String datasetId, String datastore, String path, String user) {
        val urlTemplate = format("%s/cancel/{datasetId}/{datastore}?path={path}&username={user}", address);
        restTemplate.exchange(
            urlTemplate,
            POST,
            new HttpEntity<>(withBasicAuth(username, password)),
            Void.class,
            datasetId,
            datastore,
            path,
            user
        );
    }

    @SneakyThrows
    public void csv(PrintWriter writer, String datasetId) {
        log.debug("Getting CSV for {}", datasetId);

        writer.println("path,SHA256_checksum");
        writer.flush();

        int page = 1;
        while (true) {
            HubbubResponse current;
            try {
                current = get(datasetId, "eidchub", page, PAGE_SIZE);
            } catch (Exception ex) {
                log.error("Failed to fetch page {} for {} — CSV will be truncated", page, datasetId, ex);
                writer.println(format(
                    "# ERROR: checksum report incomplete — failed to retrieve page %d. This file is a partial export.",
                    page));
                writer.flush();
                return;
            }

            List<HubbubResponse.FileInfo> fileInfos = current.getData();
            if (fileInfos.isEmpty()) {
                return;
            }

            fileInfos.forEach(fileInfo ->
                writer.println(format("%s/%s,%s",
                    fileInfo.getDatasetId(), fileInfo.getPath(),
                    fileInfo.getSha256()))
            );
            writer.flush();

            HubbubResponse.Meta meta = current.getMeta();
            if (meta.getCurrentPage() >= meta.getLastPage()) {
                return;
            }
            page++;
        }
    }

    public void delete(String datasetId, String datastore, String path, String user) {
        val urlTemplate = format("%s/delete/{datasetId}/{datastore}?path={path}&username={user}", address);
        restTemplate.exchange(
            urlTemplate,
            DELETE,
            new HttpEntity<>(withBasicAuth(username, password)),
            Void.class,
            datasetId,
            datastore,
            path,
            user
        );
    }

    public HubbubResponse get(String datasetId, String datastore, int page, int size) {
        val urlTemplate = format("%s/{datasetId}/{datastore}?page={page}&size={size}", address);
        val response = restTemplate.exchange(
            urlTemplate,
            GET,
            new HttpEntity<>(withBasicAuth(username, password)),
            HubbubResponse.class,
            datasetId,
            datastore,
            page,
            size
        );
        return response.getBody();
    }

    public HubbubResponse get(String datasetId, String datastore, String path) {
        val urlTemplate = format("%s/{datasetId}/{datastore}?path={path}", address);
        val response = restTemplate.exchange(
            urlTemplate,
            GET,
            new HttpEntity<>(withBasicAuth(username, password)),
            HubbubResponse.class,
            datasetId,
            datastore,
            path
        );
        return response.getBody();
    }

    public void move(String datasetId, String datastore, Optional<String> possiblePath, String user, String destination) {
        if (possiblePath.isPresent()) {
            val urlTemplate = format("%s/move/{datasetId}/{datastore}?path={path}&username={user}&to={destination}", address);
            restTemplate.exchange(
                urlTemplate,
                POST,
                new HttpEntity<>(withBasicAuth(username, password)),
                Void.class,
                datasetId,
                datastore,
                possiblePath.get(),
                user,
                destination
            );
        } else {
            val urlTemplate = format("%s/move/{datasetId}/{datastore}?username={user}&to={destination}",address);
            restTemplate.exchange(
                urlTemplate,
                POST,
                new HttpEntity<>(withBasicAuth(username, password)),
                Void.class,
                datasetId,
                datastore,
                user,
                destination
            );
        }
    }

    private String sanitisedFilename(String datasetId, String filename) {
        String sanitisedFilename = filename
            .toLowerCase(Locale.ROOT)
            .replace(' ', '-');
        if (sanitisedFilename.isBlank()) {
            throw new UploadException(format("Filename is blank in upload to %s", datasetId));
        }

        return sanitisedFilename;
    }

    @SneakyThrows
    public void upload(String datasetId, String username, MultipartFile multipartFile, String filename) {
        if (filename == null) {
            throw new UploadException(format("Missing filename in upload to %s", datasetId));
        }

        String sanitisedFilename = sanitisedFilename(datasetId, filename);
        Path path = Path.of(uploadLocation, datasetId, sanitisedFilename);
        Path uploadPath = path.getParent();
        Files.createDirectories(uploadPath);

        if (FilenameUtils.getExtension(filename).equals("zip")) {
            ZipInputStream zipStream = new ZipInputStream(multipartFile.getInputStream());
            String unzipPath = FilenameUtils.getBaseName(filename);
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.toLowerCase().contains("__macosx")) {
                    log.debug("Skipping macOS metadata file: {}", entryName);
                    continue;
                }
                String unZipFileName = Paths.get(unzipPath, sanitisedFilename(datasetId, entry.getName())).toString();
                Path resolvedPath = uploadPath.resolve(unZipFileName);
                if (entry.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    if (Files.exists(resolvedPath)) {
                        throw new ResponseStatusException(HttpStatusCode.valueOf(409), unZipFileName + " already exists");
                    }
                    log.debug("Unzip adding {} to {}", unZipFileName, datasetId);
                    Files.createDirectories(resolvedPath.getParent());
                    long fileSize = zipStream.transferTo(Files.newOutputStream(resolvedPath));
                    try {
                        register(datasetId, unZipFileName, username, fileSize);
                    } catch (Exception ex) {
                        Files.delete(resolvedPath);
                        throw ex;
                    }
                }
            }
        } else {
            log.debug("Adding {} to {}", sanitisedFilename, datasetId);
            register(datasetId, sanitisedFilename, username, multipartFile.getSize());
            multipartFile.transferTo(path.toFile());
        }
    }

    public void validate(String datasetId, String datastore, Optional<String> possiblePath, String user) {
        if (possiblePath.isPresent()) {
            val urlTemplate = format("%s/validate/{datasetId}/{datastore}?path={path}&username={user}", address);
            restTemplate.exchange(
                urlTemplate,
                POST,
                new HttpEntity<>(withBasicAuth(username, password)),
                Void.class,
                datasetId,
                datastore,
                possiblePath.get(),
                user
            );
        } else {
            val urlTemplate = format("%s/validate/{datasetId}/{datastore}?username={user}", address);
            restTemplate.exchange(
                urlTemplate,
                POST,
                new HttpEntity<>(withBasicAuth(username, password)),
                Void.class,
                datasetId,
                datastore,
                user
            );
        }
    }

    public void register(String datasetId, String user) {
        val urlTemplate = format("%s/register/{datasetId}?username={user}", address);
        restTemplate.exchange(
            urlTemplate,
            POST,
            new HttpEntity<>(withBasicAuth(username, password)),
            Void.class,
            datasetId,
            user
        );
    }

    private void register(String datasetId, String path, String user, long size) {
        val urlTemplate = format("%s/register/{datasetId}?path={path}&username={user}&size={size}", address);
        log.info("Registering - datasetId: {}, path: {}, user: {}, size: {}", datasetId, path, user, size);

        restTemplate.exchange(
            urlTemplate,
            POST,
            new HttpEntity<>(withBasicAuth(username, password)),
            Void.class,
            datasetId,
            path,
            user,
            size
        );
    }

    public void unregister(String datasetId, String datastore, String path, String user) {
        val urlTemplate = format("%s/unregister/{datasetId}/{datastore}?path={path}&username={user}", address);
        restTemplate.exchange(
            urlTemplate,
            POST,
            new HttpEntity<>(withBasicAuth(username, password)),
            Void.class,
            datasetId,
            datastore,
            path,
            user
        );
    }

    public void hashDropbox(String datasetId, String user) {
        val urlTemplate = format("%s/hash/{datasetId}?username={user}", address);
        restTemplate.exchange(
            urlTemplate,
            POST,
            new HttpEntity<>(withBasicAuth(username, password)),
            Void.class,
            datasetId,
            user
        );
    }
}
