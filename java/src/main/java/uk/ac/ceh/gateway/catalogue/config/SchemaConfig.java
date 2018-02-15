package uk.ac.ceh.gateway.catalogue.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

@Configuration
public class SchemaConfig {
    @Value("${schemas.location}") String schemas;
    
    @Bean @Qualifier("gemini")
    public Schema geminiSchema() throws IOException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source[] sources = Arrays.asList("gemini/srv/srv.xsd", "gemini/gmx/gmx.xsd")
                .stream()
                .map( (s) -> new StreamSource(new File(schemas, s)))
                .toArray(Source[]::new);
        
        return schemaFactory.newSchema(sources);
    }
}
