package uk.ac.ceh.gateway.catalogue.validation;

import java.io.InputStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;

/**
 * The following is a validator which will read a document from the 
 * documentWritingService as a specified mediatype. It will then delegate the 
 * read InputStream to the subclasses #validate(InputStream) method.
 */
@Data
@Slf4j
public abstract class AbstractDocumentValidator implements Validator {
    private final String name;
    private final MediaType mediaType;
    private final DocumentWritingService documentWritingService;

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
