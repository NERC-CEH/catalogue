package uk.ac.ceh.gateway.catalogue.erammp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class ProcessingStep {
    private final String step;

    @Builder
    @JsonCreator
    private ProcessingStep(
        @JsonProperty("step") String step) {
        this.step =  nullToEmpty(step);
    }
}
