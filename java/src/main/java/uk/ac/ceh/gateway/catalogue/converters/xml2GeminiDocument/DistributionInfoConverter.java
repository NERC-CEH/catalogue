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
import uk.ac.ceh.gateway.catalogue.gemini.DistributionInfo;

public class DistributionInfoConverter {
    private static final String DISTRIBUTION_INFO = "/*/gmd:distributionInfo/*/gmd:distributionFormat/*";
    private static final String NAME = "gmd:name/gco:CharacterString";
    private static final String VERSION = "gmd:version/gco:CharacterString";
    private final XPathExpression distributionInfo, name, version;

    public DistributionInfoConverter(XPath xpath) throws XPathExpressionException {
        this.distributionInfo = xpath.compile(DISTRIBUTION_INFO);
        this.name = xpath.compile(NAME);
        this.version = xpath.compile(VERSION);
    }

    public List<DistributionInfo> convert(Document document) throws XPathExpressionException {
        List<DistributionInfo> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) distributionInfo.evaluate(document, XPathConstants.NODESET);

        for(int i=0; i<nodeList.getLength(); i++){
            Node distributionInfoNode = nodeList.item(i);
            DistributionInfo distInfo = DistributionInfo.builder()
                .name(name.evaluate(distributionInfoNode))
                .version(version.evaluate(distributionInfoNode))
                .build();
            toReturn.add(distInfo);
        }
        return toReturn;
    }
}
