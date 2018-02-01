package uk.ac.ceh.gateway.catalogue.util;

import java.io.File;
import java.util.function.Consumer;
import org.apache.commons.io.FileUtils;
import net.lingala.zip4j.core.ZipFile;
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
    private static void extract(File file) {
        val zipFile = new ZipFile(file);
        val extracted = new File(file.getParentFile(), String.format("_extracted-%s", file.getName().replace(".zip", "")));
        zipFile.extractAll(extracted.getAbsolutePath());
        // i think I need to do this but not sure :s
        // FileUtils.forceDelete(file);
        extractAll(extracted);
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
        extractAll(directory);
        consumer.accept(directory);
        compressAll(directory);
    }
}