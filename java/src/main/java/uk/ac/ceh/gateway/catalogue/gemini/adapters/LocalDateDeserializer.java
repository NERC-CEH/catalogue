package uk.ac.ceh.gateway.catalogue.gemini.adapters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.time.LocalDate;
import uk.ac.ceh.gateway.catalogue.gemini.LocalDateFactory;

/**
 *
 * @link http://blog.chris-ritchie.com/2014/09/localdate-java-8-custom-serializer.html
 */
public class LocalDateDeserializer extends JsonDeserializer<LocalDate>{

    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        TextNode node = jp.getCodec().readTree(jp);
        return LocalDateFactory.parse(node.textValue());
    }
}
