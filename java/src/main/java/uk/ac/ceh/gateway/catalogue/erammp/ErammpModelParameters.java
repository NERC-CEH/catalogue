package uk.ac.ceh.gateway.catalogue.erammp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class ErammpModelParameters {
    private final String internalName, externalName, definition, inputSource, unit, datatype, spatialResolution, temporalResolution, managementPolicy, access, licensing, dataSource, reuse, displayed, displayFormat, ipr, additionalNotes;

    @Builder
    @JsonCreator
    private ErammpModelParameters(
        @JsonProperty("internalName") String internalName,
        @JsonProperty("externalName") String externalName,
        @JsonProperty("definition") String definition,
        @JsonProperty("inputSource") String inputSource,
        @JsonProperty("unit") String unit,
        @JsonProperty("datatype") String datatype,
        @JsonProperty("spatialResolution") String spatialResolution,
        @JsonProperty("temporalResolution") String temporalResolution,
        @JsonProperty("managementPolicy") String managementPolicy,
        @JsonProperty("access") String access,
        @JsonProperty("licensing") String licensing,
        @JsonProperty("dataSource") String dataSource,
        @JsonProperty("reuse") String reuse,
        @JsonProperty("displayed") String displayed,
        @JsonProperty("displayFormat") String displayFormat,
        @JsonProperty("ipr") String ipr,
        @JsonProperty("additionalNotes") String additionalNotes
    ) {
        this.internalName = nullToEmpty(internalName);
        this.externalName = nullToEmpty(externalName);
        this.definition = nullToEmpty(definition);
        this.inputSource = nullToEmpty(inputSource);
        this.unit = nullToEmpty(unit);
        this.datatype = nullToEmpty(datatype);
        this.spatialResolution = nullToEmpty(spatialResolution);
        this.temporalResolution = nullToEmpty(temporalResolution);
        this.managementPolicy = nullToEmpty(managementPolicy);
        this.access = nullToEmpty(access);
        this.licensing = nullToEmpty(licensing);
        this.dataSource = nullToEmpty(dataSource);
        this.reuse = nullToEmpty(reuse);
        this.displayed = nullToEmpty(displayed);
        this.displayFormat = nullToEmpty(displayFormat);
        this.ipr = nullToEmpty(ipr);
        this.additionalNotes = nullToEmpty(additionalNotes);
    }
}


