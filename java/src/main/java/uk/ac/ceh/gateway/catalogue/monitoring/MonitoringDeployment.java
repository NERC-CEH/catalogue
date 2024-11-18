package uk.ac.ceh.gateway.catalogue.monitoring;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class MonitoringDeployment {
    private final String start, end, conditions;

    @Builder
    @JsonCreator
    private MonitoringDeployment(
        @JsonProperty("start") String start,
        @JsonProperty("end") String end,
        @JsonProperty("conditions") String conditions){
        this.start = nullToEmpty(start);
        this.end = nullToEmpty(end);
        this.conditions = nullToEmpty(conditions);
    }
}
