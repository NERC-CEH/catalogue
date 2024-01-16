package uk.ac.ceh.gateway.catalogue.infrastructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class InfrastructureCategory {
    private final String value, description, infrastructureClass, uri;

    @Builder
    @JsonCreator
    public InfrastructureCategory(
        @JsonProperty("value") String value,
        @JsonProperty("description") String description,
        @JsonProperty("infrastructureClass") String infrastructureClass,
        @JsonProperty("uri") String uri){
        this.value = nullToEmpty(value);
        this.description = nullToEmpty(description);
        this.infrastructureClass = nullToEmpty(infrastructureClass);
        this.uri = nullToEmpty(uri);
    }

}
