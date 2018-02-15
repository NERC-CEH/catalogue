package uk.ac.ceh.gateway.catalogue.validation;

/**
 * Define a named object validator which given an object will return a 
 * Validation Result.
 */
public interface Validator {
    String getName();
    ValidationResult validate(Object input);
}
