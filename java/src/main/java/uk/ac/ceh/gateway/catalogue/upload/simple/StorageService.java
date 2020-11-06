package uk.ac.ceh.gateway.catalogue.upload.simple;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.stream.Stream;

public interface StorageService {
    void store(String id, MultipartFile file) throws FileAlreadyExistsException;
    Stream<String> filenames(String id) throws FileNotFoundException;
    void delete(String id, String filename) throws NoSuchFileException;
}
