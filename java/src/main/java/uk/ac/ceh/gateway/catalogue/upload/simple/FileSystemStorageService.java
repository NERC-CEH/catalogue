package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Service
@Profile("upload-simple")
@ToString
public class FileSystemStorageService implements StorageService {
    private final String datastore;

    public FileSystemStorageService(
        @Value("${upload.simple.datastore}") String datastore
    ) {
        this.datastore = datastore;
        log.info("Creating");
    }

    @Override
    @SneakyThrows
    public void store(String id, MultipartFile file, String filename) {
        try {
            val path = Path.of(datastore, id, filename);
            log.info("Storing {}", path);
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                throw new FileExistsException(id, filename);
            }
            file.transferTo(path.toFile());
        } catch (FileExistsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StorageServiceException(id, ex.getMessage(), ex);
        }
    }

    @Override
    @SneakyThrows
    public List<FileInfo> filenames(String id) {
        try {
            log.info("In {} loading all files", id);
            val dir = Path.of(datastore, id);
            if (!Files.isDirectory(dir)) {
                throw new UserInputException(id, "Could not retrieve files");
            }
            try (val paths = Files.walk(dir, Integer.MAX_VALUE)) {
                return paths
                    .filter(Files::isRegularFile)
                    .map(dir::relativize)
                    .map(Path::toString)
                    .sorted()
                    .map(FileInfo::new)
                    .toList();
            }
        } catch (UserInputException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StorageServiceException(id, ex.getMessage(), ex);
        }
    }

    @Override
    @SneakyThrows
    public void delete(String id, String filename) {
        try {
            log.info("In {} deleting {}", id, filename);
            val deleteFile = Path.of(datastore, id, filename);
            Files.delete(deleteFile);
        } catch (NoSuchFileException ex) {
            throw new UserInputException(id, format("File not found %s", filename));
        } catch (Exception ex) {
            throw new StorageServiceException(id, ex.getMessage(), ex);
        }
    }
}
