package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
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

    @Override
    @SneakyThrows
    public void store(String id, MultipartFile file) {
        log.info("Storing {}/{}/{}", datastore, id, file.getOriginalFilename());
        if (directoryDoesNotExist(id)) {
            Files.createDirectory(Paths.get(datastore, id));
        }

        val uploadFile= Paths.get(datastore, id, file.getOriginalFilename());
        if (Files.exists(uploadFile)) {
            throw new FileAlreadyExistsException(uploadFile.toString());
        }

        file.transferTo(uploadFile.toFile());
    }

    @Override
    @SneakyThrows
    public Stream<String> filenames(String id) {
        log.info("In {} loading all files", id);
        if (directoryDoesNotExist(id)) {
            throw new FileNotFoundException(id);
        }
        val directory = Paths.get(datastore, id).toFile();
        val filenames = Optional.ofNullable(directory.list()).orElse(new String[0]);
        return Stream.of(filenames);
    }

    @Override
    @SneakyThrows
    public void delete(String id, String filename) {
        log.info("In {} deleting {}", id, filename);
        val deleteFile = Paths.get(datastore, id, filename);
        Files.delete(deleteFile);
    }

    private boolean directoryDoesNotExist(String id) {
        val directory = Paths.get(datastore, id);
        return !Files.exists(directory);
    }
}
