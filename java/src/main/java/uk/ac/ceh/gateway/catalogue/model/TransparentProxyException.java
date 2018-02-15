package uk.ac.ceh.gateway.catalogue.model;

import lombok.Getter;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * An exception thrown when transparent proxying fails. The original request will
 * be attached to this exception
 */
public class TransparentProxyException extends HttpMessageNotReadableException {
    static final long serialVersionUID = 1L;
    private final @Getter TransparentProxy request;

    public TransparentProxyException(String mess, TransparentProxy request) {
        this(mess, null, request);
    }

    public TransparentProxyException(String mess, Throwable cause, TransparentProxy request) {
        super(mess, cause);
        this.request = request;
    }
}
