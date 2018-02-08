package uk.ac.ceh.gateway.catalogue.util;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.val;

public class ZipFileUtils {

    private static void extractAll (File directory) {
        if (directory.exists()) {
            Lists.newArrayList(directory.listFiles())
                .stream()
                .filter(file -> isZipFile(file))
                .forEach(file -> extract(file));
        }
    }
    
    @SneakyThrows
    private static File extract(File file) {
        val extracted = new File(file.getParentFile(), String.format("_extracted-%s", file.getName().replace(".zip", "")));
        try {
            if (file.exists()) {
                val zipFile = new ZipFile(file);
                zipFile.extractAll(extracted.getAbsolutePath());
                extractAll(extracted);
            }
        } catch (ZipException ze) {
            FileUtils.forceDelete(extracted);
            throw ze;
        }
        return extracted;
    }

    private static boolean isZipFile(File file) {
        return file.getPath().endsWith(".zip");
    }

    private static void compressAll (List<String> compressList, File directory) {
        compressAll(compressList, directory, null);   
    }

    private static void compressAll (List<String> compressList, File directory, ZipFile zip) {
        if (directory.exists()) {
            Lists.newArrayList(directory.listFiles())
                .stream()
                .forEach(file -> {
                    compress(compressList, file, zip);
                });
        }
    }

    @SneakyThrows
    private static void compress (List<String> compressList, File file, ZipFile zip) {
        val parameters = new ZipParameters();
        if (compressList.contains(file.getAbsolutePath()) && isExtracted(file)) {
            val zipFile = new File(file.getParentFile(), file.getName().replace("_extracted-", "") + ".zip");
            if (zipFile.exists()) FileUtils.forceDelete(zipFile);     
            compressAll(compressList, file, new ZipFile(zipFile));
            if (zip != null) zip.addFile(zipFile, parameters);
            FileUtils.forceDelete(file);
        } else if (isExtracted(file)) {
            FileUtils.forceDelete(file);
        } else if (zip != null) {
            if (file.isDirectory()) zip.addFolder(file, parameters);
            else zip.addFile(file, parameters);
        }
    }

    private static boolean isExtracted(File file) {
        return file.isDirectory() && file.getName().startsWith("_extracted");
    }

    public static void archive(File directory, Consumer<File> consumer) {
        archive(Lists.newArrayList(), directory, consumer);
    }

    public static void archive(List<String> compressList, File directory, Consumer<File> consumer) {
        try {
            extractAll(directory);
            consumer.accept(directory);
        } finally {
            compressAll(compressList, directory);
        }
    }

    @SneakyThrows
    public static void archiveZip(File file, Consumer<File> consumer) {
        archiveZip(Lists.newArrayList(), file, consumer);
    }

    @SneakyThrows
    public static void archiveZip(List<String> compressList, File file, Consumer<File> consumer) {
        File extracted = null;
        try {
            extracted = extract(file);
            if (extracted.exists()) consumer.accept(extracted);
        } finally {
            if (extracted != null && extracted.exists()) {
                compressAll(compressList, extracted);
                if (extracted.exists()) FileUtils.forceDelete(extracted);
            }
        }
    }

    public static List<String> filenameToCompessList(String filename) {
        List<String> compressList = Lists.newArrayList();
        val split = filename.split("/");
        String compressFile = "";
        for (val chunk : split) {
            if (StringUtils.isNotBlank(chunk)) {
                compressFile += "/"  + chunk;
                if (chunk.startsWith("_extracted")) compressList.add(compressFile);
            }
        }
        return compressList;
    }

    public static List<String> compressListToZipFiles(List<String> compressList) {
        return compressList.stream().map(compressed -> {
            val split = compressed.split("/");
            val folder = split[split.length - 1];
            val value = compressed.replace(folder, "");
            return value + folder.replace("_extracted-", "") + ".zip";
        }).collect(Collectors.toList());
    }
}