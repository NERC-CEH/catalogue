package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;
import java.nio.file.Files;

import org.apache.commons.io.FilenameUtils;

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.util.HashUtils;

public class UploadFileBuilder {

    @SneakyThrows
    public static UploadFile create(Checksum checksum, UploadType type, String hash) {
        val file = checksum.getFile();
        val uploadFile = new UploadFile();
        val name = file.getName();
        uploadFile.setPath(file.getAbsolutePath());
        uploadFile.setType(type);
        uploadFile.setHash(hash);
        uploadFile.setId(name.replaceAll("[^\\w?]", "-"));
        uploadFile.setName(name);
        uploadFile.setEncoding("utf-8");

        if (file.exists()) {
            uploadFile.setFormat(FilenameUtils.getExtension(name));
            uploadFile.setMediatype(Files.probeContentType(file.toPath()));
            uploadFile.setBytes(file.length());
        }
        return uploadFile;
    }

    public static UploadFile create(File file, UploadType type, String hash) {
        return create(new Checksum(hash, file), type, hash);        
    }

    public static UploadFile create(File file, UploadType type) {
        val hash = HashUtils.hash(file);
        return create(new Checksum(hash, file), type, hash);        
    }
}

