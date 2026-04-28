package uk.ac.ceh.gateway.catalogue.gemini.adapters;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import java.time.LocalDate;
import uk.ac.ceh.gateway.catalogue.gemini.LocalDateFactory;

public class LocalDateDeserializer extends ValueDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
        JsonNode node = jp.readValueAsTree();
        return LocalDateFactory.parse(node.stringValue());
    }
}
