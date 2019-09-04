package uk.ac.ceh.gateway.catalogue.erammp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class DataLocation {
    private final String name, fileLocation, purpose;
    
    @Builder
    @JsonCreator
    private DataLocation(
        @JsonProperty("name") String name, @JsonProperty("fileLocation") String fileLocation,
        @JsonProperty("purpose") String purpose) {
        this.name =  nullToEmpty(name);
        this.fileLocation =  nullToEmpty(fileLocation);
        this.purpose =  nullToEmpty(purpose);
    }
}
