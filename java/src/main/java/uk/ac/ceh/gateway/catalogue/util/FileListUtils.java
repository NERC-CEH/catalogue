package uk.ac.ceh.gateway.catalogue.util;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class FileListUtils {

    public static List<File> listFiles (File directory, IOFileFilter fileFilter, IOFileFilter directoryFilter) {
        return FileUtils.listFiles(directory, fileFilter, fileFilter)
            .stream()
            .collect(Collectors.toList());
    }

    public static List<File> listFiles (File directory) {
        return listFiles(directory, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    };

    public static List<String> relativePathsTree (File directory, IOFileFilter fileFilter, IOFileFilter directoryFilter) {
        return listFiles(directory, fileFilter, fileFilter)
            .stream()
            .map(file -> { return relativePath(directory, file); })
            .filter(filename -> { return !filename.equals(""); })
            .collect(Collectors.toList());
    }
    
    private static String relativePath(File directory, File file) {
        return file.getAbsolutePath()
                .replaceAll(directory.getAbsolutePath(), "")
                .replaceFirst("/", "");
    }

    public static List<String> relativePathsTree (File directory) {
        return relativePathsTree(directory, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    }
}