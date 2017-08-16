package uk.ac.ceh.gateway.catalogue.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;
import org.springframework.security.crypto.codec.Hex;

@Value
public class FileChecksum {
    private static final Pattern LINE_REGEX = Pattern.compile("([a-f0-9]{32})\\ \\*(.*)");
    private final byte[] md5;
    private String filename;

    public String getMD5Hash() {
        return new String(Hex.encode(md5));
    }

    public static FileChecksum fromLine(String line) {
        Matcher matcher = LINE_REGEX.matcher(line);
        if(matcher.matches()) {
            return new FileChecksum(Hex.decode(matcher.group(1)), matcher.group(2));
        }
        else {
            throw new IllegalArgumentException("The provided line is not a valid file checksum line");
        }
    }
}