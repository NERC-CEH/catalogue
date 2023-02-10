package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import lombok.Builder;
import java.time.LocalDate;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateSerializer;

@Value
public class DatasetReferenceDate {
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate creationDate, publicationDate, revisionDate, unavailableDate, supersededDate, deprecatedDate, releasedDate;
    
    @Builder
    @JsonCreator
    private DatasetReferenceDate(
        @JsonProperty("creationDate") LocalDate creationDate,
        @JsonProperty("publicationDate") LocalDate publicationDate,
        @JsonProperty("revisionDate") LocalDate revisionDate,
        @JsonProperty("unavailableDate") LocalDate unavailableDate,
        @JsonProperty("supersededDate") LocalDate supersededDate,
        @JsonProperty("deprecatedDate") LocalDate deprecatedDate,
        @JsonProperty("releasedDate") LocalDate releasedDate) {
        
        this.creationDate = creationDate;
        this.publicationDate = publicationDate;
        this.revisionDate = revisionDate;        
        this.unavailableDate = unavailableDate;        
        this.supersededDate = supersededDate;        
        this.deprecatedDate = deprecatedDate;        
        this.releasedDate = releasedDate;        
    }
}
