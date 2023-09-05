package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Builder;
import lombok.Value;

@Value
public class Conformity {
    private final String specification, degree, explanation;
    
    @Builder
    @JsonCreator
    public Conformity(
        @JsonProperty("specification") String specification,
        @JsonProperty("degree") String degree,
        @JsonProperty("explanation") String explanation
    ) {
        this.specification = nullToEmpty(specification);
        this.degree = nullToEmpty(degree);
        this.explanation = nullToEmpty(explanation);
    }
}
