package uk.ac.ceh.gateway.catalogue.gemini.adapters;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import java.time.LocalDateTime;
import uk.ac.ceh.gateway.catalogue.gemini.LocalDateFactory;

public class LocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
        JsonNode node = jp.readValueAsTree();
        return LocalDateFactory.parseForDateTime(node.stringValue());
    }
}
