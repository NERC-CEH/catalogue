package uk.ac.ceh.gateway.catalogue.upload.simple;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    void store(String id, MultipartFile file);
    Stream<Path> loadAll(String id);
    void delete(String id, String filename);
}
