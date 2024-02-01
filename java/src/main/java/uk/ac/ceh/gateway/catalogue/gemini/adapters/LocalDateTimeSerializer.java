package uk.ac.ceh.gateway.catalogue.gemini.adapters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime>{

    @Override
    public void serialize(LocalDateTime localDateTime, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        log.debug("Serializing localDateTime: {}", localDateTime);
        generator.writeString(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
