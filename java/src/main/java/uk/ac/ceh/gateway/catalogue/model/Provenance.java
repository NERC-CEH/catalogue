package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class Provenance {
    private final String agent, entity, activity, thedate;
    
    @Builder
    @JsonCreator
    private Provenance(
        @JsonProperty("agent") String agent,
        @JsonProperty("entity") String entity,
        @JsonProperty("activity") String activity,
        @JsonProperty("thedate") String thedate) {
        this.agent =  nullToEmpty(agent);
        this.entity =  nullToEmpty(entity);
        this.activity =  nullToEmpty(activity);
        this.thedate = nullToEmpty(thedate);        
    }
}
