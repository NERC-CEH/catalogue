package uk.ac.ceh.gateway.catalogue.converters;

import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.NodeListConverter;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.ceh.gateway.catalogue.converters.ukeofXml2EfDocument.BoundingBoxConverter;
import uk.ac.ceh.gateway.catalogue.converters.ukeofXml2EfDocument.GeometryConverter;
import uk.ac.ceh.gateway.catalogue.ef.EFDocument;

/**
 *
 * @author jcoop, cjohn
 */
public class UkeofXml2EFDocumentMessageConverter extends AbstractHttpMessageConverter<EFDocument> {
    private final XPathExpression id, title, description, type;
    private final XPath xpath;
    private final GeometryConverter geometryConverter;
    private final BoundingBoxConverter boundingBoxConverter;
    
    public UkeofXml2EFDocumentMessageConverter() throws XPathExpressionException {
        super(MediaType.APPLICATION_XML);
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new HardcodedUKEOFNamespaceResolver());
        this.id = xpath.compile("/*/ukeof:metadata/ukeof:fileIdentifier");
        this.title = xpath.compile("/*/ukeof:name");
        this.description = xpath.compile("/*/ukeof:description");
        this.type = xpath.compile("local-name(/*)");
        this.geometryConverter = new GeometryConverter(xpath);
        this.boundingBoxConverter = new BoundingBoxConverter(xpath);
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(EFDocument.class);
    }

    @Override
    protected EFDocument readInternal(Class<? extends EFDocument> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputMessage.getBody());

            EFDocument toReturn = new EFDocument();
            toReturn.setId(id.evaluate(document));
            toReturn.setTitle(title.evaluate(document));
            toReturn.setDescription(description.evaluate(document));
            toReturn.setType(type.evaluate(document));
            toReturn.setGeometry(geometryConverter.convert(document));
            toReturn.setBoundingBox(boundingBoxConverter.convert(document));
            return toReturn;
        }
        catch(ParserConfigurationException pce) {
            throw new HttpMessageNotReadableException("The document reader was not set up correctly", pce);
        } catch (SAXException se) {
            throw new HttpMessageNotReadableException("The xml content could not be parsed", se);
        } catch (XPathExpressionException ex) {
            throw new HttpMessageNotReadableException("An xpath failed to evaluate", ex);
        }
    }

    @Override
    protected void writeInternal(EFDocument t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        throw new HttpMessageNotWritableException("I will not be able to write that document for you");
    }
    
    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false; // I can never write
    }
    
    public static List<String> getListOfStrings(Document document, XPathExpression expression) throws XPathExpressionException{
        return NodeListConverter.getListOfStrings((NodeList) expression.evaluate(document, XPathConstants.NODESET));
    }
}