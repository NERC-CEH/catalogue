package uk.ac.ceh.gateway.catalogue.converters;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.ac.ceh.gateway.catalogue.converters.wmsCapabilities.LayerConverter;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

@Slf4j
@ToString
public class Xml2WmsCapabilitiesMessageConverter extends AbstractHttpMessageConverter<WmsCapabilities> {
    private static final String MAP_URL = "//wms:GetMap/*/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href";
    private static final String FEATURE_INFO_URL = "//wms:GetFeatureInfo/*/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href";

    private final LayerConverter layerConverter;
    private final XPathExpression map, featureInfo;

    @SneakyThrows
    public Xml2WmsCapabilitiesMessageConverter() {
        super(MediaType.TEXT_XML, MediaType.APPLICATION_XML);

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new HardcodedOGCNamespaceResolver());
        this.layerConverter = new LayerConverter(xpath);
        this.map = xpath.compile(MAP_URL);
        this.featureInfo = xpath.compile(FEATURE_INFO_URL);
        log.info("Creating {}", this);
    }

    @Override
    @SneakyThrows
    protected WmsCapabilities readInternal(Class<? extends WmsCapabilities> clazz, HttpInputMessage inputMessage) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputMessage.getBody());

            WmsCapabilities toReturn = new WmsCapabilities();
            toReturn.setDirectMap(map.evaluate(document));
            toReturn.setDirectFeatureInfo(featureInfo.evaluate(document));
            toReturn.setLayers(layerConverter.convert(document));
            return toReturn;
        }
        catch(ParserConfigurationException pce) {
            throw new HttpMessageNotReadableException("The document reader was not set up correctly", pce, inputMessage);
        } catch (SAXException se) {
            throw new HttpMessageNotReadableException("The xml content could not be parsed", se, inputMessage);
        } catch (XPathExpressionException ex) {
            throw new HttpMessageNotReadableException("An xpath failed to evaluate", ex, inputMessage);
        }
    }

    @Override
    @SneakyThrows
    protected void writeInternal(WmsCapabilities t, HttpOutputMessage outputMessage) {
        throw new HttpMessageNotWritableException("I will not be able to write that document for you");
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false; // I can never write
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(WmsCapabilities.class);
    }
}
