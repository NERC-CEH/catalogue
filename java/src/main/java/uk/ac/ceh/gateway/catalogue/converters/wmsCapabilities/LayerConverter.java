package uk.ac.ceh.gateway.catalogue.converters.wmsCapabilities;

import com.google.common.base.Strings;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ceh.gateway.catalogue.ogc.Layer;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ToString
public class LayerConverter {
    private final XPathExpression layer, name, title, legendUrl;

    public LayerConverter(XPath xpath) throws XPathExpressionException {
        this.layer = xpath.compile("//wms:Layer[wms:Name][not(wms:Layer)]");
        this.name = xpath.compile("wms:Name");
        this.title = xpath.compile("wms:Title");
        this.legendUrl = xpath.compile("wms:Style[wms:Name = 'default']/wms:LegendURL/wms:OnlineResource/@xlink:href");
        log.info("Creating {}", this);
    }

    public List<Layer> convert(Document document) throws XPathExpressionException {
        List<Layer> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) layer.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++) {
            Node layerNode = nodeList.item(i);

            Layer toAdd = new Layer();
            toAdd.setName(name.evaluate(layerNode));
            toAdd.setTitle(title.evaluate(layerNode));
            toAdd.setLegendUrl(Strings.emptyToNull(legendUrl.evaluate(layerNode)));

            toReturn.add(toAdd);
        }
        return toReturn;
    }
}
