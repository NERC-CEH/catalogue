package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;
import lombok.Data;

@Data
public class Checksum {
    private final String hash;
    private final File file;
}