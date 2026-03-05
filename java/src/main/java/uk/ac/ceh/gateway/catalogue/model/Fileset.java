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
    private final String filesetName, encodingFormat, includes, filesetRegex;
    public final List<ObservedProperty> observedProperty;

    @Builder
    @JsonCreator
    private Fileset(
        @JsonProperty("filesetName") String filesetName,
        @JsonProperty("encodingFormat") String encodingFormat,
        @JsonProperty("includes") String includes,
        @JsonProperty("filesetRegex") String filesetRegex,
        @JsonProperty("observedProperty") List<ObservedProperty> observedProperty) {
        this.filesetName = nullToEmpty(filesetName);
        this.encodingFormat = nullToEmpty(encodingFormat);
        this.includes = nullToEmpty(includes);
        String safeRegex = nullToEmpty(filesetRegex);

        if (!this.includes.isEmpty()) {
            this.filesetRegex = "^" +
                    this.includes
                        .replace(".", "\\.")  // escape literal dot
                        .replace("*", ".+")   // glob * => regex .+
                        .replace("?", ".")    // glob ? => regex .
                    + "$";
        } else {
            this.filesetRegex = safeRegex;
        }

        this.observedProperty = (observedProperty == null)? new ArrayList<>(): observedProperty;
    }
}
