package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * A field of a data type's schema, or a model's parameter. Its name was stored as {@code name} until
 * fe683df84 (2024-10-25) renamed it {@code value}; records saved before then still say {@code name},
 * and lost it when read (NERC-CEH/dri-one#472).
 */
@DisplayName("ObservedProperty")
class ObservedPropertyTest {

    // mirrors WebConfig#objectMapper
    private final JsonMapper mapper = JsonMapper.builder()
        .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_EMPTY))
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .findAndAddModules()
        .build();

    @Test
    @DisplayName("reads its name from value")
    void readsValue() {
        ObservedProperty property = mapper.readValue("{\"value\": \"SITE\", \"type\": \"string\"}", ObservedProperty.class);

        assertThat(property.getValue(), equalTo("SITE"));
    }

    @Test
    @DisplayName("reads the name a record saved before the rename has, from name")
    void readsTheOldName() {
        ObservedProperty property = mapper.readValue("{\"name\": \"ID\", \"type\": \"integer\"}", ObservedProperty.class);

        assertThat(property.getValue(), equalTo("ID"));
        assertThat(property.getType(), equalTo("integer"));
    }

    @Test
    @DisplayName("writes an old record's name back as value, so a save keeps it")
    void writesTheOldNameAsValue() {
        ObservedProperty property = mapper.readValue("{\"name\": \"ID\", \"type\": \"integer\"}", ObservedProperty.class);

        String json = mapper.writeValueAsString(property);

        assertThat(json, allOf(containsString("\"value\":\"ID\""), not(containsString("\"name\""))));
    }

    @Test
    @DisplayName("keeps the field names of a data type saved before the rename")
    void dataTypeKeepsOldFieldNames() {
        DataType dataType = mapper.readValue(
            "{\"type\": \"dataType\", \"schema\": [{\"name\": \"ID\", \"type\": \"integer\"}, {\"name\": \"YEAR\", \"type\": \"year\"}]}",
            DataType.class);

        assertThat(dataType.getSchema().stream().map(ObservedProperty::getValue).toList(), contains("ID", "YEAR"));
    }
}
