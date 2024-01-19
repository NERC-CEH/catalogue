package uk.ac.ceh.gateway.catalogue.gemini.adapters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.time.LocalDateTime;
import uk.ac.ceh.gateway.catalogue.gemini.LocalDateFactory;

/**
 *
 * @link http://blog.chris-ritchie.com/2014/09/localdate-java-8-custom-serializer.html
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>{

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        TextNode node = jp.getCodec().readTree(jp);
        return LocalDateFactory.parseForDateTime(node.textValue());
    }
}
