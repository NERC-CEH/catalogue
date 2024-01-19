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
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;

/**
 * Extract anchors or text from useConstraints and accessConstraints.
 */
public class LegalConstraintsWithAnchorConverter {
    private static final String URI = "*/gmx:Anchor/@xlink:href";
    private static final String VALUE = "*/gmx:Anchor/@xlink:title | */gmx:Anchor | */gco:CharacterString";
    private static final String CODE = "*/gmd:MD_RestrictionCode/@codeListValue";
    private final XPathExpression legalConstraints, uri, code, value;

    public LegalConstraintsWithAnchorConverter(XPath xpath, String legalConstraints) throws XPathExpressionException {
        this.legalConstraints = xpath.compile(legalConstraints);
        this.uri = xpath.compile(URI);
        this.code = xpath.compile(CODE);
        this.value = xpath.compile(VALUE);
    }

    public List<ResourceConstraint> convert(Document document) throws XPathExpressionException {
        List<ResourceConstraint> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) legalConstraints.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            ResourceConstraint resourceConstraint = ResourceConstraint.builder()
                .value(value.evaluate(node))
                .uri(uri.evaluate(node))
                .code(code.evaluate(node))
                .build();
            toReturn.add(resourceConstraint);
        }
        return toReturn;
    }

}
