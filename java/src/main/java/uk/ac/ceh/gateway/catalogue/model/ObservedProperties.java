package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import com.google.common.collect.Lists;
import lombok.Value;
import lombok.Builder;

@Value
public class ObservedProperties {
    private final String name, title, description, type, units, unitsUri, format;
    private final Constraints constraints;

    @Builder
    @JsonCreator
    private ObservedProperties(
        @JsonProperty("name") String name,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("type") String type,
        @JsonProperty("units") String units,
        @JsonProperty("unitsUri") String unitsUri,
        @JsonProperty("format") String format,
        @JsonProperty("constraints") Constraints constraints) {
        this.name =  nullToEmpty(name);
        this.title =  nullToEmpty(title);
        this.description =  nullToEmpty(description);
        this.type =  nullToEmpty(type);
        this.units =  nullToEmpty(units);
        this.unitsUri =  nullToEmpty(unitsUri);
        this.format =  nullToEmpty(format);
        this.constraints = constraints;
    }

    @Value
    @JsonIgnoreProperties({"empty"})
    public static class Constraints {
        private final String minimum, maximum, minLength, maxLength;
        private final boolean unique;

        @Builder
        @JsonCreator
        private Constraints(
            @JsonProperty("minimum") String minimum,
            @JsonProperty("maximum") String maximum,
            @JsonProperty("minLength") String minLength,
            @JsonProperty("maxLength") String maxLength,
            @JsonProperty("unique") boolean unique) {
            this.minimum = nullToEmpty(minimum);
            this.maximum = nullToEmpty(maximum);
            this.minLength = nullToEmpty(minLength);
            this.maxLength = nullToEmpty(maxLength);
            this.unique = unique;
        }

    }
}
