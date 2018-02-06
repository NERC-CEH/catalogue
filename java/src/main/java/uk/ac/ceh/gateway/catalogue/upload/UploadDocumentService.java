package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;

import ch.qos.logback.core.net.SyslogOutputStream;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
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
            boolean zipped = isZipped(directory);
            val uploadFilesValue = new UploadFiles(directory.getAbsolutePath());
            uploadFilesValue.setZipped(zipped);
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
        val files = FileListUtils.relativePathsTree(directory);
        return files.contains(directory.getName() + ".zip");
    }

    public void add(CatalogueUser user, UploadDocument document, String filename, InputStream in) {
        document.validate();
        val documents = document.getUploadFiles().get("documents");
        val directory = new File(documents.getPath());

        ZipFileUtils.archive(directory, unarchived -> {
            val file = new File(directory, filename);
            saveInputStream(file, in);
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
    private void saveInputStream(File file, InputStream in) {
        FileUtils.copyInputStreamToFile(in, file);
    }

    @SneakyThrows
    public void move(CatalogueUser user, UploadDocument document, String from, String to, String filename) {
        document.validate();
        val fromUploadFiles = document.getUploadFiles().get(from);
        val fromDirectory = new File(fromUploadFiles.getPath());
        
        val uploadFile = fromUploadFiles.getDocuments().get(filename);
        if (uploadFile != null) {
            val fromPath = uploadFile.getPath();

            val toUploadFiles = document.getUploadFiles().get(to);
            val toDirectory = new File(toUploadFiles.getPath());

            ZipFileUtils.archive(fromDirectory, unarchivedFrom -> {
                ZipFileUtils.archive(toDirectory, unarchivedTo -> {
                    val fromFile = new File(uploadFile.getPath());
                    val toFile = new File(
                        uploadFile.getPath()
                            .replace(fromDirectory.getAbsolutePath(), toDirectory.getAbsolutePath())
                    );

                    if (isZipFile(fromFile)) {
                        ZipFileUtils.archiveZip(fromFile, unarchivedZip -> {
                            val filenames = FileListUtils.absolutePathsTree(unarchivedZip);
                            for (val innerFilename : filenames) {
                                fromUploadFiles.getDocuments().remove(innerFilename);
                            }
                        });
                    }

                    moveFile(fromFile, toFile);

                    if (isZipFile(toFile)) {
                        ZipFileUtils.archiveZip(toFile, unarchivedZip -> {
                            val filenames = FileListUtils.absolutePathsTree(unarchivedZip);
                            for (val innerFilename : filenames) {
                                val innerUploadFile = UploadFileBuilder.create(toDirectory, new File(innerFilename), UploadType.DOCUMENTS);
                                toUploadFiles.getDocuments().put(innerFilename, innerUploadFile);
                            }
                        });
                    }

                    UploadFileBuilder.update(uploadFile, toDirectory, toFile, UploadType.DOCUMENTS);
                });
            });

            fromUploadFiles.getDocuments().remove(filename);
            toUploadFiles.getDocuments().put(uploadFile.getPath(), uploadFile);

            saveUploadDocument(user, document, String.format("moving file from: %s, to: %s", fromPath, uploadFile.getPath()));
        }
    }

    @SneakyThrows
    private void moveFile(File from, File to) {
        forceDelete(to.getAbsolutePath());
        FileUtils.moveFile(from, to);
    }

    public void delete(CatalogueUser user, UploadDocument document, String name, String filename) {
        document.validate();
        val documents = document.getUploadFiles().get(name);
        UploadFile uploadFile = documents.getDocuments().get(filename);
        if (uploadFile == null) uploadFile = documents.getInvalid().get(filename);

        if (uploadFile != null) {
            val directory = new File(documents.getPath());
            ZipFileUtils.archive(directory, unarchived -> {
                forceDelete(filename);
            });
            documents.getDocuments().remove(filename);
            saveUploadDocument(user, document, String.format("removing file: %s", uploadFile.getPath()));
        }
    }

    @SneakyThrows
    private void forceDelete(String filename) {
        val file = new File(filename);
        if (file.exists()) FileUtils.forceDelete(file);
    }

    public void moveToDatastore(CatalogueUser user, UploadDocument document) {
        document.validate();
        val fromUploadFiles = document.getUploadFiles().get("documents");
        val fromDirectory = new File(fromUploadFiles.getPath());

        val toUploadFiles = document.getUploadFiles().get("datastore");
        val toDirectory = new File(toUploadFiles.getPath());

        ZipFileUtils.archive(fromDirectory, unarchivedFrom -> {
            ZipFileUtils.archive(toDirectory, unarchivedTo -> {
                for (val fromEntry : fromUploadFiles.getDocuments().entrySet()) {
                    val uploadFile = fromEntry.getValue();

                    val fromFile = new File(uploadFile.getPath());
                    val file = new File(
                        uploadFile.getPath()
                            .replace(fromDirectory.getAbsolutePath(), toDirectory.getAbsolutePath())
                    );
                    moveFile(fromFile, file);
                    UploadFileBuilder.update(uploadFile, toDirectory, file, UploadType.DOCUMENTS);
                    toUploadFiles.getDocuments().put(file.getPath(), uploadFile);
                }
            });
        });

        fromUploadFiles.setDocuments(Maps.newHashMap());
        saveUploadDocument(user, document, "moving all files from documents to datastore");
    }

    @SneakyThrows
    public void zip(CatalogueUser user, UploadDocument document) {
        document.validate();
        val uploadFiles = document.getUploadFiles().get("datastore");

        val directory = new File(uploadFiles.getPath());
        if (!isZipped(directory)) {
            val zipname = String.format("%s.zip", document.getParentId());
            val zipFile = new File(directory, zipname);
            val zip = new ZipFile(zipFile.getAbsolutePath());
            val parameters = new ZipParameters();
            val filesnames = FileListUtils.absolutePathsTree(directory);
            for (val filename : filesnames) {
                if (!filename.equals(zipname)) {
                    addToZip(new File(filename), zip, parameters);
                }
            }

            uploadFiles.setDocuments(Maps.newHashMap());
            
            ZipFileUtils.archiveZip(zipFile, unarchivedZip -> {
                val filenames = FileListUtils.absolutePathsTree(unarchivedZip);
                for (val innerFilename : filenames) {
                    if (!innerFilename.endsWith(".hash")) {
                        val zippedUploadFile = UploadFileBuilder.create(directory, new File(innerFilename), UploadType.DOCUMENTS);
                        uploadFiles.getDocuments().put(zippedUploadFile.getPath(), zippedUploadFile);
                    }
                }
            });

            val uploadFile = UploadFileBuilder.create(directory, zipFile, UploadType.DOCUMENTS);
            uploadFiles.getDocuments().put(uploadFile.getPath(), uploadFile);
        }

        uploadFiles.setZipped(true);
        saveUploadDocument(user, document, "zipping datastore");
    }

    @SneakyThrows
    private void addToZip(File file, ZipFile zipFile, ZipParameters parameters) {
        if (file.isDirectory()) zipFile.addFolder(file, parameters);
        else zipFile.addFile(file, parameters);
        FileUtils.forceDelete(file);
    }

    @SneakyThrows
    public void unzip(CatalogueUser user, UploadDocument document) {
        document.validate();
        val uploadFiles = document.getUploadFiles().get("datastore");

        val directory = new File(uploadFiles.getPath());
        if (isZipped(directory)) {
            val zipname = String.format("%s.zip", document.getParentId());
            val zipFile = new File(directory, zipname);
            val zip = new ZipFile(zipFile.getAbsolutePath());
            zip.extractAll(directory.getAbsolutePath());
            FileUtils.forceDelete(zipFile);

            uploadFiles.setDocuments(Maps.newHashMap());

            ZipFileUtils.archive(directory, unarchived -> {
                val filenames = FileListUtils.absolutePathsTree(unarchived);
                for (val innerFilename : filenames) {
                    if (!innerFilename.endsWith(".hash")) {
                        val zippedUploadFile = UploadFileBuilder.create(directory, new File(innerFilename), UploadType.DOCUMENTS);
                        uploadFiles.getDocuments().put(zippedUploadFile.getPath(), zippedUploadFile);
                    }
                }
            });

        }

        uploadFiles.setZipped(false);
        saveUploadDocument(user, document, "unzipping datastore");
    }

    public void acceptInvalid(CatalogueUser user, UploadDocument document, String name, String filename) {
        document.validate();
        val uploadFiles = document.getUploadFiles().get(name);
        val directory = new File(uploadFiles.getPath());
        val uploadFile = uploadFiles.getInvalid().get(filename);
        uploadFiles.getInvalid().remove(filename);

        ZipFileUtils.archive(directory, unarchived -> {
            val filenames = FileListUtils.absolutePathsTree(unarchived);
            for (val innerFilename : filenames) {
                if (innerFilename.equals(filename)) {
                    UploadFileBuilder.update(uploadFile, directory, new File(innerFilename), UploadType.DOCUMENTS);
                }
            }
        });

        uploadFiles.getDocuments().put(filename, uploadFile);
        saveUploadDocument(user, document, String.format("accepting invalid file %s", filename));
    }

    @SneakyThrows
    private void saveUploadDocument(CatalogueUser user, UploadDocument document, String message) {
        for (val uploadFiles : document.getUploadFiles().values()) {
            uploadFiles.setInvalid(Maps.newHashMap());
        }
        val found = documentRepository.read(document.getId());
        document.setMetadata(found.getMetadata());
        documentRepository.save(user, document, message);
    }
}