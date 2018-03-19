package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.util.FileListUtils;
import uk.ac.ceh.gateway.catalogue.util.FilenameContainsFilterUtils;
import uk.ac.ceh.gateway.catalogue.util.ZipFileUtils;

@AllArgsConstructor
public class UploadDocumentService {

    private static final Pattern regex = Pattern.compile("([a-f0-9]{32})\\s*\\*?(.*)");
    private final DocumentRepository documentRepository;
    private final Map<String, File> folders;
    private final Map<String, String> physicalLocations;

    @SneakyThrows
    public UploadDocument create(CatalogueUser user, GeminiDocument geminiDocument) {
        val guid = geminiDocument.getId();
        Map<String, UploadFiles> uploadFiles = Maps.newHashMap();
        val uploadDocument = new UploadDocument(guid, uploadFiles);
        uploadDocument.setType("dataResource");
        uploadDocument.setTitle(geminiDocument.getTitle());
        documentRepository.saveNew(user, uploadDocument, "eidc", "creating new upload document");

        folders.entrySet().stream().forEach(entry -> {
            val key = entry.getKey();
            val value = entry.getValue();
            val physicalLocation = physicalLocations.get(key);
            val directory = new File(value, guid);
            val documents = new HashMap<String, UploadFile>();
            val invalid = new HashMap<String, UploadFile>();
            createDocuments(uploadDocument.getId(), directory, documents, physicalLocation);
            boolean zipped = isZipped(directory);
            val uploadFilesValue = new UploadFiles(directory.getAbsolutePath(), physicalLocation);
            uploadFilesValue.setZipped(zipped);
            uploadFilesValue.setDocuments(documents);
            uploadFilesValue.setInvalid(invalid);
            uploadFiles.put(key, uploadFilesValue);
        });

        saveUploadDocument(user, uploadDocument, "updating upload document");
        geminiDocument.setUploadId(uploadDocument.getId());
        documentRepository.save(user, geminiDocument, String.format("updating upload id: %s", uploadDocument.getId()));
        uploadDocument.validate();
        return uploadDocument;
    }

    private void createDocuments(String guid, File directory, Map<String, UploadFile> documents, String physicalLocation) {
        ZipFileUtils.archive(directory, unarchived -> {
            updateFromHashFiles(guid, unarchived, documents, physicalLocation);
        });
    }

    private boolean isZipFile(File file) {
        return file.getPath().endsWith(".zip");
    }

    private void updateFromHashFiles (String guid, File directory, Map<String, UploadFile> documents, String physicalLocation) {
        val checksums = getChecksums(directory, null);
        checksums.stream().forEach(checksum -> {
            val hash = checksum.getHash();
            val file = checksum.getFile();
            val exists = file.exists();
            if (exists) {
                val uploadFile = UploadFileBuilder.create(guid, directory, physicalLocation, checksum, UploadType.DOCUMENTS, hash);
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

    public void add(CatalogueUser user, UploadDocument uploadDocument, String filename, InputStream in) {
        uploadDocument.validate();
        val documents = uploadDocument.getUploadFiles().get("documents");
        val directory = new File(documents.getPath());
        val physicalLocation = documents.getPhysicalLocation();

        ZipFileUtils.archive(directory, unarchived -> {
            val file = new File(directory, filename);
            saveInputStream(file, in);
            if (isZipFile(file)) {
                ZipFileUtils.archiveZip(file, unarchivedZip -> {
                    val filenames = FileListUtils.absolutePathsTree(unarchivedZip, FilenameContainsFilterUtils.doesNotContain(".hash"), FilenameContainsFilterUtils.doesNotContain(".hash"));
                    for (val innerFilename : filenames) {
                        val zippedUploadFile = UploadFileBuilder.create(uploadDocument.getId(), directory, physicalLocation, new File(innerFilename), UploadType.DOCUMENTS);
                        documents.getDocuments().put(zippedUploadFile.getPath(), zippedUploadFile);
                    }
                });
            }
            val uploadFile = UploadFileBuilder.create(uploadDocument.getId(), directory, physicalLocation, file, UploadType.DOCUMENTS);
            documents.getDocuments().put(uploadFile.getPath(), uploadFile);
            saveUploadDocument(user, uploadDocument, String.format("adding file: %s", file.getPath()));
        });
        uploadDocument.validate();
    }

    @SneakyThrows
    private void saveInputStream(File file, InputStream in) {
        FileUtils.copyInputStreamToFile(in, file);
    }

    @SneakyThrows
    public void move(CatalogueUser user, UploadDocument uploadDocument, String from, String to, String fromFilename) {
        uploadDocument.validate();
        val fromUploadFiles = uploadDocument.getUploadFiles().get(from);
        val fromDirectory = new File(fromUploadFiles.getPath());
        val fromPhysicalLocation = fromUploadFiles.getPhysicalLocation();
        
        val isZipped = fromFilename.endsWith("/" + uploadDocument.getParentId() + ".zip");

        val uploadFile = fromUploadFiles.getDocuments().get(fromFilename);
        if (!isZipped && uploadFile != null) {
            val toUploadFiles = uploadDocument.getUploadFiles().get(to);
            val toDirectory = new File(toUploadFiles.getPath());
            val toPhysicalLocation = toUploadFiles.getPhysicalLocation();

            val hasInvalid = fromUploadFiles.getInvalid().size() > 0 || toUploadFiles.getInvalid().size() > 0;
            if (!hasInvalid) {
                val extractedZipname = "_extracted-" + uploadDocument.getParentId();
                File toFileDirectory = toDirectory;
                if (toUploadFiles.isZipped()) toFileDirectory = new File(toDirectory, extractedZipname);

                String toFilename = fromFilename.replace(fromDirectory.getAbsolutePath(), toFileDirectory.getAbsolutePath());
                if (!toUploadFiles.isZipped()) toFilename = toFilename.replace(extractedZipname, "").replace("//", "/");
                val fromCompressList = ZipFileUtils.filenameToCompessList(fromFilename);
                val toCompressList = ZipFileUtils.filenameToCompessList(toFilename);

                val fromFile = new File(fromFilename);
                val toFile = new File(toFilename);

                ZipFileUtils.archive(fromCompressList, fromDirectory, unarchivedFrom -> {
                    ZipFileUtils.archive(toCompressList, toDirectory, unarchivedTo -> {
                        if (isZipFile(fromFile)) {
                            ZipFileUtils.archiveZip(fromCompressList, fromFile, unarchivedZip -> {
                                val filenames = FileListUtils.absolutePathsTree(unarchivedZip, FilenameContainsFilterUtils.doesNotContain(".hash"), FilenameContainsFilterUtils.doesNotContain(".hash"));
                                for (val innerFilename : filenames) {
                                    fromUploadFiles.getDocuments().remove(innerFilename);
                                }
                            });
                        }

                        moveFile(fromFile, toFile);

                        if (isZipFile(toFile)) {
                            ZipFileUtils.archiveZip(toCompressList, toFile, unarchivedZip -> {
                                val filenames = FileListUtils.absolutePathsTree(unarchivedZip, FilenameContainsFilterUtils.doesNotContain(".hash"), FilenameContainsFilterUtils.doesNotContain(".hash"));
                                for (val innerFilename : filenames) {
                                    val innerUploadFile = UploadFileBuilder.create(uploadDocument.getId(), toDirectory, toPhysicalLocation, new File(innerFilename), UploadType.DOCUMENTS);
                                    toUploadFiles.getDocuments().put(innerFilename, innerUploadFile);
                                }
                            });
                        }

                        UploadFileBuilder.update(uploadDocument.getId(), uploadFile, toDirectory, toPhysicalLocation, toFile, UploadType.DOCUMENTS);
                    });
                });

                fromUploadFiles.getDocuments().remove(fromFilename);

                toUploadFiles.getDocuments().put(toFilename, uploadFile);

                updateZipHashes(uploadDocument.getId(), fromUploadFiles, fromCompressList, fromDirectory, fromPhysicalLocation);
                updateZipHashes(uploadDocument.getId(),toUploadFiles, toCompressList, toDirectory, toPhysicalLocation);
                saveUploadDocument(user, uploadDocument, String.format("moving file from: %s, to: %s", fromFilename, toFilename));
            }
            removeEmptyFolders(fromDirectory);
            // uploadDocument.validate();
        }
    }

    @SneakyThrows
    private void moveFile(File from, File to) {
        forceDelete(to.getAbsolutePath());
        FileUtils.moveFile(from, to);
    }

    public void delete(CatalogueUser user, UploadDocument uploadDocument, String name, String filename) {
        uploadDocument.validate();
        val uploadFiles = uploadDocument.getUploadFiles().get(name);
        UploadFile uploadFile = uploadFiles.getDocuments().get(filename);
        if (uploadFile == null) uploadFile = uploadFiles.getInvalid().get(filename);

        if (uploadFile != null) {
            val directory = new File(uploadFiles.getPath());
            val phyisicalLocation = uploadFiles.getPhysicalLocation();
            val compressList = ZipFileUtils.filenameToCompessList(filename);
            ZipFileUtils.archive(compressList, directory, unarchived -> {
                val file = new File(filename);
                if (file.exists() && isZipFile(file)) {
                    ZipFileUtils.archiveZip(file, unarchivedZip -> {
                        val filenames = FileListUtils.absolutePathsTree(unarchivedZip, FilenameContainsFilterUtils.doesNotContain(".hash"), FilenameContainsFilterUtils.doesNotContain(".hash"));
                        for (val innerFilename : filenames) {
                            uploadFiles.getDocuments().remove(innerFilename);
                        }
                    });
                }
                forceDelete(filename);
            });

            updateZipHashes(uploadDocument.getId(), uploadFiles, compressList, directory, phyisicalLocation);

            uploadFiles.getDocuments().remove(filename);
            uploadFiles.getInvalid().remove(filename);
            saveUploadDocument(user, uploadDocument, String.format("removing file: %s", uploadFile.getPath()));
            removeEmptyFolders(directory);
        }
        uploadDocument.validate();
    }

    @SneakyThrows
    private void forceDelete(String filename) {
        val file = new File(filename);
        if (file.exists()) FileUtils.forceDelete(file);
    }

    public void moveToDatastore(CatalogueUser user, UploadDocument uploadDocument) {
        uploadDocument.validate();
        val fromUploadFiles = uploadDocument.getUploadFiles().get("documents");
        val fromDirectory = new File(fromUploadFiles.getPath());

        val toUploadFiles = uploadDocument.getUploadFiles().get("datastore");
        val toDirectory = new File(toUploadFiles.getPath());
        val toPhysicalLocation = toUploadFiles.getPhysicalLocation();

        val hasInvalid = fromUploadFiles.getInvalid().size() > 0 || toUploadFiles.getInvalid().size() > 0;
        if (!hasInvalid) {
            val extractedZipname = "_extracted-" + uploadDocument.getParentId();
            List<String> tempCompresList = Lists.newArrayList();
            if (toUploadFiles.isZipped()) tempCompresList = ZipFileUtils.filenameToCompessList(new File(toDirectory, extractedZipname).getAbsolutePath());
            val toCompresList = tempCompresList;

            ZipFileUtils.archive(fromDirectory, unarchivedFrom -> {
                ZipFileUtils.archive(toCompresList, toDirectory, unarchivedTo -> {
                    for (val fromEntry : fromUploadFiles.getDocuments().entrySet()) {
                        val uploadFile = fromEntry.getValue();

                        val fromFilename = uploadFile.getPath();
                        val fromFile = new File(fromFilename);

                        File toFileDirectory = toDirectory;
                        if (toUploadFiles.isZipped()) toFileDirectory = new File(toDirectory, extractedZipname);

                        String toFilename = fromFilename.replace(fromDirectory.getAbsolutePath(), toFileDirectory.getAbsolutePath());
                        if (!toUploadFiles.isZipped()) toFilename = toFilename.replace(extractedZipname, "").replace("//", "/");
                        val toFile = new File(toFilename);

                        moveFile(fromFile, toFile);
                        UploadFileBuilder.update(uploadDocument.getId(), uploadFile, toDirectory, toPhysicalLocation, toFile, UploadType.DOCUMENTS);
                        toUploadFiles.getDocuments().put(toFile.getPath(), uploadFile);
                    }
                });
            });

            fromUploadFiles.setDocuments(Maps.newHashMap());

            updateZipHashes(uploadDocument.getId(), toUploadFiles, toCompresList, toDirectory, toPhysicalLocation);

            removeEmptyFolders(fromDirectory);

            saveUploadDocument(user, uploadDocument, "moving all files from documents to datastore");
            uploadDocument.validate();
        }
    }

    @SneakyThrows
    public void zip(CatalogueUser user, UploadDocument uploadDocument) {
        uploadDocument.validate();
        val uploadFiles = uploadDocument.getUploadFiles().get("datastore");
        if (uploadFiles.getInvalid().size() == 0) {
            val directory = new File(uploadFiles.getPath());
            val physicalLocation = uploadFiles.getPhysicalLocation();
            if (!isZipped(directory)) {
                val zipname = String.format("%s.zip", uploadDocument.getParentId());
                val zipFile = new File(directory, zipname);
                val zip = new ZipFile(zipFile.getAbsolutePath());
                val parameters = new ZipParameters();
                val filenames = FileListUtils.absolutePathsTree(directory);

                for (val filename : filenames) {
                    if (!filename.equals(zipname)) {
                        addToZip(new File(filename), zip, parameters);
                    }
                }

                uploadFiles.setDocuments(Maps.newHashMap());
                if (zipFile.exists()) {
                    ZipFileUtils.archiveZip(zipFile, unarchivedZip -> {
                        val innerFilenames = FileListUtils.absolutePathsTree(unarchivedZip, FilenameContainsFilterUtils.doesNotContain(".hash"), FilenameContainsFilterUtils.doesNotContain(".hash"));
                        for (val innerFilename : innerFilenames) {
                            val zippedUploadFile = UploadFileBuilder.create(uploadDocument.getId(), directory, physicalLocation, new File(innerFilename), UploadType.DOCUMENTS);
                            uploadFiles.getDocuments().put(zippedUploadFile.getPath(), zippedUploadFile);
                        }
                    });

                    val uploadFile = UploadFileBuilder.create(uploadDocument.getId(), directory, physicalLocation, zipFile, UploadType.DOCUMENTS);
                    uploadFiles.getDocuments().put(uploadFile.getPath(), uploadFile);
                }
            }

            uploadFiles.setZipped(true);
            saveUploadDocument(user, uploadDocument, "zipping datastore");
            uploadDocument.validate();
        }
    }

    @SneakyThrows
    private void addToZip(File file, ZipFile zipFile, ZipParameters parameters) {
        if (file.isDirectory()) zipFile.addFolder(file, parameters);
        else zipFile.addFile(file, parameters);
        FileUtils.forceDelete(file);
    }

    @SneakyThrows
    public void unzip(CatalogueUser user, UploadDocument uploadDocument) {
        uploadDocument.validate();
        val uploadFiles = uploadDocument.getUploadFiles().get("datastore");

        if (uploadFiles.getInvalid().size() == 0) {
            val directory = new File(uploadFiles.getPath());
            val physicalLocation = uploadFiles.getPhysicalLocation();
            if (isZipped(directory)) {
                val zipname = String.format("%s.zip", uploadDocument.getParentId());
                val zipFile = new File(directory, zipname);
                val zip = new ZipFile(zipFile.getAbsolutePath());
                zip.extractAll(directory.getAbsolutePath());
                FileUtils.forceDelete(zipFile);

                uploadFiles.setDocuments(Maps.newHashMap());

                ZipFileUtils.archive(directory, unarchivedZip -> {
                    val innerFilenames = FileListUtils.absolutePathsTree(unarchivedZip, FilenameContainsFilterUtils.doesNotContain(".hash"), FilenameContainsFilterUtils.doesNotContain(".hash"));
                    for (val innerFilename : innerFilenames) {
                        val zippedUploadFile = UploadFileBuilder.create(uploadDocument.getId(), directory, physicalLocation, new File(innerFilename), UploadType.DOCUMENTS);
                        uploadFiles.getDocuments().put(zippedUploadFile.getPath(), zippedUploadFile);
                    }
                });
            }

            uploadFiles.setZipped(false);
            saveUploadDocument(user, uploadDocument, "unzipping datastore");
            uploadDocument.validate();
        }
    }

    private void updateZipHashes (String guid, UploadFiles uploadFiles, List<String> compressList, File directory, String physicalLocation) {
        ZipFileUtils.archive(directory, unarchived -> {
            updateCompressedZipFiles(guid, uploadFiles, compressList, unarchived, physicalLocation);
        });
    }
    
    @SneakyThrows
    private void updateCompressedZipFiles(String guid, UploadFiles uploadFiles, List<String> compressList, File directory, String physicalLocation) {
        val zips = ZipFileUtils.compressListToZipFiles(compressList);
        for (val zipname : zips) {
            val zipFile = new File(zipname);
            if (zipFile.exists()) {
                UploadFile uploadFile = uploadFiles.getDocuments().get(zipname);
                if (uploadFile == null) uploadFile = uploadFiles.getInvalid().get(zipname);

                if (uploadFile == null) uploadFile = UploadFileBuilder.create(guid, directory, physicalLocation, zipFile, UploadType.DOCUMENTS);
                else UploadFileBuilder.update(guid, uploadFile, directory, physicalLocation, zipFile, UploadType.DOCUMENTS);

                uploadFiles.getInvalid().remove(zipname);
                uploadFiles.getDocuments().put(zipname, uploadFile);
            } else {
                uploadFiles.getInvalid().remove(zipname);
                uploadFiles.getDocuments().remove(zipname);
            }
        }
    }

    public void acceptInvalid(CatalogueUser user, UploadDocument uploadDocument, String name, String filename) {
        uploadDocument.validate();
        val uploadFiles = uploadDocument.getUploadFiles().get(name);
        val directory = new File(uploadFiles.getPath());
        val physicalLocation = uploadFiles.getPhysicalLocation();
        val uploadFile = uploadFiles.getInvalid().get(filename);
        uploadFiles.getInvalid().remove(filename);

        ZipFileUtils.archive(directory, unarchived -> {
            val filenames = FileListUtils.absolutePathsTree(directory, FilenameContainsFilterUtils.doesNotContain(".hash"), FilenameContainsFilterUtils.doesNotContain(".hash"));
            for (val innerFilename : filenames) {
                if (innerFilename.equals(filename)) {
                    UploadFileBuilder.update(uploadDocument.getId(), uploadFile, directory, physicalLocation, new File(innerFilename), UploadType.DOCUMENTS);
                }
            }
        });

        uploadFiles.getDocuments().put(filename, uploadFile);
        saveUploadDocument(user, uploadDocument, String.format("accepting invalid file %s", filename));
        uploadDocument.validate();
    }

    @SneakyThrows
    private void saveUploadDocument(CatalogueUser user, UploadDocument document, String message) {
        for (val uploadFiles : document.getUploadFiles().values()) {
            HashMap<String, UploadFile> invalid = Maps.newHashMap();
            val values = uploadFiles.getInvalid().values();
            values.removeIf(value -> !value.getType().equals(UploadType.MISSING_FILE));
            for (val value : values) {
                invalid.put(value.getPath(), value);
            }
            uploadFiles.setInvalid(invalid);
        }
        val found = documentRepository.read(document.getId());
        if (found != null)
            document.setMetadata(found.getMetadata());
        documentRepository.save(user, document, message);
    }

    private void removeEmptyFolders (File directory) {
        if (directory.exists()) {
            val files = FileListUtils.listFilesAndDirs(directory);
            files.remove(directory);
            for (val file : files) {
                if (file.isDirectory()) {
                    val subFiles = FileListUtils.listFilesAndDirs(file);
                    subFiles.remove(file);
                    if (subFiles.size() == 0) forceDelete(file.getAbsolutePath());
                }
            }

            val postDeleteFiles = FileListUtils.listFilesAndDirs(directory);
            postDeleteFiles.remove(directory);
            if (files.size() == 0) forceDelete(directory.getAbsolutePath());
        }
    }
}