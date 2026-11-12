package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class FileSizesTest {
    // Shared with parseSizeTest.js: the strings web/src/file-size/formatBytes.js gives:
    //   0 -> "0 B", 1 -> "1 B", 1023 -> "1023 B", 1024 -> "1 KB", 1536 -> "1.5 KB",
    //   33554432 -> "32 MB", 1048575 -> "1 MB", 1.5 * 1024^4 -> "1.5 TB"
    @ParameterizedTest
    @CsvSource({
        "0, 0 B",
        "1, 1 B",
        "1023, 1023 B",
        "1024, 1 KB",
        "1536, 1.5 KB",
        "33554432, 32 MB",
        "1048575, 1 MB",
        "1649267441664, 1.5 TB",
        "1125899906842624, 1024 TB",
        "1152, 1.13 KB",
        "-1, 0 B"
    })
    void formatsAsTheBrowserDoes(long bytes, String expected) {
        assertThat(new FileSizes().format(bytes), equalTo(expected));
    }
}
