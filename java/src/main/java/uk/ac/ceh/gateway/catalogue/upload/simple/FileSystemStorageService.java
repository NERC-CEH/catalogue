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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

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
        try {
            log.info("Storing {}/{}/{}", datastore, id, file.getOriginalFilename());
            if (directoryDoesNotExist(id)) {
                Files.createDirectory(Paths.get(datastore, id));
            }

            val uploadFile = Paths.get(datastore, id, file.getOriginalFilename());
            if (Files.exists(uploadFile)) {
                throw new FileExitsException(id, file.getOriginalFilename());
            }

            file.transferTo(uploadFile.toFile());
        } catch (FileExitsException ex) {
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
            if (directoryDoesNotExist(id)) {
                throw new UserInputException(id, "Could not retrieve files");
            }
            val directory = Paths.get(datastore, id).toFile();
            val filenames = Optional.ofNullable(directory.list()).orElse(new String[0]);
            return Arrays.stream(filenames)
                .sorted()
                .map(FileInfo::new)
                .collect(Collectors.toList());
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
            val deleteFile = Paths.get(datastore, id, filename);
            Files.delete(deleteFile);
        } catch (NoSuchFileException ex) {
            throw new UserInputException(id, format("File not found %s", filename));
        }
        catch (Exception ex) {
            throw new StorageServiceException(id, ex.getMessage(), ex);
        }
    }

    private boolean directoryDoesNotExist(String id) {
        val directory = Paths.get(datastore, id);
        return !Files.exists(directory);
    }
}
