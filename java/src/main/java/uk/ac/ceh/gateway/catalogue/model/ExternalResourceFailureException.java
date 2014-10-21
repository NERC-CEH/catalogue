package uk.ac.ceh.gateway.catalogue.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author cjohn
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class ExternalResourceFailureException extends RuntimeException {
    public ExternalResourceFailureException(String mess) {
        super(mess);
    }
}
