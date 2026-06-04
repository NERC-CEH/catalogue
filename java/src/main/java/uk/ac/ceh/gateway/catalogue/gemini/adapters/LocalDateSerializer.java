package uk.ac.ceh.gateway.catalogue.gemini.adapters;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import java.time.LocalDate;

public class LocalDateSerializer extends ValueSerializer<LocalDate> {

    @Override
    public void serialize(LocalDate localDate, JsonGenerator generator, SerializationContext provider) throws JacksonException {
        generator.writeString(localDate.toString());
    }
}
