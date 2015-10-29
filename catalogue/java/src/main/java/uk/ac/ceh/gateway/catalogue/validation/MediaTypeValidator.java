package uk.ac.ceh.gateway.catalogue.validation;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;

/**
 * This is an example of a validator. This will should be removed
 * @author cjohn
 */
public class MediaTypeValidator extends AbstractDocumentValidator {

    public MediaTypeValidator(String name, MediaType mediaType, DocumentWritingService documentWritingService) {
        super(name, mediaType, documentWritingService);
    }

    @Override
    public ValidationResult validate(InputStream stream) {
        try {
            IOUtils.copy(stream, new NullOutputStream());
            return new ValidationResult();
        } catch (IOException ex) {
            return new ValidationResult().reject(ex.getMessage(), ValidationLevel.ERROR);
        }
    }    
}
