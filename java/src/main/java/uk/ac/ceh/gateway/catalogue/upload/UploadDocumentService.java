package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.hibernate.validator.internal.metadata.aggregated.rule.ParallelMethodsMustNotDefineGroupConversionForCascadedReturnValue;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.util.FileListUtils;
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
        ZipFileUtils.archive(directory, unarchived -> {
            updateFromHashFiles(unarchived, documents);
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
                val uploadFile = UploadFileBuilder.create(directory, checksum, UploadType.DOCUMENTS, hash);
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

    public void add(CatalogueUser user, UploadDocument document, String filename, InputStream in) {
        document.validate();
        val documents = document.getUploadFiles().get("documents");
        val directory = new File(documents.getPath());

        ZipFileUtils.archive(directory, unarchived -> {
            val file = new File(directory, filename);
            saveInputStream(unarchived, file, in);
            if (isZipFile(file)) {
                ZipFileUtils.archiveZip(file, unarchivedZip -> {
                    val filenames = FileListUtils.absolutePathsTree(unarchivedZip);
                    for (val innerFilename : filenames) {
                        val zippedUploadFile = UploadFileBuilder.create(directory, new File(innerFilename), UploadType.DOCUMENTS);
                        documents.getDocuments().put(zippedUploadFile.getPath(), zippedUploadFile);
                    }
                });
            }
            val uploadFile = UploadFileBuilder.create(directory, file, UploadType.DOCUMENTS);
            documents.getDocuments().put(uploadFile.getPath(), uploadFile);
            saveUploadDocument(user, document, String.format("adding file: %s", file.getPath()));
        });
    }

    @SneakyThrows
    private void saveInputStream(File directory, File file, InputStream in) {
        FileUtils.copyInputStreamToFile(in, file);
    }

    public void move() {
    }

    public void delete(CatalogueUser user, UploadDocument document, String filename) {
        document.validate();
        val documents = document.getUploadFiles().get("documents");
        UploadFile uploadFile = documents.getDocuments().get(filename);
        if (uploadFile == null) uploadFile = documents.getInvalid().get(filename);

        if (uploadFile != null) {
            val directory = new File(documents.getPath());
            ZipFileUtils.archive(directory, unarchived -> {
                forceDelete(filename);
            });
            documents.getDocuments().remove(filename);
            document.validate();
            saveUploadDocument(user, document, String.format("removing file: %s", uploadFile.getPath()));
        }
    }

    @SneakyThrows
    private void forceDelete(String filename) {
        val file = new File(filename);
        if (file.exists()) FileUtils.forceDelete(file);
    }

    public void zip() {
    }

    public void unzip() {
    }

    public void acceptInvalid(CatalogueUser user, UploadDocument document, String name, String filename) {
        document.validate();
        val uploadFiles = document.getUploadFiles().get(name);
        val uploadFile = uploadFiles.getInvalid().get(filename);
        uploadFile.setType(UploadType.DOCUMENTS);
        uploadFiles.getInvalid().remove(filename);
        uploadFiles.getDocuments().put(filename, uploadFile);
        saveUploadDocument(user, document, String.format("accepting invalid file %s", filename));
    }

    @SneakyThrows
    private void saveUploadDocument(CatalogueUser user, UploadDocument document, String message) {
        for (val uploadFiles : document.getUploadFiles().values()) {
            uploadFiles.setInvalid(Maps.newHashMap());
        }
        document.setMetadata(documentRepository.read(document.getId()).getMetadata());
        documentRepository.save(user, document, message);
    }
}