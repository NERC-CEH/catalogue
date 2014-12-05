package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import lombok.experimental.Builder;
import java.time.LocalDate;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateSerializer;

@Value
@Builder
public class DatasetReferenceDate {
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate creationDate, publicationDate, revisionDate;
    
}
