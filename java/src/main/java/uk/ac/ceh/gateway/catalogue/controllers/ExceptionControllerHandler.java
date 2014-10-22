package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ceh.gateway.catalogue.model.ErrorResponse;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;

/**
 *
 * @author cjohn
 */
@ControllerAdvice
public class ExceptionControllerHandler {
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(ExternalResourceFailureException.class)
    @ResponseBody
    public ErrorResponse handleExternalResourceFailureException(ExternalResourceFailureException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}
