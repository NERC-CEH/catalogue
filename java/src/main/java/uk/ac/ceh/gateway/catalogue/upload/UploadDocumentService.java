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
import uk.ac.ceh.gateway.catalogue.util.FileListUtils;
import uk.ac.ceh.gateway.catalogue.util.HashUtils;
import uk.ac.ceh.gateway.catalogue.util.ZipFileUtils;

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
            createDocuments(directory, documents);
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
        return uploadDocument;
    }

    private void createDocuments(File directory, Map<String, UploadFile> documents) {
        ZipFileUtils.archive(directory, folder -> {
            updateFromHashFiles(directory, documents);
        });
    }

    private boolean isZipFile(File file) {
        return file.getPath().endsWith(".zip");
    }

    private void updateFromHashFiles (File directory, Map<String, UploadFile> documents) {
        val checksums = getChecksums(directory, null);
        checksums.stream().forEach(checksum -> {
            val hash = checksum.getHash();
            val file = checksum.getFile();
            val exists = file.exists();
            if (exists) {
                val uploadFile = UploadFileBuilder.create(checksum, UploadType.DOCUMENTS, hash);
                documents.put(uploadFile.getPath(), uploadFile);
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

    @SneakyThrows
    private void removeFile (String filename) {
        val file = new File(filename);
        if (file.exists()) {
            val parent = file.getParentFile();
            FileUtils.forceDelete(parent);
        }
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