package uk.ac.ceh.gateway.catalogue.serviceagreement;

public class ServiceAgreementException extends RuntimeException{

    public ServiceAgreementException(Exception ex) {
    }

    /**
     * @param message returned to the client as the body of the 400 response, so it must not contain internal detail
     */
    public ServiceAgreementException(String message) {
        super(message);
    }

}
