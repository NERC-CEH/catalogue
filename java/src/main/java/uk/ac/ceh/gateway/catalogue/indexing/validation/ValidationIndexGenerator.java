package uk.ac.ceh.gateway.catalogue.indexing.validation;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
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
    public ValidationReport generateIndex(MetadataDocument metadataDocument) {
        val validationReport = new ValidationReport(metadataDocument.getId());

        for(Validator validator: validators) {
            validationReport.addValidationResult(validator.getName(), validator.validate(metadataDocument));
        }
        log.debug(validationReport.toString());
        return validationReport;
    }
}
