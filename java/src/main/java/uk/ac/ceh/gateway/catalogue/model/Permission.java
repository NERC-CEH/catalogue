package uk.ac.ceh.gateway.catalogue.model;

/**
 * The following enum represents the different permissions that a UKEOF User may
 * have on a per document basis.
 * 
 * @author Christopher Johnson
 */
public enum Permission {
    DOCUMENT_READ, SENSITIVE_READ, INFO_READ, WRITE;
}