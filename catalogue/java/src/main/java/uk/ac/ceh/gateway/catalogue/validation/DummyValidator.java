package uk.ac.ceh.gateway.catalogue.validation;

import java.io.InputStream;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;

/**
 * This is an example of a validator. This will should be removed
 * @author cjohn
 */
public class DummyValidator extends AbstractDocumentValidator {

    public DummyValidator(DocumentWritingService documentWritingService) {
        super("Dummy Validator", MediaType.APPLICATION_JSON, documentWritingService);
    }

    @Override
    public ValidationResult validate(InputStream stream) {
        return new ValidationResult();
    }
    
}
