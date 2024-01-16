package uk.ac.ceh.gateway.catalogue.validation;

import java.util.List;
import lombok.Data;

/**
 * The following class represents the complete set of validation results for a
 * particular validator.
 *
 * Since a document can only be in one validation state, we can group the
 * documents in a similar state together. This is useful when presented an
 * overview of the entire catalogue.
 */
@Data
public class ValidatorResult {
    private final String name;
    private final List<ValidatorState> states;

    @Data
    public static class ValidatorState {
        private final ValidationLevel level;
        private final List<String> documents;
    }
}
