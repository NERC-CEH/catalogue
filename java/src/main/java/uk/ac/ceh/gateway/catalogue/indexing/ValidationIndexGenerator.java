package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.Validator;

import java.util.List;

/**
 * For the given metadata document, evaluate the validators which have been
 */
@AllArgsConstructor
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
