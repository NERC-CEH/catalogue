package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class Funding {
    String funderName, funderIdentifier, awardTitle, awardNumber, awardURI;

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

    public boolean isOrcid() {
        return funderIdentifier.matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$");
    }

    public boolean isRor() {
        return funderIdentifier.matches("^https://ror.org/\\w{8,10}$");
    }
}
