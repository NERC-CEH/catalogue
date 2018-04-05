package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import org.apache.commons.io.FilenameUtils;

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.util.HashUtils;

public class UploadFileBuilder {

    @SneakyThrows
    public static void update (String guid, UploadFile uploadFile, File directory, String physicalLocation, File file, UploadType type) {
        String hash = "";
        if (file.exists()) hash = HashUtils.hash(file);
        update(guid, uploadFile, directory, physicalLocation, file, type, hash);
    }

    @SneakyThrows
    public static void update (String guid, UploadFile uploadFile, File directory, String physicalLocation, File file, UploadType type, String hash) {
        String name = extractName(directory, file);

        uploadFile.setPath(file.getAbsolutePath());
        uploadFile.setPhysicalLocation(FilenameUtils.separatorsToWindows(String.format("%s/%s/%s", physicalLocation, guid, name)));
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
    }

    @SneakyThrows
    public static UploadFile create(String guid, File directory, String physicalLocation, Checksum checksum, UploadType type, String hash) {
        val file = checksum.getFile();
        val uploadFile = new UploadFile();
        update(guid, uploadFile, directory, physicalLocation, file, type, hash);
        return uploadFile;
    }

    public static UploadFile create(String guid, File directory, String physicalLocation, File file, UploadType type, String hash) {
        return create(guid, directory, physicalLocation, new Checksum(hash, file), type, hash);        
    }

    public static UploadFile create(String guid, File directory, String physicalLocation, File file, UploadType type) {
        val hash = HashUtils.hash(file);
        return create(guid, directory, physicalLocation, new Checksum(hash, file), type, hash);        
    }

    private static String extractName(File directory, File file) {
        String name = file.getAbsolutePath().replace(directory.getAbsolutePath(), "");
        if (name.charAt(0) == '/') name = name.replaceFirst("/", "");
        return Lists.newArrayList(name.split("/"))
            .stream()
            .filter(chunk -> !chunk.equals(""))
            .map(chunk -> {
                if (chunk.contains("_extracted-")) return chunk.replace("_extracted-", "") + ".zip";
                return chunk;
            }).collect(Collectors.joining("/"));
    }
}
