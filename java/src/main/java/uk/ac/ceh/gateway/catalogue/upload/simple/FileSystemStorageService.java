package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@Service
@Profile("upload:simple")
@ToString
public class FileSystemStorageService implements StorageService {
    private final String datastore;

    public FileSystemStorageService(
            @Value("${upload.simple.datastore}") String datastore
    ) {
        this.datastore = datastore;
        log.info("Creating {}", this);
    }

    @SneakyThrows
    @Override
    public void store(String id, MultipartFile file) {
        log.info("Storing {}/{}/{}", datastore, id, file.getOriginalFilename());

        val directory = Paths.get(datastore, id);
        if ( !Files.exists(directory)) {
            Files.createDirectory(directory);
        }

        val uploadFile= Paths.get(datastore, id, file.getOriginalFilename());
        if (Files.exists(uploadFile)) {
            throw new FileAlreadyExistsException(uploadFile.toString());
        }

        file.transferTo(uploadFile.toFile());
    }

    @Override
    public Stream<String> filenames(String id) {
        log.info("In {} loading all files", id);
        return Stream.empty();
    }

    @Override
    public void delete(String id, String filename) {
        log.info("In {} deleting {}", id, filename);
    }
}
