package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class Funding {
    private final String funderName, funderIdentifier, awardTitle, awardNumber, awardURI;

    @Builder
    @JsonCreator
    private Funding(
        @JsonProperty("funderName") String funderName,
        @JsonProperty("funderIdentifier") String funderIdentifier,
        @JsonProperty("awardTitle") String awardTitle,
        @JsonProperty("awardNumber") String awardNumber,
        @JsonProperty("awardURI") String awardURI){
        this.funderName = nullToEmpty(funderName);
        this.funderIdentifier = nullToEmpty(funderIdentifier);
        this.awardTitle = nullToEmpty(awardTitle);
        this.awardNumber = nullToEmpty(awardNumber);
        this.awardURI = nullToEmpty(awardURI);
    }
}
