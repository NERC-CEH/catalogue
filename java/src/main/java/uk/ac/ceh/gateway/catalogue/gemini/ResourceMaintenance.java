package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Value;

@Value
public class ResourceMaintenance {
    private String frequencyOfUpdate, note;

    @Builder
    @JsonCreator
    private ResourceMaintenance(
        @JsonProperty("frequencyOfUpdate") String frequencyOfUpdate,
        @JsonProperty("note") String note) {
        this.frequencyOfUpdate = Strings.nullToEmpty(frequencyOfUpdate);
        this.note = Strings.nullToEmpty(note);
    }
}
