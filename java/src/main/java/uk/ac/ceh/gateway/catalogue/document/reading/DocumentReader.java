package uk.ac.ceh.gateway.catalogue.document.reading;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

import static java.lang.String.format;

@Service
@Slf4j
@ToString
public class DocumentReader {
    private final String datastore;

    public DocumentReader(
            @Value("${data.repository.location}") String datastore
    ) {
        this.datastore = datastore;
        log.info("Creating {}", this);
    }

    public File read(String guid, String extension) {
        return new File(datastore, format("%s.%s", guid, extension));
    }

}
