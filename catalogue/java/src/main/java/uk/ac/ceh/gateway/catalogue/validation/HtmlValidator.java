package uk.ac.ceh.gateway.catalogue.validation;

import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.http.MediaType;
import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;

/**
 * This is an example of a validator. This will should be removed
 * @author cjohn
 */
public class HtmlValidator extends AbstractDocumentValidator {

    public HtmlValidator(DocumentWritingService documentWritingService) {
        super("HTML", MediaType.TEXT_HTML, documentWritingService);
    }

    @Override
    public ValidationResult validate(InputStream stream) {
        ValidationResult toReturn = new ValidationResult();
        Tidy tidy = new Tidy();
        tidy.setMessageListener((m) -> toReturn.reject(m.getMessage(), lookup(m.getLevel())));
        tidy.parse(stream, (OutputStream)null);
        return toReturn;
    }
    
    private ValidationLevel lookup(TidyMessage.Level level) {
        switch(level.getCode()) {
            case 3:   return ValidationLevel.ERROR;
            case 2:   return ValidationLevel.WARNING;
            default:  return ValidationLevel.VALID;
        }
    }
    
}
