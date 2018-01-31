package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.lingala.zip4j.core.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.util.HashUtils;

@AllArgsConstructor
public class UploadDocumentService {

    private static final Pattern regex = Pattern.compile("([a-f0-9]{32})\\s*\\*?(.*)");
    private final DocumentRepository documentRepository;
    private final Map<String, File> folders;

    @SneakyThrows
    public UploadDocument create(CatalogueUser user, GeminiDocument geminiDocument) {
        val guid = geminiDocument.getId();
        Map<String, UploadFiles> uploadFiles = Maps.newHashMap();
        folders.entrySet().stream().forEach(entry -> {
            val key = entry.getKey();
            val value = entry.getValue();
            val directory = new File(value, guid);
            val documents = new HashMap<String, UploadFile>();
            val invalid = new HashMap<String, UploadFile>();
            createDocumentsAndInvalid(directory, documents, invalid);
            boolean isZipped = isZipped(directory);
            val uploadFilesValue = new UploadFiles(directory.getAbsolutePath(), isZipped);
            uploadFilesValue.setDocuments(documents);
            uploadFilesValue.setInvalid(invalid);
            uploadFiles.put(key, uploadFilesValue);
        });
        val uploadDocument = new UploadDocument(guid, uploadFiles);
        uploadDocument.setType("dataset");
        uploadDocument.setTitle(geminiDocument.getTitle());

        documentRepository.saveNew(user, uploadDocument, "eidc", "creating new upload document");
        geminiDocument.setUploadId(uploadDocument.getId());
        documentRepository.save(user, geminiDocument, String.format("updating upload id: %s", uploadDocument.getId()));
        System.out.println(String.format("uploadId %s", geminiDocument.getUploadId()));
        return uploadDocument;
    }

    private void createDocumentsAndInvalid(File directory, Map<String, UploadFile> documents, Map<String, UploadFile> invalid) {
        extractAll(directory);
        updateFromHashFiles(directory, documents, invalid);
        updateWithUknownFiles(directory, documents, invalid);
        cleanExtracted(directory);
    }

    private void extractAll(File directory) {
        if (directory.exists()) {
            Lists.newArrayList(directory.listFiles())
                .stream()
                .filter(file -> isZipFile(file))
                .forEach(file -> extract(file));
        }
    }

    @SneakyThrows
    private void extract(File file) {
        val zipFile = new ZipFile(file);
        val extracted = new File(file.getParentFile(), String.format("_extracted-%s", file.getName().replace(".zip", "")));
        zipFile.extractAll(extracted.getAbsolutePath());
        extractAll(extracted);
    }

    private boolean isZipFile(File file) {
        return file.getPath().endsWith(".zip");
    }

    private void updateFromHashFiles (File directory, Map<String, UploadFile> documents, Map<String, UploadFile> invalid) {
        val checksums = getChecksums(directory, null);
        checksums.stream().forEach(checksum -> {
            val expectedHash = checksum.getHash();
            val file = checksum.getFile();
            val exists = file.exists();
            if (!exists) {
                val uploadFile = createUploadFile(checksum, UploadType.MISSING_FILE, expectedHash);
                invalid.put(uploadFile.getPath(), uploadFile);
            } else {
                val actualHash = HashUtils.hash(file);
                if (!expectedHash.equals(actualHash)) {
                    val uploadFile = createUploadFile(checksum, UploadType.INVALID_HASH, actualHash);
                    invalid.put(uploadFile.getPath(), uploadFile);
                } else {
                    val uploadFile = createUploadFile(checksum, UploadType.DOCUMENTS, actualHash);
                    documents.put(uploadFile.getPath(), uploadFile);
                }
            }
        });
    }

    @SneakyThrows   
    private List<Checksum> getChecksums(File directory, List<Checksum> foundChecksums) {
        if (foundChecksums == null) foundChecksums = Lists.newArrayList();
        if (directory.exists()) {
            val files = directory.listFiles();
            for(val file : files) {
                if (file.isDirectory()) foundChecksums = getChecksums(file, foundChecksums);
                else if (isHashFile(file)) {
                    val lines = FileUtils.readLines(file, Charset.defaultCharset());
                    List<Checksum> checksums = lines.stream()
                        .filter(line -> { return regex.matcher(line).matches(); })
                        .map(line -> { return toChecksum(line, file); })
                        .collect(Collectors.toList());
                        foundChecksums.addAll(checksums);
                }
            }
        }
        return foundChecksums;
    }

    private boolean isHashFile(File file) {
        return file.getPath().endsWith(".hash");
    }

    private Checksum toChecksum(String line, File file) {
        val matches = regex.matcher(line);
        matches.matches();
        val hash = matches.group(1);
        val name = matches.group(2);
        val hashFile = new File(file.getParentFile().getAbsolutePath(), name);
        return new Checksum(hash, hashFile);
    }

    private void updateWithUknownFiles (File directory, Map<String, UploadFile> documents, Map<String, UploadFile> invalid) {
        val filenames = treeOfFileNames(directory, null);
        val keys = new ArrayList<String>();
        keys.addAll(documents.keySet());
        keys.addAll(invalid.keySet());

        filenames.stream()
        .filter(filename -> { return !keys.contains(filename) && !filename.endsWith(".hash"); })
        .forEach(filename -> {
            val file = new File(filename);
            val hash = HashUtils.hash(file);
            val uploadFile = createUploadFile(new Checksum(hash, file), UploadType.UNKNOWN_FILE, hash);
            invalid.put(filename, uploadFile);
        });
    }

    @SneakyThrows
    private UploadFile createUploadFile (Checksum checksum, UploadType type, String hash) {
        val file = checksum.getFile();
        val uploadFile = new UploadFile();
        val name = file.getName();
        uploadFile.setPath(file.getAbsolutePath());
        uploadFile.setType(type);
        uploadFile.setHash(hash);
        uploadFile.setId(name.replaceAll("[^\\w?]","-"));
        uploadFile.setName(name);
        uploadFile.setEncoding("utf-8");

        if (file.exists()) {
            uploadFile.setFormat(FilenameUtils.getExtension(name));
            uploadFile.setMediatype(Files.probeContentType(file.toPath()));
            uploadFile.setBytes(file.length());
        }

        return uploadFile;
    }

    private void cleanExtracted(File directory) {
        val files = treeOfFileNames(directory, null);
        files.stream()
            .filter(filename -> { return filename.contains("_extracted-"); })
            .forEach(filename -> removeFile(filename));
    }

    @SneakyThrows
    private void removeFile (String filename) {
        val file = new File(filename);
        if (file.exists()) {
            val parent = file.getParentFile();
            FileUtils.forceDelete(parent);
        }
    }

    private List<String> treeOfFileNames (File directory, List<String> filenames) {
        if (directory.exists()) {
            val files = FileUtils.listFiles(
                directory,
                FileFilterUtils.trueFileFilter(),
                FileFilterUtils.trueFileFilter());
            return files.stream()
                .map(file -> { return file.getAbsolutePath(); })
                .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    private boolean isZipped(File directory) {
        if (!directory.exists()) return false;
        val files = Lists.newArrayList(directory.listFiles());
        if (files.size() != 2) return false;
        files.removeIf(file -> { return isZipFile(file) || isHashFile(file); });
        return files.size() == 0;
    }

    public void add() {
    }

    public void move() {
    }

    public void delete() {
    }

    public void zip() {
    }

    public void unzip() {
    }

    public void acceptInvalid() {
    }
}