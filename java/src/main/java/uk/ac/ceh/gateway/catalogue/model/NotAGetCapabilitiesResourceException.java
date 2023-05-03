package uk.ac.ceh.gateway.catalogue.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotAGetCapabilitiesResourceException extends IllegalArgumentException {
    static final long serialVersionUID = 1L;

    public NotAGetCapabilitiesResourceException(String mess) {
        super(mess);
    }
}
