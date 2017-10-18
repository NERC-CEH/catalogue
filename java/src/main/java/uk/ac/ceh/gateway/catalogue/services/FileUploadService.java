package uk.ac.ceh.gateway.catalogue.services;

import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.model.FileChecksum;

import java.io.*;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
@AllArgsConstructor
public class FileUploadService {
    private static final String CHECKSUM_NAME = "checksums.hash";
    @Qualifier("dropbox")
    private final File dropbox;

    public FileChecksum uploadData(InputStream input, String guid, String filename) throws IOException, NoSuchAlgorithmException {
        byte[] md5 = saveFile(input, getDepositFile(guid, filename, true));
        return appendChecksum(guid, new FileChecksum(md5, filename));
    }
    
    public List<FileChecksum> getChecksums(String guid) throws IOException {
        return adjustChecksum(guid, (checksums) -> {
            if(!checksums.exists()) {
                return Collections.emptyList();
            }
            try (Stream<String> lines = Files.lines(checksums.toPath())) {
                return lines.filter((line) -> !line.startsWith("#"))
                            .map(FileChecksum::fromLine)
                            .collect(Collectors.toList());
            }
        });
    }

    public void deleteFile(String guid, final String filename) throws IOException {
        File toDelete = getDepositFile(guid, filename, false);
        Files.delete(toDelete.toPath());
        adjustChecksum(guid, (checksums) -> {
            try (Stream<String> lines = Files.lines(checksums.toPath())) {
                List<String> replaced = lines
                        .filter((l) -> l.startsWith("#") || !l.endsWith(String.format(" *%s", filename)))
                        .collect(Collectors.toList());
                Files.write(checksums.toPath(), replaced);
            }
            return null;
        });
    }

    public List<String> getFiles(String guid) throws IOException {
        File directory = getDepositDirectory(guid, false);
        if(!directory.isDirectory()) {
            throw new IllegalArgumentException("The specified guid does not exist");
        }
        return Arrays.asList(directory.list((d, f) -> !f.equals(CHECKSUM_NAME)));
    }

    protected byte[] saveFile(InputStream input, File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        try(InputStream in = new DigestInputStream(input, md5);
        OutputStream out = Files.newOutputStream(file.toPath(), CREATE_NEW)) {
            IOUtils.copyLarge(in, out);
        }
        return md5.digest();
    }

    protected FileChecksum appendChecksum(String guid, FileChecksum checksum) throws IOException {        
        return adjustChecksum(guid, (checksums) -> {
            boolean exists = checksums.exists();
            try (PrintWriter out = new PrintWriter(new FileWriter(checksums,true))) {
                if(!exists) {
                    out.println("# made using the EIDC data ingestion utility");
                    out.println("#");
                }
                out.println(String.format("%s *%s", checksum.getMD5Hash(), checksum.getFilename()));
            }
            return checksum;
        });
    }

    public File getDepositFile(String guid, String filename) throws IOException {
        return getDepositFile(guid, filename, false);
    }

    private File getDepositFile(String guid, String filename, boolean create) throws IOException {
        if(!filename.equals(CHECKSUM_NAME)) {
            return getDepositLocation(guid, filename, create);
        }
        else {
            throw new IllegalArgumentException(CHECKSUM_NAME + " is a reserved file name");
        }
    }

    private File getDepositLocation(String guid, String file, boolean create) throws IOException {
        File directory = getDepositDirectory(guid, create);
        File toReturn = new File(directory, file).getCanonicalFile();
        
        if(!toReturn.getParentFile().equals(directory)) {
            throw new IllegalArgumentException("The specified file can not contain subdirectories");
        }
        return toReturn;
    }

    private File getDepositDirectory(String guid, boolean create) throws IOException {
        File toReturn = new File(dropbox, guid).getCanonicalFile();
        
        if(!toReturn.getParentFile().equals(dropbox)) {
            throw new IllegalArgumentException("Guids can not point contain subdirectories");
        }
        
        if(create) toReturn.mkdirs();
        return toReturn;
    }
    
    private <R> R adjustChecksum(String guid, ChecksumOperation<R> op) throws IOException {
        File checksums = getDepositLocation(guid, CHECKSUM_NAME, true);
        synchronized(checksums.getCanonicalPath().intern()) {
            return op.run(checksums);
        }
    }
    
    @FunctionalInterface
    private interface ChecksumOperation<R> {
        R run(File checksums) throws IOException;
    }
}