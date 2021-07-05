package uk.ac.ceh.gateway.catalogue.validation;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;

import java.io.InputStream;

/**
 * The following is a validator which will read a document from the
 * documentWritingService as a specified mediatype. It will then delegate the
 * read InputStream to the subclasses #validate(InputStream) method.
 */
@Slf4j
@ToString
public abstract class AbstractDocumentValidator implements Validator {
    @Getter private final String name;
    private final MediaType mediaType;
    private final DocumentWritingService documentWritingService;

    public AbstractDocumentValidator(String name, MediaType mediaType, DocumentWritingService documentWritingService) {
        this.name = name;
        this.mediaType = mediaType;
        this.documentWritingService = documentWritingService;
        log.info("Creating {}", this);
    }

    @Override
    public ValidationResult validate(Object input) {
        try {
            return validate(documentWritingService.write(input, mediaType));
        } catch (Exception ex) {
            log.error("Failed to validate document {}", ex.getMessage());
            return new ValidationResult()
                    .reject(ex.getMessage(), ValidationLevel.FAILED_TO_READ);
        }
    }

    public abstract ValidationResult validate(InputStream stream);
}
