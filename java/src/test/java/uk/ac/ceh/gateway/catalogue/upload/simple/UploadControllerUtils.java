package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;

public class UploadControllerUtils {
    public static final String ID = "993c5778-e139-4171-a57f-7a0f396be4b8";

    @SneakyThrows
    public static MockMultipartFile dataCsv(Class clazz) {
        return new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                IOUtils.toByteArray(clazz.getResourceAsStream("data.csv"))
        );
    }

    @SneakyThrows
    public static MockMultipartFile fileWithSpacesCsv(Class clazz) {
        return new MockMultipartFile(
                "file",
                "file with spaces.csv",
                "text/csv",
                IOUtils.toByteArray(clazz.getResourceAsStream("file with spaces.csv"))
        );
    }
}
