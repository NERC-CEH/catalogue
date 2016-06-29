package uk.ac.ceh.gateway.catalogue.converters;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.NodeListConverter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.ceh.gateway.catalogue.ogc.FeatureInfo;
import uk.ac.ceh.gateway.catalogue.ogc.FeatureInfo.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.FeatureInfo.Layer.Feature;

/**
 *
 * @author cjohn
 */
public class Gml2FeatureInfoMessageConverter extends AbstractHttpMessageConverter<FeatureInfo> {
    private static final String LAYERS = "//msGMLOutput/*";
    private static final String FEATURES = "*";
    private static final String ATTRIBUTES = "*[not(*)]";
    private final XPath xpath;
    private final XPathExpression layers, features, attributes;
    
    public Gml2FeatureInfoMessageConverter() throws XPathExpressionException {
        super(MediaType.parseMediaType("application/vnd.ogc.xml"));
        xpath = XPathFactory.newInstance().newXPath();

        this.layers = xpath.compile(LAYERS);
        this.features = xpath.compile(FEATURES);
        this.attributes = xpath.compile(ATTRIBUTES);
    }
    
    @Override
    protected FeatureInfo readInternal(Class<? extends FeatureInfo> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputMessage.getBody());

            FeatureInfo toReturn = new FeatureInfo();
            toReturn.setLayers(getLayers((NodeList) layers.evaluate(document, XPathConstants.NODESET)));
            
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

    private List<Layer> getLayers(NodeList nodes) throws XPathExpressionException {
        List<Layer> toReturn = new ArrayList<>();
        for(int i=0; i<nodes.getLength(); i++){
            Node node = nodes.item(i);
            String layerName = node.getNodeName();
            Layer layer = new Layer();
            layer.setName(layerName.substring(0, layerName.lastIndexOf('_')));
            layer.setFeatures(getFeatures((NodeList)features.evaluate(node, XPathConstants.NODESET)));

            toReturn.add(layer);
        }
        return toReturn;
    }

    private List<Feature> getFeatures(NodeList nodes) throws XPathExpressionException {
        List<Feature> toReturn = new ArrayList<>();

        for(int i=0; i<nodes.getLength(); i++){
            Node node = nodes.item(i);
            Feature feature = new Feature();
            feature.setAttributes(getAttributes((NodeList)attributes.evaluate(node, XPathConstants.NODESET)));
            toReturn.add(feature);
        }
        return toReturn;
    }

    private Map<String,String> getAttributes(NodeList nodes) throws XPathExpressionException {
        Map<String,String> attrs = new HashMap<>();
        for(int i=0; i<nodes.getLength(); i++){
            Node node = nodes.item(i);
            attrs.put(node.getNodeName(), node.getTextContent());
        }
        return attrs;            
    }

    @Override
    protected void writeInternal(FeatureInfo t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        throw new HttpMessageNotWritableException("I will not be able to write that document for you");
    }
    
    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false; // I can never write
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(FeatureInfo.class);
    }
}
