package uk.ac.ceh.gateway.catalogue.validation;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;

/**
 * This is an example of a validator. This should be removed
 */
public class MediaTypeValidator extends AbstractDocumentValidator {

    public MediaTypeValidator(String name, MediaType mediaType, DocumentWritingService documentWritingService) {
        super(name, mediaType, documentWritingService);
    }

    @Override
    public ValidationResult validate(InputStream stream) {
        try {
            IOUtils.copy(stream, NULL_OUTPUT_STREAM);
            return new ValidationResult();
        } catch (IOException ex) {
            return new ValidationResult().reject(ex.getMessage(), ValidationLevel.ERROR);
        }
    }
}
