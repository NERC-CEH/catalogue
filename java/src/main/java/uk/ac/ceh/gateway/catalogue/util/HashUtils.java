package uk.ac.ceh.gateway.catalogue.util;

import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.codec.digest.DigestUtils;
import lombok.SneakyThrows;
import lombok.val;

public class HashUtils {
    @SneakyThrows
    public static String hash(File file) {
        val input = new FileInputStream(file);
        val hash = DigestUtils.md5Hex(input);
        input.close();
        return hash;
    }
}