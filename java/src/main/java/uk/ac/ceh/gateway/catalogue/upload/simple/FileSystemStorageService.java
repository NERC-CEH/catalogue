package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Stream;

@Slf4j
@Service
@Profile("upload:simple")
public class FileSystemStorageService implements StorageService {
    @Override
    public void store(String id, MultipartFile file) {
        log.info("In {} storing {}", id, file.getOriginalFilename());
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
