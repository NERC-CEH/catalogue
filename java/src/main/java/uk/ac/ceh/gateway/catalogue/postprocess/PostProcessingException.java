package uk.ac.ceh.gateway.catalogue.postprocess;

public class PostProcessingException extends Exception {
    static final long serialVersionUID = 1L;
    public PostProcessingException() {
        super();
    }

    public PostProcessingException(String mess) {
        super(mess);
    }

    public PostProcessingException(Throwable cause) {
        super(cause);
    }

    public PostProcessingException(String mess, Throwable cause) {
        super(mess, cause);
    }
}
