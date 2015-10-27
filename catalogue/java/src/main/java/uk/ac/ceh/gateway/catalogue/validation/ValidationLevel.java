package uk.ac.ceh.gateway.catalogue.validation;

import java.util.Arrays;
import java.util.List;

/**
 * The following defines a list of validation values from least severe to most.
 * @author cjohn
 */
public enum ValidationLevel {
    VALID, WARNING, ERROR, FAILED_TO_READ;
        
    public boolean isMoreSevere(ValidationLevel a) {
        List<ValidationLevel> levels = Arrays.asList(values());
        return levels.indexOf(this) > levels.indexOf(a);
    }
}
