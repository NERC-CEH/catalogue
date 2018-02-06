package uk.ac.ceh.gateway.catalogue.util;

import java.io.File;

import org.apache.commons.io.filefilter.IOFileFilter;

public class FilenameContainsFilterUtils {
    public static IOFileFilter contains (String value) {
        return new IOFileFilter(){
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
            @Override
            public boolean accept(File file) {
                return file.getAbsolutePath().contains(value);
            }
        };
    }

    public static IOFileFilter doesNotContain (String value) {
        return new IOFileFilter(){
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
            @Override
            public boolean accept(File file) {
                return !file.getAbsolutePath().contains(value);
            }
        };
    }
}