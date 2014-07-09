package uk.ac.ceh.gateway.catalogue.linking;

import lombok.Value;

@Value
public class CoupledResource {
    private final String fileIdentifier, coupledResource;
}