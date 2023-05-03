package uk.ac.ceh.gateway.catalogue.validation;

import lombok.Data;

/**
 * A validation message which consists of a message and a validation severity 
 * level
 */
@Data
public class ValidationMessage {
    private final String message;
    private final ValidationLevel level;
}
