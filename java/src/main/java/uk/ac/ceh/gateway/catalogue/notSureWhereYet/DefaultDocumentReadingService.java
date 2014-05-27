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
import uk.ac.ceh.gateway.catalogue.gemini.Metadata;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
@Service
public class DefaultDocumentReadingService implements DocumentReadingService<Metadata> {
    private final ObjectMapper mapper;
    
    @Autowired
    public DefaultDocumentReadingService(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public Metadata read(InputStream inputStream, MediaType contentType) throws IOException, UnknownContentTypeException {
        if(contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return mapper.readValue(inputStream, Metadata.class);
        }
        else if(contentType.isCompatibleWith(MediaType.APPLICATION_XML)) {
            try {
                return readXml(inputStream);
            }
            catch(Exception ex) {
                throw new IOException(ex);
            }
        }
        else {
            throw new UnknownContentTypeException("I don't know how to read " + contentType);
        }
    }
    
    private  Map<String, Templates> schematron;
    private  XPathExpression filenameXPath;
    private  Transformer transformer;
    private  SAXTransformerFactory transformerFactory;
    
    private Metadata readXml(InputStream doc) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, XPathExpressionException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();


        this.transformerFactory = (SAXTransformerFactory) transformerFactory;
        this.transformer = transformerFactory.newTransformer();

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new HardcodedNamespaceResolver());
        this.filenameXPath = xpath.compile("/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString");
            
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document parse = builder.parse(new InputSource(doc));
        
        Metadata toReturn = new Metadata();
        
        toReturn.setId(filenameXPath.evaluate(parse));
        
        return toReturn;
    }
    
    public static class HardcodedNamespaceResolver implements NamespaceContext{

        @Override
        public String getNamespaceURI(String prefix) {        
            switch(prefix.toLowerCase()) {
                case "gmd": return "http://www.isotc211.org/2005/gmd";
                case "gco": return "http://www.isotc211.org/2005/gco";
                case "csw": return "http://www.opengis.net/cat/csw/2.0.2";
                default:    return "";
            }
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return "";
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            return Collections.emptyIterator();
        }
    }
}
