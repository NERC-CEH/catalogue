package uk.ac.ceh.gateway.catalogue.erammp;

import static com.google.common.base.Strings.nullToEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@Value
public class DataLocation {
    private final String uid, name, fileLocation, purpose;

    @Builder
    @JsonCreator
    private DataLocation(
        @JsonProperty("uid") String uid, @JsonProperty("name") String name, @JsonProperty("fileLocation") String fileLocation,
        @JsonProperty("purpose") String purpose) {
        this.uid =  nullToEmpty(uid);
        this.name =  nullToEmpty(name);
        this.fileLocation =  nullToEmpty(fileLocation);
        this.purpose =  nullToEmpty(purpose);
    }
}
