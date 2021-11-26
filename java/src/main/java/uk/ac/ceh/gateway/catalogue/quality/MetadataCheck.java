package uk.ac.ceh.gateway.catalogue.quality;

import lombok.Value;
import uk.ac.ceh.gateway.catalogue.quality.Results.Severity;

@Value
public class MetadataCheck {
    String test;
    Severity severity;
}
