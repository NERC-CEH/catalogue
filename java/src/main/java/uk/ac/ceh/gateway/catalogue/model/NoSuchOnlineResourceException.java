package uk.ac.ceh.gateway.catalogue.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author cjohn
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoSuchOnlineResourceException extends IllegalArgumentException {
    public NoSuchOnlineResourceException(String mess) {
        super(mess);
    }
}
