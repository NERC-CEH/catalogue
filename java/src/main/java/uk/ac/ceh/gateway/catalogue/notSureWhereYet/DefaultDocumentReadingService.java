package uk.ac.ceh.gateway.catalogue.notSureWhereYet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
@Service
public class DefaultDocumentReadingService implements DocumentReadingService<GeminiDocument> {
    private final ObjectMapper mapper;
    
    @Autowired
    public DefaultDocumentReadingService(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public GeminiDocument read(InputStream inputStream, MediaType contentType) throws IOException, UnknownContentTypeException {
        if(contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return mapper.readValue(inputStream, GeminiDocument.class);
        }
        else {
            throw new UnknownContentTypeException("I don't know how to read " + contentType);
        }
    }
}
