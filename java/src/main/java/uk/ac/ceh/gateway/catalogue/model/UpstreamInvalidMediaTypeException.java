package uk.ac.ceh.gateway.catalogue.model;

/**
 * An exception which is thrown when transparently proxying an endpoint and 
 * that endpoint returns a media type which is either un desired or can not be
 * parsed
 * @author cjohn
 */
public class UpstreamInvalidMediaTypeException extends TransparentProxyException {
    static final long serialVersionUID = 1L;
    
    public UpstreamInvalidMediaTypeException(String mess, TransparentProxy request) {
        this(mess, null, request);
    }

    public UpstreamInvalidMediaTypeException(String mess, Throwable cause, TransparentProxy request) {
        super(mess, cause, request);
    }
}
