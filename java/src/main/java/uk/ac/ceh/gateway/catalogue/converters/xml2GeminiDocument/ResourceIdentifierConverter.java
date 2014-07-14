package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import java.util.HashSet;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResourceIdentifier;

public class ResourceIdentifierConverter {
    private final static String RESOURCE_IDENTIFIERS = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*";
    private final static String CODE = "gmd:code/gco:CharacterString";
    private final static String CODE_SPACE = "gmd:codeSpace/gco:CharacterString";
    private final XPathExpression resourceIdentifiers, code, codeSpace;

    public ResourceIdentifierConverter(XPath xpath) throws XPathExpressionException {
        this.resourceIdentifiers = xpath.compile(RESOURCE_IDENTIFIERS);
        this.code = xpath.compile(CODE);
        this.codeSpace = xpath.compile(CODE_SPACE);
    }

    public Set<ResourceIdentifier> convert(Document document) throws XPathExpressionException {
        Set<ResourceIdentifier> toReturn = new HashSet<>();
        NodeList nodeList = (NodeList) resourceIdentifiers.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node resourceIdentifierNode = nodeList.item(i);
            ResourceIdentifier resourceIdentifier = ResourceIdentifier.builder()
                .code(code.evaluate(resourceIdentifierNode))
                .codeSpace(codeSpace.evaluate(resourceIdentifierNode))
                .build();
            toReturn.add(resourceIdentifier);
        }
        return toReturn;
    }
}