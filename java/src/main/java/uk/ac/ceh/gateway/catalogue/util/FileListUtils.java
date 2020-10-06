package uk.ac.ceh.gateway.catalogue.util;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileListUtils {

    public static List<File> listFilesAndDirs (File directory, IOFileFilter fileFilter, IOFileFilter directoryFilter) {
        return new ArrayList<>(FileUtils.listFilesAndDirs(directory, fileFilter, fileFilter));
    }

    public static List<File> listFiles (File directory, IOFileFilter fileFilter, IOFileFilter directoryFilter) {
        return new ArrayList<>(FileUtils.listFiles(directory, fileFilter, fileFilter));
    }

    public static List<File> listFilesAndDirs (File directory) {
        return listFilesAndDirs(directory, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    };

    public static List<File> listFiles (File directory) {
        return listFiles(directory, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    };

    public static List<String> absolutePathsTree (File directory, IOFileFilter fileFilter, IOFileFilter directoryFilter) {
        if (!directory.exists()) return Lists.newArrayList();
        return listFiles(directory, fileFilter, fileFilter)
            .stream()
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());
    }

    public static List<String> absolutePathsTreeAndDirs (File directory, IOFileFilter fileFilter, IOFileFilter directoryFilter) {
        if (!directory.exists()) return Lists.newArrayList();
        return listFilesAndDirs(directory, fileFilter, fileFilter)
            .stream()
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());
    }

    public static List<String> absolutePathsTree (File directory) {
        return absolutePathsTree(directory, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    }

    public static List<String> absolutePathsTreeAndDirs (File directory) {
        return absolutePathsTreeAndDirs(directory, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    }

    public static List<String> relativePathsTree (File directory, IOFileFilter fileFilter, IOFileFilter directoryFilter) {
        return absolutePathsTree(directory, fileFilter, directoryFilter)
            .stream()
            .map(filename -> { return relativePath(directory, filename); })
            .filter(filename -> { return !filename.equals(""); })
            .collect(Collectors.toList());
    }

    public static List<String> relativePathsTreeAndDirs (File directory, IOFileFilter fileFilter, IOFileFilter directoryFilter) {
        return absolutePathsTreeAndDirs(directory, fileFilter, directoryFilter)
            .stream()
            .map(filename -> { return relativePath(directory, filename); })
            .filter(filename -> { return !filename.equals(""); })
            .collect(Collectors.toList());
    }
    
    private static String relativePath(File directory, String filename) {
        return filename
                .replaceAll(directory.getAbsolutePath(), "")
                .replaceFirst("/", "");
    }

    public static List<String> relativePathsTree (File directory) {
        return relativePathsTree(directory, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    }

    public static List<String> relativePathsTreeAndDirs (File directory) {
        return relativePathsTreeAndDirs(directory, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    }
}