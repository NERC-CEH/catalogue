package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

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

    static final int BIG_PAGE_SIZE = 1000000;

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
        log.info("Creating {}", this);
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
        val response = get(datasetId, "eidchub", 1, BIG_PAGE_SIZE);
        writer.println("path,checksum");
        response.getData().forEach(fileInfo ->
            writer.println(format("%s/%s,%s", fileInfo.getDatasetId(), fileInfo.getPath(), fileInfo.getHash()))
        );
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

    @SneakyThrows
    public void upload(String datasetId, String username, MultipartFile multipartFile) {
        val filename = Optional.ofNullable(multipartFile.getOriginalFilename())
            .orElseThrow(() -> new UploadException(format("Missing filename in upload to %s", datasetId)))
            .toLowerCase(Locale.ROOT)
            .replace(' ', '-');
        if (filename.isBlank()) {
            throw new UploadException(format("Filename is blank in upload to %s", datasetId));
        }
        log.debug("Adding {} to {}", filename, datasetId);
        val directory = Paths.get(uploadLocation, datasetId);
        if ( !Files.isDirectory(directory)) {
            val created = directory.toFile().mkdirs();
            log.debug("created directory: {} {}", directory, created);
        }
        val file = Paths.get(directory.toString(), filename).toFile();
        log.debug("new file {}", file);
        register(datasetId, filename, username, multipartFile.getSize());
        multipartFile.transferTo(file);
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
