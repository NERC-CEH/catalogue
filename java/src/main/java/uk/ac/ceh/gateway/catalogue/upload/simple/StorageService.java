package uk.ac.ceh.gateway.catalogue.upload.simple;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileAlreadyExistsException;
import java.util.stream.Stream;

public interface StorageService {
    void store(String id, MultipartFile file) throws FileAlreadyExistsException;
    Stream<String> filenames(String id);
    void delete(String id, String filename);
}
