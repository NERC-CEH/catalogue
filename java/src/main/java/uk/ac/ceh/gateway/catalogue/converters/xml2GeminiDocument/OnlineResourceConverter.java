package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ceh.gateway.catalogue.gemini.elements.OnlineResource;

/**
 *
 * @author cjohn
 */
public class OnlineResourceConverter {
    private static final String ONLINE_RESOURCE = "/*/gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/gmd:CI_OnlineResource";    
    private static final String URL = "gmd:linkage/gmd:URL";
    private static final String NAME = "gmd:name/gco:CharacterString";
    private static final String DESCRIPTION = "gmd:description/gco:CharacterString";
    
    private final XPathExpression onlineResource, url, name, description;
    
    public OnlineResourceConverter(XPath xpath) throws XPathExpressionException {
        this.onlineResource = xpath.compile(ONLINE_RESOURCE);
        this.url = xpath.compile(URL);
        this.name = xpath.compile(NAME);
        this.description = xpath.compile(DESCRIPTION);
    }
    
    public List<OnlineResource> convert(Document document) throws XPathExpressionException {
        List<OnlineResource> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) onlineResource.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node timePeriodNode = nodeList.item(i);
            toReturn.add(new OnlineResource(url.evaluate(timePeriodNode), 
                                            name.evaluate(timePeriodNode),
                                            description.evaluate(timePeriodNode)));
        }
        return toReturn;
    }
}
