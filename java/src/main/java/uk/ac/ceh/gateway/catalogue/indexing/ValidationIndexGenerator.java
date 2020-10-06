package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.Validator;

import java.util.List;

/**
 * For the given metadata document, evaluate the validators which have been
 */
@Slf4j
@ToString
public class ValidationIndexGenerator implements IndexGenerator<MetadataDocument, ValidationReport> {
    private final List<Validator> validators;

    public ValidationIndexGenerator(List<Validator> validators) {
        this.validators = validators;
        log.info("Creating {}", this);
    }

    @Override
    public ValidationReport generateIndex(MetadataDocument toIndex) {
        ValidationReport toReturn = new ValidationReport(toIndex.getId());
        
        for(Validator validator: validators) {
            toReturn.addValidationResult(validator.getName(), validator.validate(toIndex));
        }
        return toReturn;
    }
}
