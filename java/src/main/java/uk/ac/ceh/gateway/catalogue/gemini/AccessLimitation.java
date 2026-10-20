package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

import java.util.Set;

@Value
public class AccessLimitation {
    // N07 "no limitations" is what the editor saves; COAR "open access" is what it saved before June 2026
    private static final Set<String> OPEN_ACCESS_URIS = Set.of(
        "http://vocab.nerc.ac.uk/collection/N07/current/UNRS/",
        "http://purl.org/coar/access_right/c_abf2"
    );

    private final String value, availability, description, uri;

    @Builder
    @JsonCreator
    public AccessLimitation(
        @JsonProperty("value") String value,
        @JsonProperty("availability") String availability,
        @JsonProperty("description") String description,
        @JsonProperty("uri") String uri){
        this.value = nullToEmpty(value);
        this.availability = nullToEmpty(availability);
        this.description = nullToEmpty(description);
        this.uri = nullToEmpty(uri);
    }

    // Not named isOpenAccess, so Jackson doesn't write it into the record as a property
    public boolean grantsOpenAccess() {
        return OPEN_ACCESS_URIS.contains(uri);
    }

}
