package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.List;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.validation.Validator;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;

/**
 * For the given metadata document, evaluate the validators which have been 
 * @author cjohn
 */
@Data
public class ValidationIndexGenerator implements IndexGenerator<MetadataDocument, ValidationReport> {
    private final List<Validator> validators;
    
    @Override
    public ValidationReport generateIndex(MetadataDocument toIndex) {
        ValidationReport toReturn = new ValidationReport(toIndex.getId());
        
        for(Validator validator: validators) {
            toReturn.addValidationResult(validator.getName(), validator.validate(toIndex));
        }
        return toReturn;
    }
}
