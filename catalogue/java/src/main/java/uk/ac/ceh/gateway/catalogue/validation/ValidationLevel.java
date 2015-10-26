package uk.ac.ceh.gateway.catalogue.validation;

import java.util.Arrays;
import java.util.List;

/**
 * The following defines a list of validation values from least severe to most.
 * @author cjohn
 */
public enum ValidationLevel {
    VALID, FAILED_TO_READ;
        
    public static boolean isMoreSevere(ValidationLevel a, ValidationLevel b) {
        List<ValidationLevel> levels = Arrays.asList(values());
        return levels.indexOf(b) > levels.indexOf(a);
    }
}
