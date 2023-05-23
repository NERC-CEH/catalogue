package uk.ac.ceh.gateway.catalogue.validation;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A validation result object for a named validation (e.g. HTML validation)
 *
 * Initially the result starts off in the VALID state. If the reject methods are
 * not called then the validation was successful. If a reject occurs, then we
 * keep track of the worst severity level which we can then return as a summary.
 */
@Data
public class ValidationResult {
    private final List<ValidationMessage> messages;
    private ValidationLevel worstLevel;

    public ValidationResult() {
        this.worstLevel = ValidationLevel.VALID;
        this.messages = new ArrayList<>();
    }

    /**
     * Log the rejection reason and update the severity level if this was worse
     * than previously seen.
     * @param message A message containing a severity level
     * @return this
     */
    public ValidationResult reject(ValidationMessage message) {
        if(message.getLevel().isMoreSevere(worstLevel)) {
            worstLevel = message.getLevel();
        }

        messages.add(message);
        return this;
    }

    public ValidationResult reject(String message, ValidationLevel level) {
        return reject(new ValidationMessage(message, level));
    }
}
