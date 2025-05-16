package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

@Value
public class Fileset {
    private final String filesetName, encodingFormat, includes;
    public final List<ObservedProperty> observedProperty;

    @Builder
    @JsonCreator
    private Fileset(
        @JsonProperty("filesetName") String filesetName,
        @JsonProperty("encodingFormat") String encodingFormat,
        @JsonProperty("includes") String includes,
        @JsonProperty("observedProperty") List<ObservedProperty> observedProperty) {
        this.filesetName = nullToEmpty(filesetName);
        this.encodingFormat = nullToEmpty(encodingFormat);
        this.includes = nullToEmpty(includes);
        this.observedProperty = (observedProperty == null)? new ArrayList<>(): observedProperty;
    }
}
