package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;
import java.time.LocalDate;


@Value
public class Note {
    private String note, addedBy;
    private final LocalDate addedDate;

    @Builder
    @JsonCreator
    private Note(
        @JsonProperty("note") String note,
        @JsonProperty("addedBy") String addedBy,
        @JsonProperty("addedDate") LocalDate addedDate){
        this.note = nullToEmpty(note);
        this.addedBy = nullToEmpty(addedBy);
        this.addedDate = addedDate;
    }
}
