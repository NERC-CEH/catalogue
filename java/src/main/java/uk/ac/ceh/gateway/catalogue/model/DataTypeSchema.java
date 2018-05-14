package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import com.google.common.collect.Lists;
import lombok.Value;
import lombok.Builder;


@Value
public class DataTypeSchema {
    private final String name, title, description, type, units, format, maximum, minimum, maxLength, minLength;
    private final boolean unique;

    @Builder
    @JsonCreator
    //@JsonIgnoreProperties({"empty"})
    private DataTypeSchema(
        @JsonProperty("name") String name,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("type") String type,
        @JsonProperty("units") String units,
        @JsonProperty("format") String format,
        @JsonProperty("maximum") String maximum,
        @JsonProperty("minimum") String minimum,
        @JsonProperty("maxLength") String maxLength,
        @JsonProperty("minLength") String minLength,
        @JsonProperty("unique") boolean unique) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.type = type;
        this.units = units;
        this.format = format;
        this.maximum = maximum;
        this.minimum = minimum;
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.unique = unique;
        //this.unique = (unique = true) ? true : false;
    }        

}