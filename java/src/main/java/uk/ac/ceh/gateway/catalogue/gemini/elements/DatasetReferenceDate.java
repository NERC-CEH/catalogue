package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;
import lombok.experimental.Builder;
import org.joda.time.LocalDate;

@Value
@Builder
public class DatasetReferenceDate {
    
    private final LocalDate creationDate, publicationDate, revisionDate;
    
}
