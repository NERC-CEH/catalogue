package uk.ac.ceh.gateway.catalogue.model;

import org.springframework.http.HttpInputMessage;

/**
 * An exception which is thrown when transparently proxying an endpoint and
 * that endpoint returns a media type which is either un desired or can not be
 * parsed
 */
public class UpstreamInvalidMediaTypeException extends TransparentProxyException {
    static final long serialVersionUID = 1L;

    public UpstreamInvalidMediaTypeException(String mess, Throwable cause, HttpInputMessage inputMessage, TransparentProxy request) {
        super(mess, cause, inputMessage, request);
    }
}
