package uk.ac.ceh.gateway.catalogue.erammp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class ErammpModelOutput {
    private final String internalName,definition,unit,spatialResolution,temporalResolution,reuse,displayed,displayFormat,ipr,additionalNotes;

    @Builder
    @JsonCreator
    private ErammpModelOutput(
        @JsonProperty("internalName") String internalName,
        @JsonProperty("definition") String definition,
        @JsonProperty("unit") String unit,
        @JsonProperty("spatialResolution") String spatialResolution,
        @JsonProperty("temporalResolution") String temporalResolution,
        @JsonProperty("reuse") String reuse,
        @JsonProperty("displayed") String displayed,
        @JsonProperty("displayFormat") String displayFormat,
        @JsonProperty("ipr") String ipr,
        @JsonProperty("additionalNotes") String additionalNotes
    ) {
        this.internalName = nullToEmpty(internalName);
        this.definition = nullToEmpty(definition);
        this.unit = nullToEmpty(unit);
        this.spatialResolution = nullToEmpty(spatialResolution);
        this.temporalResolution = nullToEmpty(temporalResolution);
        this.reuse = nullToEmpty(reuse);
        this.displayed = nullToEmpty(displayed);
        this.displayFormat = nullToEmpty(displayFormat);
        this.ipr = nullToEmpty(ipr);
        this.additionalNotes = nullToEmpty(additionalNotes);
    }
}

