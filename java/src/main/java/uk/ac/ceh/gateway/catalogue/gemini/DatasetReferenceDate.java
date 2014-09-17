package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.Value;
import lombok.experimental.Builder;
import java.time.LocalDate;

@Value
@Builder
public class DatasetReferenceDate {
    
    private final LocalDate creationDate, publicationDate, revisionDate;
    
}
