package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

/**
 * Extract anchors or text from useLimitation and otherConstraints. 
 */
public class LegalConstraintsWithAnchorConverter {
    private static final String URI = "@xlink:href";
    private static final String TITLE = "@xlink:title";
    private final XPathExpression legalConstraints, uri, title;
    
    public LegalConstraintsWithAnchorConverter(XPath xpath, String legalConstraints) throws XPathExpressionException {
        this.legalConstraints = xpath.compile(legalConstraints);
        this.uri = xpath.compile(URI);
        this.title = xpath.compile(TITLE);
    }

    public List<Keyword> convert(Document document) throws XPathExpressionException {
        List<Keyword> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) legalConstraints.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            String text = node.getTextContent();
            if (text.isEmpty()) {
                text = title.evaluate(node);
            }
            Keyword keyword = Keyword.builder()
                .value(text)
                .URI(uri.evaluate(node))
                .build();
            toReturn.add(keyword);
        }
        return toReturn;
    }
    
}