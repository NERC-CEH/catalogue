package uk.ac.ceh.gateway.catalogue.validation;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * The following object represents a validation report for an individual 
 * metadata document.
 * 
 * The object is composed of a metadata documents id and a list of validation 
 * summaries from validators which have been run against the document represented
 * by that ID
 */
@Data
public class ValidationReport {
    private final String documentId;
    private final Map<String, ValidationLevel> results;
    
    public ValidationReport(String documentId) {
        this.documentId = documentId;
        this.results = new HashMap<>();
    }

    public void addValidationResult(String name, ValidationResult result) {
        results.put(name, result.getWorstLevel());
    }
}
