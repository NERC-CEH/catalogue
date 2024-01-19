package uk.ac.ceh.gateway.catalogue.validation;

import java.util.Arrays;
import java.util.List;

/**
 * The following defines a list of validation values from least severe to most.
 */
public enum ValidationLevel {
    VALID("Valid"), WARNING("Warning"), ERROR("Error"), FAILED_TO_READ("Failed to read");

    private final String label;

    ValidationLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isMoreSevere(ValidationLevel a) {
        List<ValidationLevel> levels = Arrays.asList(values());
        return levels.indexOf(this) > levels.indexOf(a);
    }
}
