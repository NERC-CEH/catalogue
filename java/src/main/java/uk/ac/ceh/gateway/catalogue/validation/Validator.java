package uk.ac.ceh.gateway.catalogue.validation;

import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

/**
 * Define a named object validator which given an object will return a
 * Validation Result.
 */
public interface Validator {
    String getName();
    ValidationResult validate(MetadataDocument input);
}
