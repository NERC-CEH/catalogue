package uk.ac.ceh.gateway.catalogue.converters.wmsCapabilities;

import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.ogc.Layer;

/**
 *
 * @author cjohn
 */
public class LayerConverter {    
    private final XPathExpression layer, name;
    
    public LayerConverter(XPath xpath) throws XPathExpressionException {
        this.layer = xpath.compile("//wms:Layer[wms:Name]");
        this.name = xpath.compile("wms:Name");
    }
    
    public List<Layer> convert(Document document) throws XPathExpressionException {
        List<Layer> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) layer.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++) {
            Node layerNode = nodeList.item(i);
            
            Layer toAdd = new Layer();
            toAdd.setName(name.evaluate(layerNode));
            
            toReturn.add(toAdd);
        }
        return toReturn;
    }    
}
