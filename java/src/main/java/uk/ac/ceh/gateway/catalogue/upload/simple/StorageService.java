package uk.ac.ceh.gateway.catalogue.upload.simple;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    void store(String id, MultipartFile file);
    List<FileInfo> filenames(String id);
    void delete(String id, String filename);
}
