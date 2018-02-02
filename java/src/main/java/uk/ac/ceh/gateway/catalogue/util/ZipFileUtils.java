package uk.ac.ceh.gateway.catalogue.util;

import java.io.File;
import java.util.function.Consumer;


import org.apache.commons.io.FileUtils;
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
            val zipFile = new ZipFile(file);
            zipFile.extractAll(extracted.getAbsolutePath());
            extractAll(extracted);
        } catch (ZipException ze) {
            FileUtils.forceDelete(extracted);
            throw ze;
        }
        return extracted;
    }

    private static boolean isZipFile(File file) {
        return file.getPath().endsWith(".zip");
    }

    private static void compressAll (File directory) {
        compressAll(directory, null);   
    }

    private static void compressAll (File directory, ZipFile zip) {
        if (directory.exists()) {
            Lists.newArrayList(directory.listFiles())
                .stream()
                .forEach(file -> {
                    compress(file, zip);
                });
        }
    }

    @SneakyThrows
    private static void compress (File file, ZipFile zip) {
        val parameters = new ZipParameters();
        if (isExtracted(file)) {
            val zipFile = new File(file.getParentFile(), file.getName().replace("_extracted-", "") + ".zip");
            compressAll(file, new ZipFile(zipFile));
            if (zip != null) zip.addFile(zipFile, parameters);
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
        try {
            extractAll(directory);
            consumer.accept(directory);
        } finally {
            compressAll(directory);
        }
    }

    @SneakyThrows
    public static void archiveZip(File file, Consumer<File> consumer) {
        File extracted = null;
        try {
            extracted = extract(file);
            if (extracted.exists()) consumer.accept(extracted);
        } finally {
            if (extracted != null && extracted.exists()) {
                compressAll(extracted);
            }
        }
    }
}