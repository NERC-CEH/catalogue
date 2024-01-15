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
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

public class ResourceIdentifierConverter {
    private final static String RESOURCE_IDENTIFIERS = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*";
    private final static String CODE = "gmd:code/*";
    private final static String CODE_SPACE = "gmd:codeSpace/*";
    private final static String VERSION = "gmd:version/*";
    private final XPathExpression resourceIdentifiers, code, codeSpace, version;

    public ResourceIdentifierConverter(XPath xpath) throws XPathExpressionException {
        this.resourceIdentifiers = xpath.compile(RESOURCE_IDENTIFIERS);
        this.code = xpath.compile(CODE);
        this.codeSpace = xpath.compile(CODE_SPACE);
        this.version = xpath.compile(VERSION);
    }

    public List<ResourceIdentifier> convert(Document document) throws XPathExpressionException {
        List<ResourceIdentifier> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) resourceIdentifiers.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            ResourceIdentifier resourceIdentifier = ResourceIdentifier.builder()
                .code(code.evaluate(node))
                .codeSpace(codeSpace.evaluate(node))
                .version(version.evaluate(node))
                .build();
            toReturn.add(resourceIdentifier);
        }
        return toReturn;
    }
}
