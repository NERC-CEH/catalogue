package uk.ac.ceh.gateway.catalogue.gemini;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateSerializer;


@Value
public class ResourceMaintenance {
    String frequencyOfUpdate;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    LocalDate lastUpdate;
    String note;
    Scope scope;

    @Builder
    @JsonCreator
    private ResourceMaintenance(
        @JsonProperty("frequencyOfUpdate") String frequencyOfUpdate,
        @JsonProperty("lastUpdate") LocalDate lastUpdate,
        @JsonProperty("note") String note,
        @JsonProperty("scope") Scope scope) {

        this.frequencyOfUpdate = Strings.nullToEmpty(frequencyOfUpdate);
        this.lastUpdate = lastUpdate;
        this.note = Strings.nullToEmpty(note);
        this.scope = scope;
    }

    @Value
    public static class Scope {

        boolean attribute;
        boolean feature;
        boolean attributeInstance;
        boolean featureInstance;

        @Builder
        @JsonCreator
        private Scope(
            @JsonProperty("attribute") boolean attribute,
            @JsonProperty("feature") boolean feature,
            @JsonProperty("attributeInstance") boolean attributeInstance,
            @JsonProperty("featureInstance") boolean featureInstance) {

            this.attribute = attribute;
            this.feature = feature;
            this.attributeInstance = attributeInstance;
            this.featureInstance = featureInstance;
        }
    }
}